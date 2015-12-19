package com.vicxiao.weixinhacker.listener;

import com.vicxiao.weixinhacker.message.AudioMessage;

/**
 * Created by vic on 15-12-19.
 */
public interface AudioMessageListener {

    void onNewMessage(AudioMessage message);
}
