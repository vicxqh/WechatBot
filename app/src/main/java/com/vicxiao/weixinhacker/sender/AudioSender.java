package com.vicxiao.weixinhacker.sender;

import com.vicxiao.weixinhacker.LoadPackageHandler;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by vic on 15-12-19.
 */
public class AudioSender implements  ISender<String> {
    @Override
    /**
     *
     */
    public void send(final String talker, String fileName) {
        XposedHelpers.findAndHookMethod("com.tencent.mm.modelvoice.u", LoadPackageHandler.classLoader, "jH", String.class, new XC_MethodHook() {
            String sendFile = "";

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Object v = param.args[0];
                String fileName = v.toString();
                Object queryResult = param.getResult();
                XposedBridge.log("fileName: " + fileName + " query result: " + queryResult);
                if (queryResult != null && !sendFile.equals(fileName)) {
                    sendFile = fileName;
                    int result = XposedHelpers.getIntField(queryResult, "status");
                    XposedBridge.log("status: " + result);
                    XposedHelpers.setIntField(queryResult, "bWK", 0);//read offset
                    XposedHelpers.setIntField(queryResult, "status", 3);// status must be 3 for sending
                    XposedHelpers.setObjectField(queryResult, "aBT", talker);
                }
                if (queryResult != null && sendFile.equals(fileName)) {
                    int result = XposedHelpers.getIntField(queryResult, "status");
                    XposedBridge.log("status: " + result);
                    XposedHelpers.setIntField(queryResult, "status", 3);
                }
            }
        });

        //这一句话是调用发送语句，参数为文件名，调用过程中会触发上面hook的函数
        XposedHelpers.callMethod(XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.tencent.mm.model.ah", LoadPackageHandler.classLoader), "to"), "d", XposedHelpers.newInstance(XposedHelpers.findClass("com.tencent.mm.modelvoice.f", LoadPackageHandler.classLoader), fileName));
    }
}
