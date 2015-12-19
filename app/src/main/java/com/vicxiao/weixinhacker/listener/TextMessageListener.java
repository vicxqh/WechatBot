package com.vicxiao.weixinhacker.listener;

import com.vicxiao.weixinhacker.message.TextMessage;

/**
 * Created by vic on 15-12-3.
 */
public interface TextMessageListener {
    void onNewMessage(TextMessage message);
}
