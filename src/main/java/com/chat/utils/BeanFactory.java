package com.chat.utils;

import com.chat.dao.UserRedisDao;
import com.chat.repository.MessageRepository;
import com.chat.repository.RoomRepository;
import com.chat.repository.UserRepository;
import com.chat.service.MessageService;
import com.chat.service.RoomService;
import com.chat.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class BeanFactory {

    private static final Map<Class,Object> instances = new HashMap<>();

    public static void init(){

        UserRedisDao userRedisDao = new UserRedisDao();

        MessageRepository messageRepository = new MessageRepository();
        RoomRepository roomRepository = new RoomRepository();
        UserRepository userRepository = new UserRepository();

        UserService userService = new UserService(userRepository);
        RoomService roomService = new RoomService(roomRepository, userRepository);
        MessageService messageService = new MessageService(messageRepository, userRepository);

        instances.put(UserRedisDao.class,userRedisDao);
        instances.put(MessageRepository.class,messageRepository);
        instances.put(RoomRepository.class,roomRepository);
        instances.put(UserRepository.class,userRepository);
        instances.put(UserService.class,userService);
        instances.put(RoomService.class,roomService);
        instances.put(MessageService.class,messageService);
    }

    public static <T> T getInstance(Class<T> clazz){
        return (T) instances.get(clazz);
    }

}
