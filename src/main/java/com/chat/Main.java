package com.chat;

import com.chat.service.MessageService;
import com.chat.service.RoomService;
import com.chat.service.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;


class ChatServer extends AbstractVerticle {
    private UserService userService;
    private RoomService roomService;
    private MessageService messageService;

    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/test").handler(this::getTest);

        // user
        router.post("/user").blockingHandler(this::addUser);
        router.get("/userLogin").blockingHandler(this::userlogin);
        router.get("/user").blockingHandler(this::getUserInfo);

        // message
        router.post("/message/send").blockingHandler(this::msgSend);
        router.post("/message/retrieve").blockingHandler(this::getMsgList);

        //room
        router.post("/room").blockingHandler(this::room);
        router.put("/room/id/enter").blockingHandler(this::roomEnter);
        router.put("/room/roomLeave").blockingHandler(this::roomLeave);
        router.get("/room").blockingHandler(this::roomIdList);
        router.get("/roomList").blockingHandler(this::roomList);
        router.get("/room/id/users").blockingHandler(this::roomUserList);



        server.requestHandler(router);
        server.listen(8080);
    }

    private  void roomList(RoutingContext routingContext) {

    }

    private void roomUserList(RoutingContext routingContext) {

    }

    private void roomIdList(RoutingContext routingContext) {

    }

    private void getMsgList(RoutingContext routingContext) {
    }

    private void roomLeave(RoutingContext routingContext) {
    }

    private void roomEnter(RoutingContext routingContext) {
    }

    private void room(RoutingContext routingContext) {
    }

    private void msgSend(RoutingContext routingContext) {
    }

    private void getUserInfo(RoutingContext routingContext) {
    }

    private void userlogin(RoutingContext routingContext) {
    }

    private void getTest(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");
        response.end("Hello Test!");
    }

    private void addUser(RoutingContext ctx) {
        JsonObject json = ctx.getBody().toJsonObject();

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", json);

        // 模拟service调用
        out(ctx, Json.encodePrettily(result));
    }

    private void out(RoutingContext ctx, String msg) {
        ctx.response().putHeader("Content-Type", "application/json; charset=utf-8")
                .end(msg);
    }
}

public class Main extends AbstractVerticle {

    public static void main(String[] args){
        Vertx.vertx().deployVerticle(new ChatServer());
    }

}

