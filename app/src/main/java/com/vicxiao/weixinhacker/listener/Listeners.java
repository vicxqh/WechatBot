package com.vicxiao.weixinhacker.listener;

import com.vicxiao.weixinhacker.message.Message;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by vic on 15-12-3.
 */
public class Listeners {
    private static Set<MessageListener> messageListeners = new HashSet<>();

    public static void addMessageListener(MessageListener listener){
        messageListeners.add(listener);
    }

    public static void handleNewMessage(Message message){
        for (MessageListener messageListener : messageListeners) {
            messageListener.onNewMessage(message);
        }
    }
}
