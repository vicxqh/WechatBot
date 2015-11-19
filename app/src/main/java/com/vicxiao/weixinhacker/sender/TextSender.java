package com.vicxiao.weixinhacker.sender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;


/**
 * Created by xw on 2015/11/18.
 */
public class TextSender implements ISender<String>{
    private Object sender = null;
    private Method senderMethod = null;
    private String talker = null;

    public TextSender(Object sender, Method senderMethod, String talker) {
        this.sender = sender;
        this.senderMethod = senderMethod;
        this.talker = talker;
    }

    @Override
    public void send(String content) {
        if (sender == null) {
            XposedBridge.log("TextSender.sender is null");
            return;
        }
        XposedBridge.log("Sending message to [" + this.talker +"]");
        try {
            XposedBridge.log("Try send");
            senderMethod.invoke(sender, content);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

