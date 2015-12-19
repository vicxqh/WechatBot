package com.vicxiao.weixinhacker.sender;

import com.vicxiao.weixinhacker.message.TextMessage;

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
    public volatile static AudioSender audioSender = null;

    public volatile static Intent sending= null;


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
                                    continue;
                                }
                                textSender.send(((TextIntent) intent).talker, ((TextIntent) intent).content);
                                sending = intent;

                            } else if (intent instanceof AudioIntent){
                                if (textSender == null) {
                                    XposedBridge.log("AudioSender not initialized! From doOneJob");
                                    continue;
                                }
                                XposedBridge.log("Try sending audio message");
//                                audioSender.send(intent.getTalker(), ((AudioIntent) intent).getFileName());
                                sending = intent;
                            } else {
                                //TODO
                                // Other types of message
                            }
                        }else {
//                            XposedBridge.log("Sender is sleeping ");
                            Thread.sleep(500);
                        }

                    } catch (InterruptedException e) {
                        XposedBridge.log(e.toString());
                    }
                }
            }
        };
        worker.start();
    }

    public static abstract class Intent<T> {
        String talker;
        T content;

        public Intent(String talker, T content) {
            this.talker = talker;
            this.content = content;
        }

        public String getTalker(){
            return this.talker;
        }

        public T getContent(){
            return this.content;
        }
    }

    public static class TextIntent extends Intent<String> {
        public TextIntent(String talker, String content) {
            super(talker, content);
        }

    }

    public static class AudioIntent extends  Intent<String>{

        public AudioIntent(String talker, String content) {
            super(talker, content);
        }

        /**
         * Same as getContent
         * @return
         */
        public String getFileName(){
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

//        if (content.startsWith("@")){
//            int index = content.charAt(TextMessage.AT_SEPARATOR);
//            content = TextMessage.at("X") + content; // Hack to make sure the first person will be at
//        }

        if (textSender != null) {
            try {
                XposedBridge.log(String.format("Adding TextIntent[%s,%s]", talker, content));
                jobs.put(new TextIntent(talker, content));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            XposedBridge.log("TextSender not initialized! From sendText");
        }

    }

    /**
     * Submit a audio sending job.
     * @param talker
     * @param content
     */
    public static void sendAudio(String talker, String content){
        if (talker == null || content == null){
            return;
        }

        try {
            XposedBridge.log(String.format("Adding AudioIntent[%s,%s]", talker, content));
            jobs.put(new AudioIntent(talker, content));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
