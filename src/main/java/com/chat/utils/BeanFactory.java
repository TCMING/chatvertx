package com.chat.utils;

import com.chat.dao.UserRedisDao;
import com.chat.repository.MessageRepository;
import com.chat.repository.RoomRepository;
import com.chat.service.MessageService;
import com.chat.service.RoomService;
import com.chat.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class BeanFactory {

    private static final Map<Class,Object> instances = new HashMap<>();

    public static void init(){

//        UserRedisDao userRedisDao = new UserRedisDao();

        MessageRepository messageRepository = new MessageRepository();
        RoomRepository roomRepository = new RoomRepository();


        instances.put(UserRedisDao.class,null);
        instances.put(MessageRepository.class,messageRepository);
        instances.put(RoomRepository.class,roomRepository);
    }

    public static <T> T getInstance(Class<T> clazz){
        return (T) instances.get(clazz);
    }

}
