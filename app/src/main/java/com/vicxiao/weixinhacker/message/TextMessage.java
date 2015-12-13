package com.vicxiao.weixinhacker.message;

/**
 * Created by vic on 15-12-13.
 */
public class TextMessage extends Message{

    public static final char AT_SEPARATOR = (char)8197;
    public static final String AT_FORMATE = "@%s \n";

    public TextMessage(long createTime, String talker, String id, String content) {
        super(createTime, talker, id, content);
    }

    public static String at(String displayName){
        return String.format(AT_FORMATE, displayName);
    }
}
