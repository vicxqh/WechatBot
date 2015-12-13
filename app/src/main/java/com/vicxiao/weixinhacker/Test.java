package com.vicxiao.weixinhacker;

import com.vicxiao.weixinhacker.listener.Listeners;
import com.vicxiao.weixinhacker.listener.MessageListener;
import com.vicxiao.weixinhacker.message.Message;
import com.vicxiao.weixinhacker.query.Query;
import com.vicxiao.weixinhacker.sender.Senders;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by xw on 2015/11/14.
 */
public class Test implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.tencent.mm")) {
            return;
        }
        LoadPackageHanlder.initQuery(loadPackageParam);
        // If you want to send message, you must also call loadMessageListener.
        LoadPackageHanlder.loadMessageListener(loadPackageParam);
        LoadPackageHanlder.loadTextSender(loadPackageParam);
        Senders.start();

        Listeners.addMessageListener(new MessageListener() {
            @Override
            public void onNewMessage(Message message) {
                XposedBridge.log("In listener + "+ message.toString());
                if (message.getContent().equalsIgnoreCase("Go") ){
                    for (int i = 0; i < 1; i++){
                        Senders.sendText("xwxwxw1235", "Xw" + i);
                        Senders.sendText("658998013@chatroom", "chatRoom" + i);
                    }
                }
            }
        });
    }

}
