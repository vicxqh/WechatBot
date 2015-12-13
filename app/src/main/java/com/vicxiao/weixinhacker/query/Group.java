package com.vicxiao.weixinhacker.query;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;

/**
 * Created by vic on 15-12-13.
 */
public class Group {
    public static void describeGroup(){
        Cursor cursor = Query.query("select * from chatroom;");
        XposedBridge.log(Query.parseCursor(cursor));
    }

    /**
     * Warning: SQL-injection possibilities.
     * @param roomId
     * @param id
     * @return
     */
    public static String getDisplayName(String roomId, String id){
        Map<String, String> map = getDisplayNameMap(roomId);
        return map.get(id);
    }

    /**
     * Warning: SQL-injection possibilities.
     * @param roomId
     * @return
     */
    public static Map<String, String> getDisplayNameMap(String roomId){
        Map<String, String> map = new HashMap();
        Cursor cursor = Query.query("select memberlist, displayname from chatroom where chatroomname=\"" + roomId + "\";");
        if (cursor != null && cursor.moveToFirst()){
            int index = cursor.getColumnIndex("memberlist");
            String idString = cursor.getString(index);
            String[] ids = idString.split(";");
            index = cursor.getColumnIndex("displayname");
            String nameString = cursor.getString(index);
            String[] names = nameString.split("、");// Attention: the separator is  、 not ;
            for (int i = 0; i < ids.length; i++){
                map.put(ids[i], names[i]);
            }
        }
        return map;
    }


    public static List<String> getRoomList(){
        List<String> list = new ArrayList<>();
        Cursor cursor = Query.query("select chatroomname from chatroom");
        if (cursor != null && cursor.moveToFirst()){
            int index = cursor.getColumnIndex("chatroomname");
            if (index != -1) {
                do {
                    String roomId = cursor.getString(index);
                    list.add(roomId);
                } while (cursor.moveToNext());
            }
        }
        return list;
    }

    /**
     * Warning: SQL-injection possibilities.
     *
     * @param roomId
     * @return
     */
    public static List<String> getMemberListId(String roomId){
        List<String> list = new ArrayList<>();
        Cursor cursor = Query.query("select memberlist from chatroom where chatroomname=\"" + roomId +"\";");
        if (cursor != null && cursor.moveToFirst()){
            int index = cursor.getColumnIndex("memberlist");
            if (index != -1) {
                String listString = cursor.getString(index);
                for (String member : listString.split(";")) {
                    list.add(member);
                }
            }
        }
        return list;
    }

    /**
     * Warning: SQL-injection possibilities.
     *
     * @param roomId
     * @return
     */
    public static List<String> getMemberListName(String roomId){
        List<String> list = new ArrayList<>();
        Cursor cursor = Query.query("select displayname from chatroom where chatroomname=\"" + roomId +"\";");
        if (cursor != null && cursor.moveToFirst()){
            int index = cursor.getColumnIndex("displayname");
            if (index != -1) {
                String listString = cursor.getString(index);
                for (String member : listString.split("、")) {
                    list.add(member);
                }
            }
        }
        return list;
    }
}
