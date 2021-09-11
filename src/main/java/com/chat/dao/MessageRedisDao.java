package com.chat.dao;

import com.chat.Main;
import com.chat.handler.MessageHandler;
import com.chat.handler.RoomHandler;
import com.chat.handler.UserHandler;
import com.chat.model.RoomDto;
import com.chat.model.UserDto;
import com.chat.utils.GsonUtils;
import com.chat.utils.RedisClientUtil;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageRedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private  RedisAPI redisAPI;

    private final String RoomDtoList = "RoomDtoList";

    private final String IdRoomMap = "IdRoomMap";

    private final String RoomIdKey = "RoomIdKey";

    @Deprecated
    public MessageRedisDao(RedisAPI redisAPI) {
        this.redisAPI = redisAPI;
        this.baseOperate();
    }

    public MessageRedisDao() {
        this.baseOperate();
    }

    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        //保存房间消息 key(roomid+message) value(message)
        bus.<List<String>>consumer(MessageHandler.REDIS_MESSAGE_SEND).handler(msg ->{
            List<String> saveInfo = msg.body();
            RedisClientUtil.getRedisAPI().lpush(saveInfo, res -> {
                try {
                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.NUMBER) {
                        logger.info("保存房间消息列表完成 " + GsonUtils.toJsonString(saveInfo));
                        msg.reply(true);
                    } else {
                        logger.warn("保存房间消息列表失败 " + GsonUtils.toJsonString(saveInfo));
                        msg.reply(false);
                    }
                } catch (Exception e) {
                    msg.fail(400, e.getMessage());
                }
            });
        });

        //查询房间消息 key(roomid+message) value(message)
        bus.<List<String>>consumer(MessageHandler.REDIS_MESSAGE_RETRIEVE).handler(msg ->{
            List<String> queryParam = msg.body();
            if(queryParam == null || queryParam.size() != 3){
                msg.reply(false);
            }
            RedisClientUtil.getRedisAPI().lrange(queryParam.get(0),queryParam.get(1),queryParam.get(2), res -> {
                try {
                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.MULTI) {
                        logger.info("查询房间消息完成 " + GsonUtils.toJsonString(queryParam));
                        msg.reply(res.result().toString());
                    } else {
                        logger.warn("查询房间消息失败 " + GsonUtils.toJsonString(queryParam));
                        msg.reply(null);
                    }
                } catch (Exception e) {
                    msg.fail(400, e.getMessage());
                }
            });
        });

    }


}
