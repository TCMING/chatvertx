package com.chat.utils;

import com.chat.Main;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.redis.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleRedisClient {

    Logger logger = LoggerFactory.getLogger(SingleRedisClient.class);

    private static final int MAX_RECONNECT_RETRIES = 16;
    private RedisConnection connection;

    private static int index = 0;

    //cmd里面 D:\software\Redis-x64-5.0.9 > redis-server.exe redis.windows.conf
    private static String[] connectionString = {"redis://localhost:6379","redis://localhost:6380","redis://localhost:6381"};

    public RedisConnection init(){
        Promise<RedisConnection> promise = Promise.promise();
        Thread thread = new Thread( () ->{
            createRedisClient(index++,onCreate -> {
                if (onCreate.succeeded()) {
                    logger.info("Redis 连接成功！");
                    promise.complete(onCreate.result());
                }else if(onCreate.failed()) {
                    logger.error("Redis 连接失败！");
                    promise.fail("Redis 连接失败！");
                }
            });
        });
        thread.start();
        while (!promise.future().isComplete()){
            try {
                Thread.sleep(100);
            }catch (Exception e){
            }
        }
        return promise.future().result();
    }

    private void createRedisClient(int index,Handler<AsyncResult<RedisConnection>> handler) {

        Redis.createClient(Main.vertx, new RedisOptions()
                .setType(RedisClientType.STANDALONE)
                .addConnectionString(connectionString[index%3])
                .setMasterName("master")
                .setRole(RedisRole.MASTER)
                .setMaxPoolSize(8)
                .setMaxWaitingHandlers(8)).connect(onConnect -> {
            if (onConnect.succeeded()) {
                connection = onConnect.result();
                // make sure the client is reconnected on error
                connection.exceptionHandler(e -> {
                    // attempt to reconnect
                    attemptReconnect();
                });
            }
            // allow further processing
            handler.handle(onConnect);
        });
    }

    private void attemptReconnect() {
        logger.info("第" + index + "次尝试重连 Redis");
        // retry with backoff up to 10240 ms
        long backoff = (long) (Math.pow(2, Math.min(index, 10)) * 10);
        Main.vertx.setTimer(backoff, timer -> createRedisClient(++index, onReconnect -> {
            if (onReconnect.succeeded()) {
                RedisClientUtil.setRedisAPI(RedisAPI.api(onReconnect.result()));
            } else if (onReconnect.failed()) {
                attemptReconnect();
            }
        }));

    }

}
