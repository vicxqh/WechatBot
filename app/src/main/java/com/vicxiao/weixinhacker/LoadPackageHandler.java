package com.vicxiao.weixinhacker;

import android.database.Cursor;

import com.vicxiao.weixinhacker.listener.Event;
import com.vicxiao.weixinhacker.listener.Listeners;
import com.vicxiao.weixinhacker.message.AudioMessage;
import com.vicxiao.weixinhacker.message.Message;
import com.vicxiao.weixinhacker.message.TextMessage;
import com.vicxiao.weixinhacker.query.Query;
import com.vicxiao.weixinhacker.sender.AudioSender;
import com.vicxiao.weixinhacker.sender.Senders;
import com.vicxiao.weixinhacker.sender.TextSender;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by vic on 15-11-29.
 */
public class LoadPackageHandler {

    public static class AudioHooker extends XC_MethodHook{
        public volatile boolean isSending = false;

        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            synchronized (this){
                if (Senders.sending != null && Senders.sending instanceof Senders.AudioIntent && !isSending ){
                    XposedBridge.log("Try send in rawQuery");
                    isSending = true;
                    Senders.audioSender.send(Senders.sending.getTalker(), ((Senders.AudioIntent) Senders.sending).getContent());
                    XposedBridge.log("Try send in rawQuery 2");
                }
            }
        }
    }

    public final static AudioHooker audioHooker = new AudioHooker();

    public static void loadSenders(final XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.tencent.mm.ui.chatting.ChattingUI$a", loadPackageParam.classLoader, "EI", java.lang.String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                String content = (String) param.args[0];
                if (content.equalsIgnoreCase("Start") && Senders.textSender == null){
                    Method method = (Method) param.method;
                    Object receiver = param.thisObject;
                    Senders.textSender = new TextSender(receiver, method);
                    Senders.audioSender = new AudioSender(loadPackageParam);
                    Senders.start();
                }
            }

        });
    }

    public static void initQuery(XC_LoadPackage.LoadPackageParam loadPackageParam){
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (Query.method == null){
                    Query.method  = (Method) param.method;
                    Query.receiver = param.thisObject;
                }
            }
        });
    }

    public static void logCursor(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (Query.method == null) {
                    Query.method = (Method) param.method;
                    Query.receiver = param.thisObject;
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Cursor result = (Cursor) param.getResult();
                //Log args
                StringBuilder argString = new StringBuilder();
                for (Object arg : param.args) {
                    if (arg == null) {
                        argString.append("NULL" + "~~~");
                    } else {
                        argString.append(arg.toString() + "~~~");
                    }
                }

                XposedBridge.log(argString.substring(0, argString.length() - 3));
                StringBuilder cursor = new StringBuilder();
                if (result == null) {
                    cursor.append("NULL");
                } else {
                    cursor.append(Query.parseCursor(result));
                }
                XposedBridge.log(cursor.toString());

                XposedBridge.log("---------------------------------------------");
            }
        });
    }

    public static void testAudioSender(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //this is used for text retransmission

        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, audioHooker);
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Cursor result = (Cursor) param.getResult();
                if (result != null && param.args[0].toString().contains("FROM voiceinfo")){
                    XposedBridge.log(param.args[0].toString());
                    XposedBridge.log(Query.parseCursor(result));

                }
                if (result != null && param.args[0].toString().equals("SELECT FileName, User, MsgId, NetOffset, FileNowSize, TotalLen, Status, CreateTime, LastModifyTime, ClientId, VoiceLength, MsgLocalId, Human, reserved1, reserved2, MsgSource FROM voiceinfo WHERE FileName= ?")
                        && Senders.sending != null && Senders.sending instanceof Senders.AudioIntent){
                    Senders.AudioIntent intent = (Senders.AudioIntent) Senders.sending;
                    if (result.moveToFirst()){
                        String fileName = result.getString(result.getColumnIndex("FileName"));
                        String talker = result.getString(result.getColumnIndex("User"));
                        int reserved1 = result.getInt(result.getColumnIndex("reserved1"));
                        int reserved2 = result.getInt(result.getColumnIndex("reserved2"));
                        if (intent.getContent().equals(fileName) && intent.getTalker().equals(talker)){
                            // Might need a better lock
                                XposedBridge.log("Audio signal all");
                                Senders.sending = null;
                                audioHooker.isSending = false;
                        }
                    }

                }
            }
        });



        XposedHelpers.findAndHookMethod("com.tencent.mm.modelvoice.u", loadPackageParam.classLoader, "jH", String.class, new XC_MethodHook() {
            String sendFile = "";

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Object v = param.args[0];
                String fileName = v.toString();
                Object queryResult = param.getResult();
                if (queryResult != null && !sendFile.equals(fileName)) {
                    sendFile = fileName;
                    int result = XposedHelpers.getIntField(queryResult, "status");
                    XposedBridge.log("status: " + result);
                    XposedHelpers.setIntField(queryResult, "bWK", 0);//read offset
                    XposedHelpers.setIntField(queryResult, "status", 3);// status must be 3 for sending
                    XposedHelpers.setObjectField(queryResult, "aBT", AudioSender.getTalkerName());
                }
                if (queryResult != null && sendFile.equals(fileName)) {
                    int result = XposedHelpers.getIntField(queryResult, "status");
                    XposedHelpers.setIntField(queryResult, "status", 3);
                }
            }
        });
    }

    public static void loadMessageListener(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        findAndHookMethod("com.tencent.mm.aw.g", loadPackageParam.classLoader, "rawQuery", String.class, String[].class, new XC_MethodHook() {

            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Cursor result = (Cursor) param.getResult();
                if (result != null && param.args[0].toString().startsWith("select * from message")) {
                    //XposedBridge.log(Query.parseCursor(result));
                    Message.Type type = Message.getType(result);
                    int status = Message.getStatus(result);
                    switch (type){
                        case TEXT_MESSAGE:
                            List<TextMessage> textMessages = Message.getTextMessage(result);
                            for (TextMessage message : textMessages) {
                                if (message == null){
                                    continue;
                                }
                                if (message.getCreateTime() >Message.lastSend){
                                    Message.lastSend = message.getCreateTime();
                                    if (Senders.sending != null){
                                        if (Senders.sending.getTalker().equals(message.getTalker()) && Senders.sending.getContent().equals(message.getContent())){
                                            Senders.sending = null;
                                            XposedBridge.log("Signal all");
                                        }
                                    }
                                    Listeners.handleNewTextMessage(message);
                                }
                            }
                            break;
                        case EVENT:
                            textMessages = Message.getTextMessage(result);
                            for (Message message : textMessages) {
                                if (message == null) {
                                    continue;
                                }
                                if (message.getCreateTime() >Message.lastSend) {
                                    Message.lastSend = message.getCreateTime();
                                    Pattern inviteMemberPattern = Pattern.compile("(.*)邀请(.*)加入了群聊");
                                    Matcher matcher = inviteMemberPattern.matcher(message.getContent());
                                    if (matcher.find()){
                                        XposedBridge.log(matcher.group(1) + "[invite]" + matcher.group(2));
                                        Event.Builder builder = new Event.Builder();
                                        builder.setChatRoom(message.getTalker());
                                        builder.setRecommender(matcher.group(1));
                                        builder.setDisplayName(matcher.group(2));
                                        builder.setType(Event.Type.NEW_MEMBER);
                                        Listeners.handleNewEvent(builder.build());
                                    }
                                }
                            }

                            break;
                        case AUDIO_MESSAGE:
                            if (status == 2 || status == 3){
                                List<AudioMessage> audioMessages = Message.getAudioMessage(result);
                                for (AudioMessage audioMessage : audioMessages) {
                                    if (audioMessage == null) {
                                        continue;
                                    }
                                    if (audioMessage.getCreateTime() >Message.lastSend){
                                        Message.lastSend = audioMessage.getCreateTime();
                                        if (Senders.sending != null && audioMessage.status == 2){
                                            Senders.sending = null;
                                            XposedBridge.log("AudioMessage Signal all");
                                        }
                                        if (audioMessage.status == 3){
                                            Listeners.handleNewAudioMessage(audioMessage);
                                        }
                                    }
                                }
                            }
                            break;
                        case UNKNOWN:
                            XposedBridge.log("Unknown message type:");
                            XposedBridge.log(Query.parseCursor(result));
                            break;
                    }
                }

            }
        });
    }

}
