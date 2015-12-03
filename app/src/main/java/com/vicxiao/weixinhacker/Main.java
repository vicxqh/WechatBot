package com.vicxiao.weixinhacker;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by vic on 15-12-3.
 */
public class Main implements IXposedHookLoadPackage {


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        LoadPackageHanlder.initQuery(loadPackageParam);
        LoadPackageHanlder.loadTextSender(loadPackageParam);

    }
}
