package com.chat;

import com.chat.codec.StandardDtoCodec;
import com.chat.model.MessageDto;
import com.chat.model.QueryControlData;
import com.chat.model.RoomDto;
import com.chat.model.UserDto;
import com.chat.verticle.ChatVerticle;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static Vertx vertx;

    public static void main(String[] args) {
        vertx = Vertx.vertx();
        //初始化消息转换
        vertx.eventBus().registerDefaultCodec(UserDto.class, new StandardDtoCodec<>("UserDto"));
        vertx.eventBus().registerDefaultCodec(RoomDto.class, new StandardDtoCodec<>("RoomDto"));
        vertx.eventBus().registerDefaultCodec(MessageDto.class, new StandardDtoCodec<>("MessageDto"));
        vertx.eventBus().registerDefaultCodec(QueryControlData.class, new StandardDtoCodec<>("QueryControlData"));
        vertx.eventBus().registerDefaultCodec(ArrayList.class, new StandardDtoCodec<>("ArrayList"));

        vertx.deployVerticle(ChatVerticle.class, new DeploymentOptions().setInstances(8));
        vertx.deployVerticle(RedisVerticle.class, new DeploymentOptions().setWorker(true).setInstances(8));
    }
}
