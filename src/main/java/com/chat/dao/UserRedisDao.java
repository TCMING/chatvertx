package com.chat.dao;

import com.chat.Main;
import com.chat.model.UserDto;
import com.chat.utils.GsonUtils;
import com.chat.utils.RedisClientUtil;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.redis.client.Response;
import io.vertx.redis.client.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserRedisDao {

    Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private String userListKey = "userList";

    private String userMapKey = "UserMapKey";

    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        //查询单个用户
        bus.<String>consumer(RedisVerticle.REDIS_USER_SINGLE_QUERY).handler(msg ->{
            String username = msg.body();
            RedisClientUtil.getRedisAPI().hget(userListKey,username,res ->{
                if(res.succeeded() && res.result().type() == ResponseType.SIMPLE){
                    logger.info("查询用户信息完成,username={} ",username);
                    String value = res.result().toString();
                    msg.reply(GsonUtils.jsonToBean(value,UserDto.class));
                }else{
                    logger.info("保存用户信息失败 ");
                    msg.reply(false);
                }
            });
        });

        //保存用户
        bus.<UserDto>consumer(RedisVerticle.REDIS_USER_CREATE).handler(msg ->{
            UserDto userDto = msg.body();
            List<String> args = Stream.of(userMapKey, userDto.getUsername(),GsonUtils.toJsonString(userDto)).collect(Collectors.toList());
            RedisClientUtil.getRedisAPI().hset(args, res ->{
               if(res.succeeded() && res.result().type() == ResponseType.NUMBER){
                   logger.info("保存用户信息完成 " + userDto.getUsername());
                   msg.reply(true);
               }else{
                   logger.info("保存用户信息失败 " + userDto.getUsername());
                   msg.reply(false);
               }
            });
        });

    }


}
