package com.vicxiao.weixinhacker;

import android.database.Cursor;

import com.vicxiao.weixinhacker.sender.Senders;
import com.vicxiao.weixinhacker.sender.TextSender;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
    boolean submitted = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.tencent.mm")) {
//            XposedBridge.log("WechatBot loading...!");
            loadTrigger(loadPackageParam);
//            XposedBridge.log("WechatBot loaded!");
            loadTextSender(loadPackageParam);

        }
    }

    static int lastSend = -1;
    private void loadTrigger(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Cursor result = (Cursor) param.getResult();
                if (result != null && param.args[0].toString().startsWith("select * from message")) {
//                    XposedBridge.log("++++++++++++++++++++++++++");
//                    XposedBridge.log(parseCursor(result));
//                    XposedBridge.log("++++++++++++++++++++++++++");
                    List<Message> messages = getMessage(result);
                    for (Message message : messages) {
                        if (message == null){
                            continue;
                        }
                        XposedBridge.log(message.toString());
                        if (message.createTime >lastSend){
                            lastSend = message.createTime;
                            if (Senders.doOneJob()){
                                break;
                            } else if (message.content.startsWith("@@")){
                                XposedBridge.log("@@");
                                Senders.sendTextToAll("#######\n" + message.id + "\n" + message.content.substring(2));
                                Senders.doOneJob();
                            }
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

    static class Message {
        int createTime;
        String talker;
        String id; // null means send by myself
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
                String[] ss = s.split(":\n");
                String id = null;
                String content;
                if (ss.length != 2){
                    content = s;
                } else {
                    id = ss[0];
                    content = ss[1];
                }

                Message m = new Message(createTime, talker,id, content);
                //ignore duplicated messages
                if (last != null && last.createTime >= m.createTime){
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
