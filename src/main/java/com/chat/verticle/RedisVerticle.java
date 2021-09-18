package com.chat.verticle;

import com.chat.dao.MessageJedisDao;

import com.chat.dao.RoomJedisDao;

import com.chat.dao.UserJedisDao;

import com.chat.utils.RedisClientUtil;
import com.chat.utils.SingleRedisClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.redis.client.RedisAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisVerticle extends AbstractVerticle {

    public static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private MessageJedisDao userRedisDao;

    private RoomJedisDao roomRedisDao;

    private UserJedisDao messageRedisDao;

    public static final String UPDATE_CLUSTER_ADD = "update_cluster_add";

    @Override
    public void start(){
        System.out.println("---------"+Thread.currentThread().getName());
        updateCluster();
        initRedisDao();
    }

    private void updateCluster(){
        MessageConsumer<String> consumner =  vertx.eventBus().consumer(UPDATE_CLUSTER_ADD);
        consumner.handler( msg -> {
            try {
                String json = msg.body();
                if(RedisClientUtil.initRedisServer(json)){
                    msg.reply(true);
                    return ;
                }
            }catch (Exception e){
                logger.error("初始化redisServer失败",e);
            }
            msg.reply(false);
        } );
    }

    public void initRedisDao() {
        this.userRedisDao = new MessageJedisDao();
        this.roomRedisDao = new RoomJedisDao();
        this.messageRedisDao = new UserJedisDao();
    }

}
