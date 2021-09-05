package com.chat.utils;

import com.chat.Main;
import com.chat.dao.UserRedisDao;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.redis.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleRedisClient {

    Logger logger = LoggerFactory.getLogger(SingleRedisClient.class);

    private static final int MAX_RECONNECT_RETRIES = 16;
    private RedisConnection client;
    private RedisAPI redis;
    //cmd里面 D:\software\Redis-x64-5.0.9 > redis-server.exe redis.windows.conf
    private static final String REDIS_CONNECTION_STRING = "redis://localhost:6379";

    public void start(RedisVerticle redisVerticle){

        createRedisClient(onCreate -> {
            if (onCreate.succeeded()) {
                logger.info("Redis 连接成功！");
                redis = RedisAPI.api(client);
                redisVerticle.setUserRedisDao(redis);
            }else if(onCreate.failed()) {
                logger.error("Redis 连接失败！");
            }
        });

    }

    private void createRedisClient(Handler<AsyncResult<RedisConnection>> handler) {

        Redis.createClient(Main.vertx, new RedisOptions()
                .setType(RedisClientType.STANDALONE)
                .addConnectionString(REDIS_CONNECTION_STRING)
                .setMasterName("master")
                .setRole(RedisRole.MASTER)
                .setPoolCleanerInterval(-1)
                .setPoolRecycleTimeout(120000)
                .setMaxPoolSize(8)
                .setMaxWaitingHandlers(8)).connect(onConnect -> {
            if (onConnect.succeeded()) {
                client = onConnect.result();
                // make sure the client is reconnected on error
                client.exceptionHandler(e -> {
                    // attempt to reconnect
                    attemptReconnect(0);
                });
            }
            // allow further processing
            handler.handle(onConnect);
        });
    }

    private void attemptReconnect(int retry) {

        logger.info("第" + retry + "次尝试重连 Redis");

        if (retry > MAX_RECONNECT_RETRIES) {
            // we should stop now, as there's nothing we can do.
        } else {
            // retry with backoff up to 10240 ms
            long backoff = (long) (Math.pow(2, Math.min(retry, 10)) * 10);

            Main.vertx.setTimer(backoff, timer -> createRedisClient(onReconnect -> {
                if (onReconnect.failed()) {
                    attemptReconnect(retry + 1);
                }
            }));
        }
    }

}
