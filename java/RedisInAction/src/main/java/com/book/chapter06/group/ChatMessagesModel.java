package com.book.chapter06.group;

import java.util.List;
import java.util.Map;

public class ChatMessagesModel {

    public String chatId;
    public List<MessageModel> messages;

    public ChatMessagesModel(String chatId, List<MessageModel> messages){
        this.chatId = chatId;
        this.messages = messages;
    }

    public boolean equals(Object other){
        if (!(other instanceof ChatMessagesModel)){
            return false;
        }
        ChatMessagesModel otherCm = (ChatMessagesModel)other;
        return chatId.equals(otherCm.chatId) &&
                messages.equals(otherCm.messages);
    }
}
