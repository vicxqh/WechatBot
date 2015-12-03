package com.vicxiao.weixinhacker.sender;

/**
 * Created by xw on 2015/11/19.
 */
public interface ISender <T> {
    void send(String talker, T content);
}
