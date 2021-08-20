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
    public void start() throws Exception{
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/user").handler(this::addUser);
        router.get("/test").handler(this::getTest);

        server.requestHandler(router);
        server.listen(8080);
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

