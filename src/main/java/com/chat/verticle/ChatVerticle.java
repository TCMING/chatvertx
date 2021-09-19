package com.chat.verticle;

import com.chat.handler.MessageHandler;
import com.chat.handler.RoomHandler;
import com.chat.handler.UserHandler;
import com.chat.utils.JedisSentinelPools;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class ChatVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);

    private UserHandler userHandler;
    private RoomHandler roomHandler;
    private MessageHandler messageHandler;

    public ChatVerticle() {
        this.messageHandler = new MessageHandler();
        this.userHandler = new UserHandler();
        this.roomHandler = new RoomHandler();
    }

    @Override
    public void start(){

        logger.info("初始化：currentThread={}",Thread.currentThread().getName());

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/test").handler(this::test);
        router.post("/updateCluster").handler(this::updateCluster);
        router.get("/checkCluster").handler(this::checkCluster);

        //user
        router.post("/user").handler(userHandler::addUser);
        router.get("/userLogin").handler(userHandler::userLogin);
        router.get("/user/:username").handler(userHandler::queryUserInfo);

        //room
        router.post("/room").handler(roomHandler::createRoom);
        router.get("/room/:roomId").handler(roomHandler::queryRoomById);
        router.post("/roomList").handler(roomHandler::queryRoomList);

        router.put("/room/:roomId/enter").handler(roomHandler::roomEnter);
        router.put("/roomLeave").handler(roomHandler::roomLeave);
        router.get("/room/:roomId/users").handler(roomHandler::roomUsers);

        // message
        router.post("/message/send").handler(messageHandler::msgSend);
        router.post("/message/retrieve").handler(messageHandler::pullMessages);

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router);
        server.listen(8080);
    }

    private void updateCluster(RoutingContext routingContext){
        String json = routingContext.getBody().toString();
        EventBus bus = vertx.eventBus();
        bus.<Boolean>request(RedisVerticle.UPDATE_CLUSTER_ADD, json, reply -> {
            if( reply.succeeded() ){
                boolean success = reply.result().body().booleanValue();
                out(routingContext, Json.encodePrettily(success));
            }
        });
    }

    private void checkCluster(RoutingContext routingContext){
        logger.info("---"+Thread.currentThread().getName());
        out(routingContext , "");
    }

    private void test(RoutingContext routingContext){
//        RedisAPI api = RedisClientUtil.getRedisAPI();
//        api.set(Arrays.asList("test","1")).onSuccess(test->{
//            out(routingContext , "1");
//        });

        Jedis jedis = null;
        try{
            jedis = JedisSentinelPools.getJedis();
            jedis.set("test","1");
            String tt = jedis.get("test");
            out(routingContext , tt);
        }catch (JedisConnectionException ce){
            ce.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }

    }

    public static void out(RoutingContext ctx, String value) {
        try {
            ctx.response().putHeader("Content-Type", "application/json; charset=utf-8").end(value);
        }catch (Exception ignore){
        }
    }

    public static void sendError(RoutingContext ctx, String msg){
        ctx.response().setStatusCode(400).end(msg);
    }

}

