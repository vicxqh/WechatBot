package com.vicxiao.weixinhacker;

import android.database.Cursor;

import com.vicxiao.weixinhacker.sender.Senders;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

/**
 * Created by xw on 2015/11/13.
 */
public class SampleMain implements IXposedHookLoadPackage {
    boolean submitted = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.tencent.mm")) {
//            XposedBridge.log("WechatBot loading...!");
//            loadTextSender(loadPackageParam);
//            XposedBridge.log("WechatBot loaded!");
//            hookLogger(loadPackageParam);

            LoadPackageHanlder.loadTextSender(loadPackageParam);

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

}
