package com.vicxiao.weixinhacker;

import com.vicxiao.weixinhacker.listener.AudioMessageListener;
import com.vicxiao.weixinhacker.listener.Listeners;
import com.vicxiao.weixinhacker.message.AudioMessage;
import com.vicxiao.weixinhacker.query.Group;
import com.vicxiao.weixinhacker.sender.Senders;

import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by vic on 15-12-19.
 */
public class TestAudio implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.tencent.mm")) {
            return;
        }

        LoadPackageHandler.initQuery(loadPackageParam);
        // If you want to send message, you must also call loadMessageListener.
        LoadPackageHandler.loadMessageListener(loadPackageParam);
        LoadPackageHandler.loadSenders(loadPackageParam);
        LoadPackageHandler.testAudioSender(loadPackageParam);
//        LoadPackageHandler.logCursor(loadPackageParam);

        Listeners.addAudioMessageListener(new AudioMessageListener() {
            Map<String, Map<String, String>> groupMap = new HashMap<String, Map<String, String>>();
            @Override
            public void onNewMessage(AudioMessage message) {
//                if (message.getTalker().contains("658998013")) {
                if (message.getTalker().contains("@")) {
                    String groupId = message.getTalker();
                    if (!groupMap.containsKey(groupId)){
                        groupMap.put(groupId, Group.getDisplayNameMap(groupId));
                    }
                    Map<String, String> displayMap = groupMap.get(groupId);
                    String nickName = displayMap.get(message.getId());
                    Senders.sendText(message.getTalker(), "转发" + nickName + "的语音");
                    Senders.sendAudio(message.getTalker(), message.getContent());
                    // Can not send multiple audio message in this block.


//                }
                }
            }
        });
    }
}
