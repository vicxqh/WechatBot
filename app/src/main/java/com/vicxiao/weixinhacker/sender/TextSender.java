package com.vicxiao.weixinhacker.sender;

import com.vicxiao.weixinhacker.message.TextMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


/**
 * Created by xw on 2015/11/18.
 */
public class TextSender implements ISender<String>{
    private Object sender = null;
    private Method senderMethod = null;

    public TextSender(Object sender, Method senderMethod) {
        this.sender = sender;
        this.senderMethod = senderMethod;
    }

    @Override
    public void send(String talker, String content) {
        if (sender == null) {
            XposedBridge.log("TextSender.sender is null");
            return;
        }
        try {
            XposedBridge.log("Setting talker name");
            XposedHelpers.setObjectField(sender, "kbO", talker);
            Object kaS = XposedHelpers.getObjectField(sender, "kaS");
            XposedHelpers.setObjectField(kaS, "field_username", talker);
            XposedBridge.log("Sending message to [" + talker + "]");
            senderMethod.invoke(sender, content);
            XposedBridge.log("Sent message to [" + talker + "]");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

