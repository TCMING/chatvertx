package com.chat.repository;

import com.chat.model.MessageDto;
import com.chat.model.MessageRetrive;

import java.util.HashMap;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author tianchengming
 * @Date 2021年7月3日 17:45
 * @Version 1.0
 */
public class MessageRepository {

    private ArrayBlockingQueue  blockingQueue = new ArrayBlockingQueue<MessageDto>(100);

    private ConcurrentHashMap<String,Integer> addedMap = new ConcurrentHashMap<>();

    public boolean saveMessage(MessageDto messageDto){
        boolean isRuning = true;
        try {
            blockingQueue.put(messageDto);
            while (isRuning){
                if(addedMap.containsKey(messageDto.getId())){
                    isRuning = false;
                    addedMap.remove(messageDto.getId());
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean queryMessage(String id){
//        messageRedisDao.queryMessage(id);
        return false;
    }

    public List<MessageRetrive> queryMessages(int roomId, int pageIndex, int pageSize) {
        HashMap<String, Integer> params = new HashMap<>();
        params.put("roomId", roomId);
        params.put("startIndex", (-1 - pageIndex) * pageSize);
        params.put("pageSize", pageSize);
//        messageRedisDao.queryAllMessage(params);
        return null;
    }

}
