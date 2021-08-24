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
        router.get("/userLogin").blockingHandler(this::userLogin);
        router.get("/user").blockingHandler(this::getUserInfo);

        //room
        router.post("/room").blockingHandler(this::room);
        router.put("/room/:roomId/enter").blockingHandler(this::roomEnter);
        router.put("/room/roomLeave").blockingHandler(this::roomLeave);  //TODO
        router.get("/room/:roomId").blockingHandler(this::roomId);
        router.post("/roomList").blockingHandler(this::roomList);
        router.get("/room/:roomId/users").blockingHandler(this::roomUserList);

        // message
        router.post("/message/send").blockingHandler(this::msgSend);
        router.post("/message/retrieve").blockingHandler(this::getMsgList);

        server.requestHandler(router);
        server.listen(8080);
    }

    private  void roomList(RoutingContext routingContext) {
        JsonObject json = routingContext.getBody().toJsonObject();
        System.out.println("roomList " + json.toString());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", json);
        // 模拟service调用
        out(routingContext, Json.encodePrettily(result));
    }

    private void roomUserList(RoutingContext routingContext) {
        String id = routingContext.request().getParam("roomId");
        System.out.println("roomUserList " + id);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "getUserByName");
        out(routingContext, Json.encodePrettily(result));
    }

    private void roomId(RoutingContext routingContext) {
        String id = routingContext.request().getParam("roomId");
        System.out.println("roomId " + id);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "getUserByName");
        out(routingContext, Json.encodePrettily(result));
    }

   private void roomLeave(RoutingContext routingContext) {
        System.out.println("roomLeave " + routingContext.request().toString());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        // 模拟service调用
        out(routingContext, Json.encodePrettily(result));
    }

    private void roomEnter(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        System.out.println("roomEnter " + id);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "getUserByName");
        out(routingContext, Json.encodePrettily(result));
    }

    private void room(RoutingContext routingContext) {
        JsonObject json = routingContext.getBody().toJsonObject();
        System.out.println("room: " + json.toString());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", json);
        // 模拟service调用
        out(routingContext, Json.encodePrettily(result));
    }

    private void msgSend(RoutingContext routingContext) {
        JsonObject json = routingContext.getBody().toJsonObject();
        System.out.println("msgSend " + json.toString());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        // 模拟service调用
        out(routingContext, Json.encodePrettily(result));
    }

    private void getMsgList(RoutingContext routingContext) {
        JsonObject json = routingContext.getBody().toJsonObject();
        System.out.println("getMsgList " + json.toString());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        // 模拟service调用
        out(routingContext, Json.encodePrettily(result));
    }

    private void getUserInfo(RoutingContext routingContext) {
        String username = routingContext.request().getParam("username");
        System.out.println("getUserInfo " + username);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "getUserByName");
        out(routingContext, Json.encodePrettily(result));
    }

    private void userLogin(RoutingContext routingContext) {
        String username = routingContext.request().getParam("username");
        String password = routingContext.request().getParam("password");
        System.out.println("userLogin " + username +" "+ password);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "login");
        out(routingContext, Json.encodePrettily(result));
    }

    private void getTest(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain");
        response.end("Hello Test!");
    }

    private void addUser(RoutingContext routingContext) {
        JsonObject json = routingContext.getBody().toJsonObject();
        System.out.println("addUser " + json.toString());

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("msg", "success");
        result.put("data", json);

        // 模拟service调用
        out(routingContext, Json.encodePrettily(result));
    }

    private void out(RoutingContext ctx, String msg) {
        ctx.response().putHeader("Content-Type", "application/json; charset=utf-8").end(msg);
    }
}

public class Main extends AbstractVerticle {

    public static void main(String[] args){
        Vertx.vertx().deployVerticle(new ChatServer());
    }

}

