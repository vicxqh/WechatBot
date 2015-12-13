package com.vicxiao.weixinhacker;

import android.database.Cursor;

import com.vicxiao.weixinhacker.listener.Listeners;
import com.vicxiao.weixinhacker.message.Message;
import com.vicxiao.weixinhacker.query.Query;
import com.vicxiao.weixinhacker.sender.Senders;
import com.vicxiao.weixinhacker.sender.TextSender;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by vic on 15-11-29.
 */
public class LoadPackageHanlder {
    public static void loadTextSender(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.tencent.mm.ui.chatting.ChattingUI$a", loadPackageParam.classLoader, "EI", java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String content = (String) param.args[0];
                if (content.equalsIgnoreCase("Start") && Senders.textSender == null){
                    Method method = (Method) param.method;
                    Object receiver = param.thisObject;
                    Senders.textSender = new TextSender(receiver, method);
                }
            }

        });
    }

    public static void initQuery(XC_LoadPackage.LoadPackageParam loadPackageParam){
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (Query.method == null){
                    Query.method  = (Method) param.method;
                    Query.receiver = param.thisObject;
                }
            }
        });
    }

    public static void logCursor(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (Query.method == null){
                    Query.method  = (Method) param.method;
                    Query.receiver = param.thisObject;
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Cursor result = (Cursor) param.getResult();
                //Log args
                StringBuilder argString = new StringBuilder();
                for (Object arg : param.args) {
                    if (arg == null){
                        argString.append("NULL" + "~~~");
                    } else {
                        argString.append(arg.toString() + "~~~");
                    }
                }

                XposedBridge.log(argString.substring(0, argString.length() - 3));
                StringBuilder cursor = new StringBuilder();
                if (result == null){
                    cursor.append("NULL");
                } else {
                    cursor.append(SampleMain.parseCursor(result));
                }
                XposedBridge.log(cursor.toString());

                XposedBridge.log("---------------------------------------------");
            }
        });
    }
    public static void loadMessageListener(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Cursor result = (Cursor) param.getResult();
                if (result != null && param.args[0].toString().startsWith("select * from message")) {
                    List<Message> messages = Message.getMessage(result);
                    for (Message message : messages) {
                        if (message == null){
                            continue;
                        }
                        XposedBridge.log(message.toString());
                        if (message.getCreateTime() >Message.lastSend){
                            Message.lastSend = message.getCreateTime();
                                if (Senders.sending != null){
                                    XposedBridge.log("Message sending");
                                    if (Senders.sending.getTalker().equals(message.getTalker()) && Senders.sending.getContent().equals(message.getContent())){
                                        Senders.sending = null;
                                        XposedBridge.log("Signal all");
                                    }
                                }
                            Listeners.handleNewMessage(message);
                        }
                    }

                }

            }
        });
    }
}
