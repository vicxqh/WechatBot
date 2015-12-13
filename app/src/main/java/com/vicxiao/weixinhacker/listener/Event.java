package com.vicxiao.weixinhacker.listener;

/**
 * Created by vic on 15-12-13.
 */
public class Event {

    public final Type type;
    public final String chatRoom;
    public final String recommender;
    public final String displayName;

    private Event(Type type, String chatRoom, String displayName, String recommender) {
        this.type = type;
        this.chatRoom = chatRoom;
        this.displayName = displayName;
        this.recommender = recommender;
    }

    public static class Builder{
        private Type type;
        private String chatRoom;
        private String displayName;
        private String recommender;

        public void setType(Type type) {
            this.type = type;
        }

        public void setChatRoom(String chatRoom) {
            this.chatRoom = chatRoom;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public void setRecommender(String recommender) {
            this.recommender = recommender;
        }

        public Event build(){
            return new Event(type, chatRoom, displayName, recommender);
        }
    }

    public enum Type{
        NEW_MEMBER
    }
}
