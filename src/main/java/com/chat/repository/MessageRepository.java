package com.chat.repository;

import com.chat.model.MessageDto;
import com.chat.model.MessageRetrive;

import java.util.List;

public class MessageRepository {
    public boolean queryMessage(String id) {
        return true;
    }

    public boolean saveMessage(MessageDto messageDto) {
        return true;
    }

    public List<MessageRetrive> queryMessages(int roomId, int pageIndex, int pageSize) {
        return null;
    }
}
