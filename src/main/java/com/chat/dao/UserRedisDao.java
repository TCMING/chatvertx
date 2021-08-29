package com.chat.dao;

import com.chat.model.UserDto;
import com.chat.utils.RedisClientUtil;
import io.vertx.redis.client.Command;
import io.vertx.redis.client.Request;

public class UserRedisDao {

    public void createUser(UserDto user) {
        RedisClientUtil.redisClient
                .connect()
                .onSuccess(conn -> {
                    conn.send(Request.cmd(Command.SET).arg("test").arg("7"))
                            .onSuccess(info -> {
                                // do something...
                                System.out.println("-----set successed");
                            });
                });

    }

}

