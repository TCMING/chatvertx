package com.chat.verticle;

import com.chat.dao.MessageRedisDao;
import com.chat.dao.RoomRedisDao;
import com.chat.dao.UserRedisDao;
import com.chat.utils.RedisClientUtil;
import com.chat.utils.SingleRedisClient;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.redis.client.RedisAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisVerticle extends AbstractVerticle {

    public static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private SingleRedisClient redisClient = new SingleRedisClient();

    private UserRedisDao userRedisDao;

    private RoomRedisDao roomRedisDao;

    private MessageRedisDao messageRedisDao;

    public static final String UPDATE_CLUSTER_ADD = "update_cluster_add";

    @Override
    public void start(){
        System.out.println("---------"+Thread.currentThread().getName());
        updateCluster();
        redisClient.start(this);
    }

    private void updateCluster(){
        MessageConsumer<String> consumner =  vertx.eventBus().consumer(UPDATE_CLUSTER_ADD);
        consumner.handler( msg -> {
            String json = msg.body();
            if(RedisClientUtil.initRedisServer(json)){
                msg.reply(true);
            }
        } );
    }

    public void initRedisDao(RedisAPI redisAPI) {
        this.userRedisDao = new UserRedisDao(redisAPI);
        this.roomRedisDao = new RoomRedisDao(redisAPI);
        this.messageRedisDao = new MessageRedisDao(redisAPI);
    }


}
