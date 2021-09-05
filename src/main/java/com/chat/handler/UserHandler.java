package com.chat.handler;

import com.chat.Main;
import com.chat.model.UserDto;
import com.chat.model.UserRequest;
import com.chat.utils.BizCheckUtils;
import com.chat.utils.GsonUtils;
import com.chat.utils.JwtUtils;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;


import static com.chat.verticle.ChatServer.out;


public class UserHandler {

    public static final String REDIS_USER_CREATE = "redis.user.create";

    public static final String REDIS_USER_ID_GET = "redis.user.id.get";

    public static final String REDIS_USER_SINGLE_QUERY = "redis.user.single.query";

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

    public void userLogin(RoutingContext routingContext) {
        try {
            String username = routingContext.request().getParam("username");
            String password = routingContext.request().getParam("password");
            System.out.println("userLogin " + username +" "+ password);

//            BizCheckUtils.check(userService.userPasswordCheck(username,password),"Invalid username or password.");
            String jwtToken = JwtUtils.createToken(username);
            out(routingContext, Json.encodePrettily(jwtToken));
        }catch (Exception e){
            routingContext.fail(400,e);
        }
    }

    public void getUserInfo(RoutingContext routingContext) {
        String username = routingContext.request().getParam("username");

        try {
            BizCheckUtils.checkNull(username, "Invalid username supplied");
//            UserDto userDto = userService.queryUserByName(username);
//            BizCheckUtils.checkNull(userDto,"Invalid username supplied");
//            UserResponse userResponse = new UserResponse(userDto.getFirstName(),userDto.getLastName(),
//                    userDto.getEmail(),userDto.getPhone());
            out(routingContext, Json.encodePrettily(true));
        }catch (Exception e){
            routingContext.fail(400,e);
        }
    }

}
