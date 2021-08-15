package com.chat;

import com.chat.controller.UserController;
import io.vertx.core.Vertx;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.HashMap;
import java.util.Map;


public class Main extends AbstractVerticle {

    private UserService userService;

    public static void main(String[] args){

	//集群部署
	//VertxOptions options = new VertxOptions();
	//Vertx.clusteredVertx(options, res -> {
	//	if (res.succeeded()) {
	//		res.result().deployVerticle(new HttpVerticle());
	//	}
	//}

        Vertx vertx = Vertx.vertx();
	vertx.deployVerticle(new RestServer());
	//userService = new UserService()
    }


    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.post("/user").blockingHandler(this::addUser);
	router.get("/userLogin").blockingHandler(this::userlogin);
	router.get("/user/{username}").handler(this::getUserInfo);

        server.requestHandler(router);
        server.listen(8080);
    }

    private void addUser(RoutingContext ctx) {

    }

    private void userlogin(RoutingContext ctx) {

    }

    private void getUserInfo(RoutingContext ctx) {

    }






}











r
