package com.chat.verticle;

import com.chat.utils.RedisClientUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;

public class RedisVerticle extends AbstractVerticle {
    public static Vertx vertxStatic;

    public static EventBus staticBus;

    public static final String UPDATE_CLUSTER_ADD = "update_cluster_add";

    @Override
    public void start() throws Exception {
        System.out.println("---------"+Thread.currentThread().getName());
        vertxStatic = vertx;
        staticBus = vertx.eventBus();
        updateCluster();
    }

    private void updateCluster(){
        MessageConsumer<String> consumner =  staticBus.consumer(UPDATE_CLUSTER_ADD);
        consumner.handler( msg -> {
            String json = msg.body();
            if(RedisClientUtil.initRedisServer(json)){
                msg.reply(true);
            }
        } );
    }


}
