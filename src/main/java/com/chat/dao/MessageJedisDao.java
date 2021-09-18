package com.chat.dao;

import com.chat.Main;
import com.chat.handler.MessageHandler;
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

import static com.chat.utils.JedisSentinelPools.getJedis;

public class MessageJedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private final static String MESSAGE_SET = "messageSet";

    public MessageJedisDao() {
        this.baseOperate();
    }

    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        //保存房间消息 key(roomid+message) value(message)
        bus.<List<String>>consumer(MessageHandler.REDIS_MESSAGE_SEND).handler(msg ->{
            List<String> saveInfo = msg.body();
            try {
                long index = getJedis().lpush(saveInfo.get(0), saveInfo.get(1));
                msg.reply(true);
            } catch (Exception e) {
                logger.warn("保存房间消息列表失败 " + GsonUtils.toJsonString(saveInfo));
                msg.fail(400, e.getMessage());
            }
        });

        //查询房间消息 key(roomid+message) value(message)
        bus.<List<String>>consumer(MessageHandler.REDIS_MESSAGE_RETRIEVE).handler(msg ->{
            List<String> queryParam = msg.body();
            try {
                if(queryParam == null || queryParam.size() != 3){
                    msg.reply(false);
                    return ;
                }
                List<String> messages = getJedis().lrange(queryParam.get(0), Integer.parseInt(queryParam.get(1)),
                        Integer.parseInt(queryParam.get(2)));
                msg.reply(messages);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        // 保存
        bus.<String>consumer(MessageHandler.REDIS_MESSAGE_ID_SADD).handler(msg ->{
            try {
                String messageId = msg.body();
                long resNum = getJedis().sadd(MESSAGE_SET,messageId);
                msg.reply(resNum == 1);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });
    }


}
