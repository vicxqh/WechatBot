package com.vicxiao.weixinhacker.sender;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by xw on 2015/11/19.
 */
public class Senders {
    private static Map<String, TextSender> textSenderMap = new HashMap<>();
    private static TextSender logger = null;

    static abstract class Intent <T> {
        String talker;
        T content;

        public Intent(String talker, T content) {
            this.talker = talker;
            this.content = content;
        }
    }

    static class TextIntent extends Intent<String>{
        public TextIntent(String talker, String content) {
            super(talker, content);
        }
    }

    private static Queue<Intent> jobs = new LinkedList<>();

    public static int getReceiverCount(){
        return textSenderMap.size();
    }

    public static boolean doOneJob(){
        XposedBridge.log("Checking jobs..");
        if (jobs.size() > 0){
            Intent intent = jobs.poll();
            if (intent instanceof  TextIntent){
                TextSender sender = textSenderMap.get(((TextIntent)intent).talker);
                sender.send(((TextIntent)intent).content);
                return true;
            } else {
                return false;
            }
        }
        XposedBridge.log("No job to do.");
        return false;
    }

    /**
     * Only submit a job
     * @param talker
     * @param content
     */
    public static void sendText(String talker, String content){
        if (talker == null || content == null){
            return;
        }
        if (textSenderMap.containsKey(talker)){
            TextSender sender = textSenderMap.get(talker);
            if (sender != null){
                jobs.offer(new TextIntent(talker, content));
            } else {
                log(String.format("Log: Talker [%s] registered with null value!.", talker));
            }
        } else {
            log(String.format("Log: Talker [%s] not registerd.", talker));
        }
    }

    public static void registerSender(String talker, ISender sender){
        if (talker == null || sender == null){
            return ;
        }
        if (sender instanceof  TextSender){
            textSenderMap.put(talker, (TextSender) sender);
        }
    }

    public static void sendTextToAll(String content){
        for (String talker : textSenderMap.keySet()) {
            sendText(talker,content);
        }
    }

    public static void unregisterSender(String talker){
        if (talker == null){
            return ;
        }
        // For each map
        textSenderMap.remove(talker);
    }


    public static void setLogger(TextSender sender){
        logger = sender;
    }

    public static void log(String message){
        if (logger != null){
            logger.send(message);
        }
    }

}
