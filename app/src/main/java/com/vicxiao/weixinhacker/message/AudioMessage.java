package com.vicxiao.weixinhacker.message;

/**
 * Created by vic on 15-12-19.
 */
public class AudioMessage extends Message {
    protected AudioMessage(long createTime, String talker, String id, String content) {
        super(createTime, talker, id, content);
    }
}
