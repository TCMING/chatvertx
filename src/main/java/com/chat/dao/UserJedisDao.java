package com.chat.dao;

import com.chat.Main;
import com.chat.handler.UserHandler;
import com.chat.model.UserDto;
import com.chat.utils.GsonUtils;
import com.chat.utils.RedisClientUtil;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.redis.client.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chat.utils.JedisSentinelPools.getJedis;

public class UserJedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private final String UserIdKey = "UserIdKey";

    public UserJedisDao() {
        this.baseOperate();
    }

    private void addUser(Message msg , UserDto userDto){
        Map<String, String> keyValues = GsonUtils.<UserDto>bean2Map(userDto);
        if (keyValues == null) {
            logger.error("用户信息错误 " + userDto.getUsername());
            msg.fail(400, "message error");
            return;
        }
        AtomicBoolean finalRes = new AtomicBoolean(true);
        keyValues.forEach((key, value) -> {
            long res = getJedis().hset(userDto.getUsername(), key, value);
            finalRes.set(res == 1);
        });
        logger.info("保存用户信息完成 username={},finalRes={}", userDto.getUsername(), finalRes.get());
        msg.reply(finalRes.get());
    }

    private void queryUser(Message msg , String username){
        Map<String, String> userMap = getJedis().hgetAll(username);
        UserDto userDto = GsonUtils.mapToBean(userMap,UserDto.class);
        if (userDto.getUsername() != null) {
            msg.reply(userDto);
        }else{
            msg.reply(null);
        }
    }

    private void updateUserRoom(Message msg , UserDto userDto){
        long res = getJedis().hset(userDto.getUsername(), "roomId", String.valueOf(userDto.getRoomId()));
        logger.info("更新用户房间id完成 " + userDto.getUsername());
        msg.reply(true);
    }

    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        //查询单个用户
        bus.<String>consumer(UserHandler.REDIS_USER_QUERY).handler(msg ->{
            String username = msg.body();
            try {
                queryUser(msg , username);
            } catch (JedisConnectionException ce){
                logger.error("-- jedis connection exception");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    logger.error("-- InterruptedException" , ie);;
                }
                queryUser(msg , username);
            } catch (Exception e) {
                logger.error("用户查询异常", e);
                msg.fail(400, e.getMessage());
            }
        });

        //保存用户 map类型 key(username) value(UserDto各字段)
        bus.<UserDto>consumer(UserHandler.REDIS_USER_CREATE).handler(msg ->{
            UserDto userDto = msg.body();
            try {
                addUser(msg , userDto);
            }catch (JedisConnectionException ce){
                logger.error("-- jedis connection exception");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    logger.error("-- InterruptedException" , ie);;
                }
                addUser(msg , userDto);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //更新用户房间信息
        bus.<UserDto>consumer(UserHandler.REDIS_USER_ROOM_ID_UPDATE).handler(msg -> {
            UserDto userDto = msg.body();
            try {
                updateUserRoom(msg , userDto);
            }catch (JedisConnectionException ce){
                logger.error("-- jedis connection exception");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    logger.error("-- InterruptedException" , ie);;
                }
                updateUserRoom(msg , userDto);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });
    }
}
