package com.vicxiao.weixinhacker.query;

import android.database.Cursor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by vic on 15-12-3.
 */
public class Query {
    public static Method method = null;
    public static Object receiver = null;

    private static Cursor query(String queryString){
        Object result = null;
        try {
            result =  method.invoke(receiver, queryString, null);
        } catch (IllegalAccessException e) {
            XposedBridge.log(e.toString());
        } catch (InvocationTargetException e) {
            XposedBridge.log(e.toString());
        }
        return (Cursor)result;
    }
}
