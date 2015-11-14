package com.vicxiao.weixinhacker;

import android.database.Cursor;

import java.lang.reflect.Method;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by xw on 2015/11/14.
 */
public class Test implements IXposedHookLoadPackage {

    Method sender = null;
    Object receiver = null;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.tencent.mm")) {
            return;
        }
        XposedBridge.log("Weixin loaded!");
        Class<?> clz = findClass("com.tencent.mm.pluginsdk.ui.chat.ChatFooter", loadPackageParam.classLoader);
        for (Method method : clz.getMethods()) {
            if (method.getName().equals("setFooterEventListener")){
                XposedBridge.hookMethod(method, new XC_MethodHook(){
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        receiver = param.args[0];
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    }
                });
            }
        }

        findAndHookMethod("com.tencent.mm.ui.chatting.x", loadPackageParam.classLoader, "qc",String.class, new XC_MethodHook() {
            boolean switcher = false;

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (((String)param.args[0]).equals("Start")){
                    if(param.method instanceof Method){
                        sender = (Method) param.method;
                    }
                } else {
                    if (sender != null && receiver != null){
                        sender.invoke(receiver, "haha");
                    }
                }
            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                CharSequence result = (CharSequence) param.getResult();
//                if (result != null && result.toString().equals("131")){
//                    XposedBridge.log("=========================");
//                    Integer x = (Integer)param.getResult();
//                    int y = x+1;
//                }
            }
        });
    }
}
