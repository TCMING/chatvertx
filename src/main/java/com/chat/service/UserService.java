package com.chat.service;

import com.chat.Main;
import com.chat.model.UserDto;
import com.chat.model.UserRequest;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicReference;


/**
 * @Author tianchengming
 * @Date 2021年7月3日 20:14
 * @Version 1.0
 */
public class UserService {



//    public UserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    public UserDto queryUserByName(String userName){
//        UserDto userDto = userRepository.queryUser(userName);
//        return userDto;
//    }
//
//    public boolean userPasswordCheck(String username, String password){
//        UserDto userDto = userRepository.queryUser(username);
//        if(userDto != null && StringUtils.equals(password,userDto.getPassword())){
//            return true;
//        }
//        return false;
//    }


}
