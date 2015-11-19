package com.vicxiao.weixinhacker;

import com.vicxiao.weixinhacker.sender.TextSender;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
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
                        if (Math.random() > 0.6 && !tried && TextSender.senderMethod != null) {
                            TextSender.send("1086230229@chatroom", "From Xposed");
                            tried = true;

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
                if (TextSender.senderMethod == null) {
                    XposedBridge.log("Get sender");
                    TextSender.senderMethod = (Method) param.method;
                    TextSender.sender = param.thisObject;
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                TextSender.currentTalker = null;// Clear talker
            }
        });
        findAndHookMethod("com.tencent.mm.ui.chatting.ChattingUI$a", loadPackageParam.classLoader, "getTalkerUserName", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (TextSender.currentTalker != null) {
                    XposedBridge.log("Try to modify sender");
                    param.setResult(TextSender.currentTalker);
                }
            }
        });
    }
}
