package com.vkzwbim.chat.bean;

import com.vkzwbim.chat.bean.message.ChatMessage;

public class EventTransfer {
    private ChatMessage chatMessage;

    public EventTransfer(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }
}
