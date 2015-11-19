package com.vicxiao.weixinhacker.sender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;


/**
 * Created by xw on 2015/11/18.
 */
public class TextSender {
    public static Object sender = null;
    public static Method senderMethod = null;
    public static String currentTalker = null;

    public static void send(String talker, String content) {
        if (sender == null) {
            XposedBridge.log("TextSender.sender is null");
            return;
        }
        XposedBridge.log("Sending message...");
        try {
            XposedBridge.log("Try send");
            currentTalker = talker;
            senderMethod.invoke(sender, content);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

