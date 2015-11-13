package com.vicxiao.weixinhacker;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by xw on 2015/11/13.
 */
public class Main implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.tencent.mm")) {
            return;
        }
        XposedBridge.log("Weixin loaded!");
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
            boolean switcher = false;

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("rawQuery");
                switcher = false;
                if (param.args != null && param.args.length > 0) {
                    if (param.args[0].toString().startsWith("select * from message")) {
                        switcher = true;
//                        XposedBridge.log("true");
                    }
                }

            }

            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                Cursor result = (Cursor) param.getResult();
                if (result != null && switcher) {
                    List<Message> messages = getMessage(result);
                    for (Message message : messages) {
                        XposedBridge.log(message.toString());
                    }
                }

            }
        });

//        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
//
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("rawQuery");
//                for (Object o : param.args) {
//                    if (o != null) {
//                        XposedBridge.log(o.toString());
//                    } else {
//                        XposedBridge.log("NULL");
//                    }
//                }
//
//            }
//
//            @Override
//            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                Cursor result = (Cursor) param.getResult();
//                if (result == null){
//                    XposedBridge.log("Null Cursor");
//                } else {
//                    XposedBridge.log(parseCursor(result));
//                }
//                XposedBridge.log("--------------------");
//            }
//        });
//        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "query",String.class, String[].class, String.class, String[].class,String.class, String.class, String.class,  new XC_MethodHook() {
//
//            boolean switcher = false;
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                XposedBridge.log("query");
//                switcher = false;
//                if(param.args != null && param.args.length > 0){
//                    if (param.args[0].toString().equals("rconversation")){
//                        switcher = true;
//                    }
//                }
//
//            }
//
//            @Override
//            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                Cursor result = (Cursor) param.getResult();
//                if (result == null){
//                    XposedBridge.log("Null Cursor");
//                } else {
//                    XposedBridge.log(parseCursor(result));
//                }
//                XposedBridge.log("--------------------");
//            }
//        });
    }

    static class Message {
        int createTime;
        String talker;
        String id;
        String content;

        public Message(int createTime, String talker, String id, String content) {
            this.createTime = createTime;
            this.talker = talker;
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString(){
            return "CreateTime["+this.createTime+"]" + "\t talker["+ this.talker +"]" + "\tID["+this.id +"]"+"\tContent["+this.content+"]\n";
        }
    }

    private static Message last = null;
    private static List<Message> getMessage(Cursor cursor) {
        List<Message> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int createTime = cursor.getInt(cursor.getColumnIndex("createTime"));
                String talker = cursor.getString(cursor.getColumnIndex("talker"));
                String s = cursor.getString(cursor.getColumnIndex("content"));
                String id = s.split(":\n")[0];
                String content = s.split(":\n")[1];
                Message m = new Message(createTime, talker,id, content);
                //ignore duplicated messages
                if (last != null && last.createTime == m.createTime){
                    continue;
                }
                last = m;
                result.add(m);
            } while (cursor.moveToNext());
        }
        return result;
    }

    private static String parseCursor(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        String[] columNames = cursor.getColumnNames();
        if (columNames != null && columNames.length > 0) {
            for (String columName : columNames) {
                sb.append(columName + "\t");
            }
            sb.append("\n");
        }
        if (cursor.moveToFirst()) {
            do {
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_NULL:
                            sb.append("NULL");
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            sb.append(cursor.getInt(i));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            sb.append(cursor.getFloat(i));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            sb.append(cursor.getString(i));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            sb.append(cursor.getBlob(i));
                            break;
                    }
                    sb.append("\t");
                }
                sb.append("\n");
            } while (cursor.moveToNext());
        }
        // resotre to first
        cursor.moveToFirst();
        return sb.toString();
    }

}
