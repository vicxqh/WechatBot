package com.vicxiao.weixinhacker.sender;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by xw on 2015/11/19.
 */
public class Senders {
    public volatile static TextSender textSender = null;

//    public static final Object sendingLock = new Object();
    //Guardedby sendingLock
    public volatile static TextIntent sending= null;
    public static final Object SIGNAL = new Object();

    public static void start() {
        XposedBridge.log("Start worker");
        Thread worker = new Thread() {

            @Override
            public void run() {
                while (true) {
                    try {
                        if (sending == null){
                            Intent intent = null;
                            intent = jobs.take();

                            if (intent instanceof TextIntent) {
                                if (textSender == null) {
                                    XposedBridge.log("TextSender not initialized! From doOneJob");
                                }
                                textSender.send(((TextIntent) intent).talker, ((TextIntent) intent).content);
                                sending = (TextIntent) intent;

                            } else {
                                //TODO
                                // Other types of message
                            }
                        }else {
                            XposedBridge.log("wait");
                            SIGNAL.wait();
                        }

                    } catch (InterruptedException e) {
                        XposedBridge.log(e.toString());
                    }
                }
            }
        };
        worker.start();
    }

    static abstract class Intent<T> {
        String talker;
        T content;

        public Intent(String talker, T content) {
            this.talker = talker;
            this.content = content;
        }
    }

    public static class TextIntent extends Intent<String> {
        public TextIntent(String talker, String content) {
            super(talker, content);
        }
        public String getTalker(){
            return this.talker;
        }

        public String getContent(){
            return this.content;
        }
    }

    private static BlockingQueue<Intent> jobs = new LinkedBlockingQueue<>(100);// May be enough


    /**
     * Only submit a job
     *
     * @param talker
     * @param content
     */
    public static void sendText(String talker, String content) {
        if (talker == null || content == null) {
            return;
        }

        if (textSender != null) {
            try {
                XposedBridge.log(String.format("Adding Intent[%s,%s]", talker, content));
                jobs.put(new TextIntent(talker, content));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            XposedBridge.log("TextSender not initialized! From sendText");
        }

    }

}
