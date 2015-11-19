package com.vicxiao.weixinhacker;

import com.vicxiao.weixinhacker.sender.Senders;
import com.vicxiao.weixinhacker.sender.TextSender;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by xw on 2015/11/13.
 */
public class Main implements IXposedHookLoadPackage {
    boolean tried = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.tencent.mm")) {
//            XposedBridge.log("WechatBot loading...!");
//            loadTextSender(loadPackageParam);
            loadTrigger(loadPackageParam);
//            XposedBridge.log("WechatBot loaded!");
//            hookLogger(loadPackageParam);

            loadTextSender(loadPackageParam);

        }
    }

    private void hookLogger(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class wechatLogClass = findClass("com.tencent.mm.sdk.platformtools.v", loadPackageParam.classLoader);
        XposedBridge.hookAllMethods(wechatLogClass, "d", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int i = 0;
                XposedBridge.log("----------------------");
                for (Object arg : param.args) {
                    if (arg != null) {
                        XposedBridge.log(arg.toString());
                    } else {
                        XposedBridge.log("XW_NULL");
                    }
                }
            }
        });
    }

    private void loadTrigger(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("rawQuery");
                if (param.args != null && param.args.length > 0) {
                    if (param.args[0].toString().startsWith("select * from message")) {
                        if (Math.random() > 0.6 && !tried && Senders.getReceiverCount() == 2) {
                            tried = true;
                            Senders.sendText("1086230229@chatroom", "Chatroom");
                            Senders.sendText("xwxwxw1235", "xw");
                            Senders.log("log");
                            Senders.sendTextToAll("ToAll");
                        }
                    }
                }

            }
        });
    }

    private void loadTextSender(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.tencent.mm.ui.chatting.ChattingUI$a", loadPackageParam.classLoader, "EI", java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String content = (String) param.args[0];
                if (isCommand(content)){
                    Method method = (Method) param.method;
                    Object receiver = param.thisObject;
                    String talkerName = (String) XposedHelpers.callMethod(receiver,"getTalkerUserName");
                    if (content.equalsIgnoreCase("Register")){
                        XposedBridge.log(String.format("Register sender [%s]", talkerName));
                        Senders.registerSender(talkerName, new TextSender(receiver,method,talkerName));
                    } else if (content.equalsIgnoreCase("Unregister")){
                        XposedBridge.log(String.format("Unregister sender [%s]", talkerName));
                        Senders.unregisterSender(talkerName);
                    } else if (content.equalsIgnoreCase("log on")){
                        XposedBridge.log("Log is on");
                        Senders.setLogger(new TextSender(receiver,method,talkerName));
                    } else if (content.equalsIgnoreCase("log off")){
                        XposedBridge.log("Log is off");
                        Senders.setLogger(null);
                    }
                }
            }

            private boolean isCommand(String content){
                if(content == null){
                    return false;
                }
                String[] commands = new String[]{"Register", "Unregister", "log on", "log off"};
                for (String command : commands) {
                    if (command.equalsIgnoreCase(content)){
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
