package com.chat;

import com.chat.model.UserDto;
import com.chat.model.UserDtoCodec;
import com.chat.utils.BeanFactory;
import com.chat.verticle.ChatServer;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Main {

    public static Vertx vertx;

    public static void main(String[] args) {
        vertx = Vertx.vertx();
        vertx.eventBus().registerDefaultCodec(UserDto.class, UserDtoCodec.create());
        BeanFactory.init();
        vertx.deployVerticle(ChatServer.class, new DeploymentOptions().setInstances(8));
        vertx.deployVerticle(RedisVerticle.class, new DeploymentOptions().setWorker(true).setInstances(2));
    }
}
