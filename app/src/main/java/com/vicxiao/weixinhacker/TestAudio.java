package com.vicxiao.weixinhacker;

import com.vicxiao.weixinhacker.sender.Senders;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by vic on 15-12-19.
 */
public class TestAudio implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.tencent.mm")) {
            return;
        }
        LoadPackageHandler.classLoader = loadPackageParam.classLoader;
        LoadPackageHandler.initQuery(loadPackageParam);
        // If you want to send message, you must also call loadMessageListener.
        LoadPackageHandler.loadMessageListener(loadPackageParam);
        LoadPackageHandler.loadTextSender(loadPackageParam);
        Senders.start();
//        LoadPackageHandler.testAudio(loadPackageParam);
    }
}
