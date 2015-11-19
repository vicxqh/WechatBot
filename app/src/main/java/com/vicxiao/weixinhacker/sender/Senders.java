package com.vicxiao.weixinhacker.sender;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xw on 2015/11/19.
 */
public class Senders {
    private static Map<String, TextSender> textSenderMap = new HashMap<>();
    private static TextSender logger = null;

    public static int getReceiverCount(){
        return textSenderMap.size();
    }

    public static void registerSender(String talker, ISender sender){
        if (talker == null || sender == null){
            return ;
        }
        if (sender instanceof  TextSender){
            textSenderMap.put(talker, (TextSender) sender);
        }
    }

    public static void sendText(String talker, String content){
        if (talker == null || content == null){
            return;
        }
        if (textSenderMap.containsKey(talker)){
            TextSender sender = textSenderMap.get(talker);
            if (sender != null){
                sender.send(content);
            } else {
                log(String.format("Log: Talker [%s] registered with null value!.", talker));
            }
        } else {
            log(String.format("Log: Talker [%s] not registerd.", talker));
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
