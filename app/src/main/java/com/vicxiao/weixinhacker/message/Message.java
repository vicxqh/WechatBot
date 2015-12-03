package com.vicxiao.weixinhacker.message;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vic on 15-12-3.
 */
public class Message {
    private int createTime;
    private String talker;
    private String id; // null means send by myself
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

    public int getCreateTime() {
        return createTime;
    }


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

    private static Message last = null;
    public static int lastSend = -1;

    public static List<Message> getMessage(Cursor cursor) {
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
}