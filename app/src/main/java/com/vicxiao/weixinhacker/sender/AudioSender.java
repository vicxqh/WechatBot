package com.vicxiao.weixinhacker.sender;

import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 *
 * !!!Should be singleton
 *
 * Created by vic on 15-12-19.
 */
public class AudioSender implements  ISender<String> {

    public static volatile String talker;

    public static String getTalkerName(){
        return talker;
    }

    private final XC_LoadPackage.LoadPackageParam loadPackageParam;

    public AudioSender(XC_LoadPackage.LoadPackageParam  loadPackageParam) {
        this.loadPackageParam = loadPackageParam;
    }

    @Override
    /**
     *
     */
    public void send(final String talker, String fileName) {
        AudioSender.talker = talker;
        XposedHelpers.callMethod(XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.tencent.mm.model.ah", loadPackageParam.classLoader), "to"), "d", XposedHelpers.newInstance(XposedHelpers.findClass("com.tencent.mm.modelvoice.f", loadPackageParam.classLoader), fileName));
    }
}
