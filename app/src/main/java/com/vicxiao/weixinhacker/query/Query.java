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

    public static Cursor query(String queryString){
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

    /**
     *
     * @param cursor
     * @return The markdown representation of the table represented by this cursor.
     */
    public static String parseCursor(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        String[] columNames = cursor.getColumnNames();
        if (columNames != null && columNames.length > 0) {
            sb.append("|");
            for (String columName : columNames) {
                sb.append(columName + "|");
            }
            sb.append("\n");
        }
        if (cursor.moveToFirst()) {
            sb.append("|");
            for (int i = 0; i < columNames.length; i++){
                sb.append("------|");
            }
            sb.append("\n");
            do {
                sb.append("|");
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_NULL:
                            sb.append("NULL");
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            sb.append(cursor.getLong(i));
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            sb.append(cursor.getDouble(i));
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            sb.append(cursor.getString(i));
                            break;
                        case Cursor.FIELD_TYPE_BLOB:
                            sb.append(cursor.getBlob(i));
                            break;
                    }
                    sb.append("|");
                }
                sb.append("\n");
            } while (cursor.moveToNext());
        }
        // resotre to first
        cursor.moveToFirst();
        return sb.toString();
    }
}
