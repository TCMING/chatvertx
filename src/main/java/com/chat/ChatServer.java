package com.chat;

import com.chat.repository.MessageRepository;
import com.chat.repository.RoomRepository;
import com.chat.repository.UserRepository;
import com.chat.service.MessageService;
import com.chat.service.RoomService;
import com.chat.service.UserService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
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
    private MessageRepository messageRepository;
    private RoomRepository roomRepository;
    private UserRepository userRepository;

    public static void main(String[] args){
        Vertx.vertx().deployVerticle(new ChatServer());
        // int loopNum = 8;
        // VertxOptions vo = new VertxOptions();
        // vo.setEventLoopPoolSize(loopNum);
        // Vertx vertx = Vertx.vertx(vo);
        // for(int i=0; i<loopNum; i++)
        //     vertx.deployVerticle(new ChatServer());
    }

    public ChatServer() {
        this.messageRepository = new MessageRepository();
        this.roomRepository = new RoomRepository();
        this.userRepository = new UserRepository();
        this.userService = new UserService(userRepository);
        this.roomService = new RoomService(roomRepository,userRepository);
        this.messageService = new MessageService(messageRepository,userRepository);
    }

    @Override
    public void start() throws Exception {
//        System.out.println("---------"+Thread.currentThread().getName());
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // user
        router.post("/user").blockingHandler(this::addUser);
        router.get("/userLogin").blockingHandler(this::userLogin);
        router.get("/user").blockingHandler(this::getUserInfo);

        //room
        router.post("/room").blockingHandler(this::room);
        router.put("/room/:roomId/enter").blockingHandler(this::roomEnter);
        router.put("/room/roomLeave").blockingHandler(this::roomLeave);
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
        System.out.println("roomLeave");

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

