package com.chat.handler;

import com.chat.Main;
import com.chat.model.UserDto;
import com.chat.model.UserRequest;
import com.chat.model.UserResponse;
import com.chat.utils.GsonUtils;
import com.chat.utils.JwtUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.chat.verticle.ChatVerticle.out;
import static com.chat.verticle.ChatVerticle.sendError;


public class UserHandler {

    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);

//    public static final String REDIS_USER_ID_INIT = "redis.user.id.init";

    public static final String REDIS_USER_CREATE = "redis.user.create";

    public static final String REDIS_USER_ROOM_ID_UPDATE = "redis.user.room.id.update";

    public static final String REDIS_USER_QUERY = "redis.user.query";

    public void userLogin(RoutingContext context) {
        try {
            String username = context.request().getParam("username");
            String password = context.request().getParam("password");
            EventBus bus = Main.vertx.eventBus();
            bus.<UserDto>request(REDIS_USER_QUERY, username, queryReply ->{
                if(queryReply.succeeded() && queryReply.result() != null){
                    UserDto userInfo = queryReply.result().body();
                    if(userInfo != null && StringUtils.equals(username,userInfo.getUsername())
                            && StringUtils.equals(password,userInfo.getPassword()) ){
                        String jwtToken = JwtUtils.createToken(username);
                        out(context,Json.encodePrettily(jwtToken));
                        return ;
                    }
                }
                sendError(context,"Invalid username supplied");
            });
        }catch (Exception e){
            context.fail(400,e);
        }
    }

    public void queryUserInfo(RoutingContext context) {
        String username = context.request().getParam("username");
        EventBus bus = Main.vertx.eventBus();
        bus.<UserDto>request(REDIS_USER_QUERY, username, queryReply ->{
            if(queryReply.succeeded() && queryReply.result() != null){
                UserDto userInfo = queryReply.result().body();
                UserResponse response = new UserResponse(userInfo.getFirstName(),userInfo.getLastName(),
                        userInfo.getEmail(),userInfo.getPhone());
                out(context,GsonUtils.toJsonString(response));
            }else{
                sendError(context,"Invalid username supplied");
            }
        });
        logger.info("test: 主线程 结束 current thread={}", Thread.currentThread().getName());
    }

    public void addUser(RoutingContext context) {
        try {
            UserRequest userRequest = GsonUtils.jsonToBean(context.getBody().toString(), UserRequest.class);

            UserDto userDto = new UserDto();
            userDto.setUsername(userRequest.getUsername());
            userDto.setFirstName(userRequest.getFirstName());
            userDto.setLastName(userRequest.getLastName());
            userDto.setPassword(userRequest.getPassword());
            userDto.setEmail(userRequest.getEmail());
            userDto.setPhone(userRequest.getPhone());

            EventBus bus = Main.vertx.eventBus();
            bus.<UserDto>request(REDIS_USER_QUERY, userDto.getUsername(), queryReply ->{
                if(queryReply.succeeded() ){
                    UserDto queryUserInfo = queryReply.result().body();
                    if(queryUserInfo == null){
                        bus.<Boolean>request(REDIS_USER_CREATE, userDto, saveReply -> {
                            if (saveReply.succeeded() && saveReply.result().body()) {
                                out(context, Json.encodePrettily(true));
                                return;
                            }
                            context.fail(400);
                        });
                    }else{
                        out(context, Json.encodePrettily(true));
                    }
                }else{
                    context.fail(400);
                }
            });
        }catch (Exception e){
            context.fail(400,e);
        }
    }

}
