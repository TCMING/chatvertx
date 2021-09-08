package com.chat.verticle;

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

    public static final String UPDATE_CLUSTER_ADD = "update_cluster_add";

    public static final String REDIS_USER_SINGLE_QUERY = "redis.user.single.query";

    public static final String REDIS_USER_CREATE = "redis.user.create";

    public static final String REDIS_USER_QUERY_ALL = "redis.user.query.all";

    public static final String REDIS_ROOM_SAVE = "redis.room.save";

    @Override
    public void start() throws Exception {
        System.out.println("---------"+Thread.currentThread().getName());
        updateCluster();
//        redisClient.start(this);
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

    public UserRedisDao getUserRedisDao() {
        return userRedisDao;
    }

    public void setUserRedisDao(RedisAPI redisAPI) {
        this.userRedisDao = new UserRedisDao(redisAPI);
    }
}
