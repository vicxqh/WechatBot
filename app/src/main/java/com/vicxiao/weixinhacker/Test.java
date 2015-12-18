package com.vicxiao.weixinhacker;

import com.vicxiao.weixinhacker.listener.Event;
import com.vicxiao.weixinhacker.listener.EventListener;
import com.vicxiao.weixinhacker.listener.Listeners;
import com.vicxiao.weixinhacker.listener.TextMessageListener;
import com.vicxiao.weixinhacker.message.Message;
import com.vicxiao.weixinhacker.message.TextMessage;
import com.vicxiao.weixinhacker.query.Group;
import com.vicxiao.weixinhacker.sender.Senders;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by xw on 2015/11/14.
 */
public class Test implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.tencent.mm")) {
            return;
        }
        LoadPackageHandler.initQuery(loadPackageParam);
        // If you want to send message, you must also call loadMessageListener.
        LoadPackageHandler.loadMessageListener(loadPackageParam);
        LoadPackageHandler.loadTextSender(loadPackageParam);
        Senders.start();

        Listeners.addTextMessageListener(new TextMessageListener() {
            @Override
            public void onNewMessage(Message message) {

                if (message.getContent().startsWith("@ALL") && message.getTalker() != null && message.getTalker().indexOf('@') > 0) {

                    List<String> members = Group.getMemberListName(message.getTalker());
                    XposedBridge.log(dumpList(members));
                    String addAll = "";
                    for (String member : members) {
                        if (!member.equals("大叔") && !member.equals(message.getId())) {
                            addAll += String.format(TextMessage.AT_FORMATE, member);
                        }
                    }
                    Senders.sendText(message.getTalker(), addAll + message.getContent().substring(5));
                } else if (message.getTalker().indexOf('@') > 0){
                    if (message.getContent().startsWith("@大叔")){
                        String command = message.getContent().substring(4);
                        String displayName = Group.getDisplayName(message.getTalker(), message.getId());
                        if ("List id".equalsIgnoreCase(command)){
                            Senders.sendText(message.getTalker(), String.format(TextMessage.AT_FORMATE, displayName) + dumpList(Group.getMemberListId(message.getTalker())));
                        } else if( "List name".equalsIgnoreCase(command)){
                            Senders.sendText(message.getTalker(), String.format(TextMessage.AT_FORMATE, displayName) + dumpList(Group.getMemberListName(message.getTalker())));
                        } else {
                            Senders.sendText(message.getTalker(), String.format(TextMessage.AT_FORMATE, displayName) + "Unknown command");
                        }
                    }
                }
            }
        });

        Listeners.addEventListener(new EventListener() {
            @Override
            public void onNewMember(Event event) {
                Senders.sendText(event.chatRoom, TextMessage.at(event.displayName) + "进群请修改群名片");
            }
        });
    }

    private static String dumpList(List<String> list){
        if (list == null || list.size() == 0){
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String s : list) {
            sb.append(s);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }
}
