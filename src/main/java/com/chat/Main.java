package com.chat;

import com.chat.controller.UserController;
import io.vertx.core.Vertx;

public class Main {

    public static void main(String[] args){
        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(UserController.class.getName());
    }

}
