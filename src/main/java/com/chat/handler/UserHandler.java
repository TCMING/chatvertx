package com.chat.handler;

import com.chat.Main;
import com.chat.model.UserDto;
import com.chat.model.UserRequest;
import com.chat.utils.BizCheckUtils;
import com.chat.utils.GsonUtils;
import com.chat.utils.JwtUtils;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.chat.verticle.ChatServer.out;
import static com.chat.verticle.ChatServer.sendError;


public class UserHandler {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    public static final String REDIS_USER_CREATE = "redis.user.create";

    public static final String REDIS_USER_ID_GET = "redis.user.id.get";

    public static final String REDIS_USER_SINGLE_QUERY = "redis.user.single.query";

    public static final String REDIS_USER_SINGLE_QUERY_STRING = "redis.user.single.query.string";

    public void userLogin(RoutingContext context) {
        try {
            String username = context.request().getParam("username");
            String password = context.request().getParam("password");
            logger.info("userLogin " + username +" "+ password);

            EventBus bus = Main.vertx.eventBus();
            bus.<UserDto>request(REDIS_USER_SINGLE_QUERY, username, queryReply ->{
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
        bus.<String>request(REDIS_USER_SINGLE_QUERY_STRING, username, queryReply ->{
            if(queryReply.succeeded() && queryReply.result() != null){
                String userInfo = queryReply.result().body();
                out(context,userInfo);
            }else{
                sendError(context,"Invalid username supplied");
            }
        });
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
            bus.<UserDto>request(REDIS_USER_SINGLE_QUERY, userDto.getUsername(), queryReply ->{
                if(queryReply.succeeded() ){
                    UserDto queryUserInfo = queryReply.result().body();
                    if(queryUserInfo == null){
                        bus.<Integer>request(REDIS_USER_ID_GET,1,idGetReply->{
                            if(idGetReply.succeeded() && idGetReply.result() != null && idGetReply.result().body() > 0){
                                userDto.setId(idGetReply.result().body());
                                bus.<Boolean>request(REDIS_USER_CREATE,userDto, saveReply ->{
                                    if(saveReply.succeeded() && saveReply.result().body()){
                                        out(context, Json.encodePrettily(true));
                                    }else {
                                        context.fail(400);
                                    }
                                });
                            }else{
                                context.fail(400);
                            }
                        });
                    }
                    out(context, Json.encodePrettily(true));

                }else{
                    context.fail(400);
                }
            });
        }catch (Exception e){
            context.fail(400,e);
        }
    }

}
