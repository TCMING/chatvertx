package com.chat.verticle;

import com.chat.handler.MessageHandler;
import com.chat.handler.RoomHandler;
import com.chat.handler.UserHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.redis.client.*;

public class ChatVerticle extends AbstractVerticle {

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

        System.out.println("---------"+Thread.currentThread().getName());

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/test").handler(this::test);
        router.post("/updateCluster").handler(this::updateCluster);

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

    private void test(RoutingContext routingContext){
        Redis redis = Redis.createClient(
                vertx,
                new RedisOptions()
                        .setType(RedisClientType.SENTINEL)
                        .addConnectionString("redis://10.63.5.164:26379")
                        .addConnectionString("redis://10.63.5.164:26380")
                        .addConnectionString("redis://10.63.5.164:26381")
                        .setMasterName("mymaster")
                        .setRole(RedisRole.MASTER)
                        .setMaxPoolSize(8)
                        .setMaxWaitingHandlers(8));

        RedisAPI api = RedisAPI.api(redis);
        api.get("test").onSuccess(value->{
            System.out.println("---------"+Thread.currentThread().getName());
            System.out.println(value);
            out(routingContext, value.toString());
        });
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

