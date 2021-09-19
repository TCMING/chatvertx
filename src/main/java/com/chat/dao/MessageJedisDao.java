package com.chat.dao;

import com.chat.Main;
import com.chat.handler.MessageHandler;
import com.chat.model.MessageRetrive;
import com.chat.utils.GsonUtils;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.List;

import static com.chat.utils.JedisSentinelPools.*;

public class MessageJedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private final static String MESSAGE_SET = "messageSet";

    public MessageJedisDao() {
        this.baseOperate();
    }

    private void messageSend(Message msg,List<String> saveInfo){
        Jedis jedis = getJedis();
        try{
            long index = jedis.lpush(saveInfo.get(0), saveInfo.get(1));
            returnResource(jedis);
            msg.reply(true);
        }catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            messageSend(msg , saveInfo);
        }
    }

    private void messageRetrieve(Message msg,List<String> queryParam ){
        Jedis jedis = getLocalJedis();
        try {
            if(queryParam == null || queryParam.size() != 3){
                msg.reply(false);
                return ;
            }
            List<String> messages = jedis.lrange(queryParam.get(0), Integer.parseInt(queryParam.get(1)),
                    Integer.parseInt(queryParam.get(2)));
            returnLocalResource(jedis);
            List<MessageRetrive> messageRetrives = new ArrayList<>();
            for(String str : messages){
                messageRetrives.add(GsonUtils.jsonToBean(str ,MessageRetrive.class ));
            }
            msg.reply(GsonUtils.toJsonString(messageRetrives));
        } catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            messageRetrieve(msg , queryParam);
        }
    }

    private void messageIdSadd(Message msg,String messageId){
        Jedis jedis = getJedis();
        try {
            long resNum = jedis.sadd(MESSAGE_SET,messageId);
            returnResource(jedis);
            msg.reply(resNum == 1);
        }  catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            messageIdSadd(msg , messageId);
        }
    }


    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        //保存房间消息 key(roomid+message) value(message)
        bus.<List<String>>consumer(MessageHandler.REDIS_MESSAGE_SEND).handler(msg ->{
            List<String> saveInfo = msg.body();
            try {
                messageSend(msg,saveInfo);
            } catch (Exception e) {
                logger.warn("保存房间消息列表失败 " + GsonUtils.toJsonString(saveInfo));
                msg.fail(400, e.getMessage());
            }
        });

        //查询房间消息 key(roomid+message) value(message)
        bus.<List<String>>consumer(MessageHandler.REDIS_MESSAGE_RETRIEVE).handler(msg ->{
            List<String> queryParam = msg.body();
            try {
                messageRetrieve(msg,queryParam);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        // 保存
        bus.<String>consumer(MessageHandler.REDIS_MESSAGE_ID_SADD).handler(msg ->{
            try {
                String messageId = msg.body();
                messageIdSadd(msg,messageId);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });
    }

}
