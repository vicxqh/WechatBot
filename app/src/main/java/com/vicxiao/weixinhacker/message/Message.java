package com.vicxiao.weixinhacker.message;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by vic on 15-12-3.
 */
public class Message {
    private long createTime;
    private String talker;
    private String id; // null means send by myself or event
    private String content;

    public String getContent() {
        return content;
    }

    public String getTalker() {
        return talker;
    }

    public String getId() {
        return id;
    }

    public long getCreateTime() {
        return createTime;
    }


    protected Message(long createTime, String talker, String id, String content) {
        this.createTime = createTime;
        this.talker = talker;
        this.id = id;
        this.content = content;
    }

    @Override
    public String toString(){
        return "CreateTime["+this.createTime+"]" + "\t talker["+ this.talker +"]" + "\tID["+this.id +"]"+"\tContent["+this.content+"]\n";
    }

    private static Message last = null;
    public static long lastSend = -1;

    public static List<TextMessage> getTextMessage(Cursor cursor) {
        List<TextMessage> result = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                long createTime = cursor.getLong(cursor.getColumnIndex("createTime"));
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

                TextMessage m = new TextMessage(createTime, talker,id, content);
                //ignore duplicated messages
                if (last != null && last.createTime >= m.getCreateTime()){
                    continue;
                }
                last = m;
                result.add(m);
            } while (cursor.moveToNext());
        }
        return result;
    }

    public static Type getType(Cursor cursor){
        Type type = Type.UNKNOWN;
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex("type");
            if (index != -1){
                int typeInt = cursor.getInt(index);
                switch (typeInt){
                    case 1:
                        type = Type.TEXT_MESSAGE;
                        break;
                    case 34:
                        type = Type.AUDIO_MESSAGE;
                        break;
                    case 10000:
                        type = Type.EVENT;
                        break;
                }
            }
        }
        return type;
    }

    public static int getStatus(Cursor cursor){
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex("status");
            return cursor.getInt(index);
        }
        return -1;
    }

    public enum Type {
        TEXT_MESSAGE, AUDIO_MESSAGE, EVENT, UNKNOWN
    }
}