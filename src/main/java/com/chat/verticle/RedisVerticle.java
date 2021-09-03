package com.chat.verticle;

import com.chat.dao.UserRedisDao;
import com.chat.utils.RedisClientUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.MessageConsumer;

public class RedisVerticle extends AbstractVerticle {

    public static final String UPDATE_CLUSTER_ADD = "update_cluster_add";

    private String userListKey = "userList";

    public static final String REDIS_USER_SINGLE_QUERY = "redis.user.single.query";

    public static final String REDIS_USER_CREATE = "redis.user.create";

    public static final String REDIS_USER_QUERY_ALL = "redis.user.query.all";

    public static final String REDIS_ROOM_SAVE = "redis.room.save";

    @Override
    public void start() throws Exception {
        System.out.println("---------"+Thread.currentThread().getName());
        updateCluster();
        createCURDService();
    }

    private void updateCluster(){
        MessageConsumer<String> consumner =  vertx.eventBus().consumer(UPDATE_CLUSTER_ADD);
        consumner.handler( msg -> {
            String json = msg.body();
            if(RedisClientUtil.initRedisServer(json)){
                msg.reply(true);
            }
            RedisClientUtil.initRedisClient();
        } );
    }

    private void createCURDService(){
        //DAO层服务
        UserRedisDao userRedisDao = new UserRedisDao();
        userRedisDao.baseOperate();

//        RoomRedisDao roomRedisDao = new RoomRedisDao();
//        roomRedisDao.baseOperate();

    }

}
