package com.chat.dao;

import com.chat.model.UserDto;
import com.chat.utils.GsonUtils;
import com.chat.utils.RedisClientUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.impl.JsonUtil;

import java.util.ArrayList;
import java.util.List;

public class UserRedisDao {

    private String userListKey = "userList";

    public void createUser(UserDto user) {
        List<String> params = new ArrayList<String>();
        params.add(userListKey);
        params.add(GsonUtils.toJsonString(user));
        RedisClientUtil.initRedisAPI().lpush(params);
    }

    public List<UserDto> queryAll() {
        RedisClientUtil.initRedisAPI().lrange(userListKey,"0" , "-1" , res->{
            if(res.succeeded()){
                return ;
            }else{
                return;
            }
        });
//        GsonUtils.jsonToList(res.result().toString() , UserDto.class);
        return null;
    }

}

