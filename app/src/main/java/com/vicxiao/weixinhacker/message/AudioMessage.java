package com.vicxiao.weixinhacker.message;

/**
 * Created by vic on 15-12-19.
 */
public class AudioMessage extends Message {
    public final int status;

    public AudioMessage(long createTime, String talker, String id, String content, int status) {
        super(createTime, talker, id, content);
        this.status = status;
    }

}
