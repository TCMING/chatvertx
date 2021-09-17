package com.chat.dao;

import com.chat.Main;
import com.chat.handler.UserHandler;
import com.chat.model.UserDto;
import com.chat.utils.BizCheckUtils;
import com.chat.utils.GsonUtils;
import com.chat.utils.RedisClientUtil;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserRedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private RedisAPI redisAPI;

    private final String UserIdKey = "UserIdKey";

    @Deprecated
    public UserRedisDao(RedisAPI redisAPI) {
        this.redisAPI = redisAPI;
        this.baseOperate();
    }

    public UserRedisDao() {
        this.baseOperate();
    }

    private void hGetAll(Message message, String username){
        RedisClientUtil.getRedisAPI().hgetall(username, res -> {
            try {
                if(res.cause() != null){
                    hGetAll(message,username);
                    return;
                }
                if (res.succeeded() && res.result() != null && res.result().size() > 0 && res.result().type() == ResponseType.MULTI) {
                    UserDto userDto = GsonUtils.jsonToBean(res.result().toString(), UserDto.class);
                    if (userDto.getUsername() != null) {
                        message.reply(userDto);
                        return;
                    }
                }
            } catch (Exception e){
                logger.error("用户查询异常",e);
            }
            message.reply(null);
        });
    }


    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        //查询单个用户
        bus.<String>consumer(UserHandler.REDIS_USER_QUERY).handler(msg ->{
            try {
                String username = msg.body();
                hGetAll(msg,username);
            } catch (Exception e) {
                logger.error("--", e);
                msg.fail(400, e.getMessage());
            }
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
            RedisClientUtil.getRedisAPI().hset(args, res -> {
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
            RedisClientUtil.getRedisAPI().hset(args, res -> {
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
