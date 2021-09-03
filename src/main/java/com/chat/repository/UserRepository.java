package com.chat.repository;

import com.chat.Main;
import com.chat.model.UserDto;
import com.chat.model.UserDtoCodec;
import com.chat.verticle.RedisVerticle;
import com.google.gson.JsonObject;
import io.vertx.core.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author tianchengming
 * @Date 2021年7月3日 17:45
 * @Version 1.0
 */
public class UserRepository {


    public UserDto queryUser(String userName){
        AtomicReference<UserDto> userDtoRef = new AtomicReference<>();
        EventBus bus = Main.vertx.eventBus();
        bus.registerCodec(UserDtoCodec.create());
        bus.<UserDto>request(RedisVerticle.REDIS_USER_SINGLE_QUERY,userName,reply ->{
            if(reply.succeeded()){
                UserDto userDto = reply.result().body();
                userDtoRef.set(userDto);
            }
        });
        return userDtoRef.get();
    }

    public boolean saveUser(UserDto userDto){
        if(queryUser(userDto.getUsername()) != null){
            return true;
        }
        EventBus bus = Main.vertx.eventBus();
        AtomicBoolean result = new AtomicBoolean(false);
        bus.<Boolean>request(RedisVerticle.REDIS_USER_CREATE,userDto,reply ->{
            if(reply.succeeded()){
                result.set(reply.result().body());
            }else{
                result.set(false);
            }
        });
        return result.get();
    }

}
