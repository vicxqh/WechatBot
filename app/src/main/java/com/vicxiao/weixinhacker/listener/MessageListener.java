package com.vicxiao.weixinhacker.listener;

import com.vicxiao.weixinhacker.message.Message;

/**
 * Created by vic on 15-12-3.
 */
public interface MessageListener {
    void onNewMessage(Message message);
}
