package com.chat.dao;

import com.chat.Main;
import com.chat.handler.UserHandler;
import com.chat.model.UserDto;
import com.chat.utils.BizCheckUtils;
import com.chat.utils.GsonUtils;
import com.chat.verticle.RedisVerticle;
import com.google.gson.JsonObject;
import io.vertx.core.eventbus.EventBus;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserRedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private final RedisAPI redisAPI;

    private final String UserIdKey = "UserIdKey";

    public UserRedisDao(RedisAPI redisAPI) {
        this.redisAPI = redisAPI;
        this.baseOperate();
    }

    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        //查询单个用户id
        bus.<Integer>consumer(UserHandler.REDIS_USER_ID_INIT).handler(msg ->{
            //默认增加 1,其实可以先申请若干个,缓存起来减少申请次数,暂时不做修改
            redisAPI.incrby(UserIdKey, "1", res -> {
                try {
                    if (res.succeeded() && res.result() != null && res.result().type() == ResponseType.NUMBER) {
                        Integer userId = res.result().toInteger();
                        logger.info("查询用户id完成,userid={} ", userId );
                        msg.reply(userId);
                    } else {
                        logger.info("查询用户id失败 ");
                        msg.reply(-1);
                    }
                } catch (Exception e) {
                    msg.fail(400, e.getMessage());
                }
            });
        });

        //查询单个用户
        bus.<String>consumer(UserHandler.REDIS_USER_QUERY).handler(msg ->{
            String username = msg.body();
            redisAPI.hgetall(username, res -> {
                try {
                    if (res.succeeded() && res.result() != null && res.result().type() == ResponseType.MULTI) {
                        List<String> values = GsonUtils.jsonToList(res.result().toString(),String.class);
                        JsonObject jsonObject = new JsonObject();
                        for(int i=0; i < values.size(); ){
                            jsonObject.addProperty(values.get(i++),values.get(i++));
                        }

                        logger.info("查询用户信息完成,value={} ", jsonObject);
                        UserDto userDto = GsonUtils.jsonObjectToBean(jsonObject,UserDto.class);
                        if(userDto.getUsername() != null){
                            msg.reply(userDto);
                            return;
                        }
                    }
                    msg.reply(null);
                } catch (Exception e) {
                    msg.fail(400, e.getMessage());
                }
            });
        });

        //保存用户 map类型 key(username) value(UserDto各字段)
        bus.<UserDto>consumer(UserHandler.REDIS_USER_CREATE).handler(msg ->{
            UserDto userDto = msg.body();

            List<String> args = Stream.of(userDto.getUsername()).collect(Collectors.toList());
            Map<String,String> keyValues = GsonUtils.<UserDto>bean2Map(userDto);
            if(keyValues == null){
                logger.error("用户信息错误 " + userDto.getUsername());
                msg.fail(400,"message error");
                return;
            }
            keyValues.forEach( (key,value) ->{
                args.add(key);
                args.add(value);
            });
            redisAPI.hset(args, res -> {
                try {
                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.NUMBER) {
                        logger.info("保存用户信息完成 " + userDto.getUsername());
                        msg.reply(true);
                    } else {
                        logger.info("保存用户信息失败 " + userDto.getUsername());
                        msg.reply(false);
                    }
                } catch (Exception e) {
                    msg.fail(400, e.getMessage());
                }
            });
        });

        //更新用户房间信息
        bus.<UserDto>consumer(UserHandler.REDIS_USER_ROOM_ID_UPDATE).handler(msg ->{
            UserDto userDto = msg.body();

            List<String> args = Stream.of(userDto.getUsername(), "roomId",String.valueOf(userDto.getRoomId())).collect(Collectors.toList());
            redisAPI.hset(args, res -> {
                try {
                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.NUMBER) {
                        logger.info("更新用户房间id完成 " + userDto.getUsername());
                        msg.reply(true);
                    } else {
                        logger.info("更新用户房间id失败 " + userDto.getUsername());
                        msg.reply(false);
                    }
                } catch (Exception e) {
                    msg.fail(400, e.getMessage());
                }
            });
        });

    }


}
