package com.chat.handler;

import com.chat.Main;
import com.chat.model.MessageRetrive;
import com.chat.model.QueryControlData;
import com.chat.model.UserDto;
import com.chat.utils.GsonUtils;
import com.chat.utils.JwtUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chat.verticle.ChatVerticle.out;
import static com.chat.verticle.ChatVerticle.sendError;

public class MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    public static final String REDIS_MESSAGE_SEND = "redis.user.send";

    public static final String REDIS_MESSAGE_RETRIEVE = "redis.message.retrieve";

    public static final String MESSAGE = "message";


    public void msgSend(RoutingContext context) {
        try {
            //保存参数准备
            String json = context.getBody().toString();
            MessageRetrive message = GsonUtils.jsonToBean(json,MessageRetrive.class);
            message.setTimestamp(String.valueOf(System.currentTimeMillis()));
            //获取当前用户username
            String username = JwtUtils.parseUsername(context.request().getHeader("Authorization"));
            EventBus bus = Main.vertx.eventBus();
            bus.<UserDto>request(UserHandler.REDIS_USER_QUERY,username,userReply ->{
                if (userReply.succeeded() && userReply.result() != null && userReply.result().body() != null) {
                    UserDto userDto = userReply.result().body();
                    if(userDto.getRoomId() <= 0){
                        sendError(context,"current user not enter room");
                        return ;
                    }
                    //与其他key区别开
                    List<String> saveMessage = Stream.of(userDto.getRoomId() + MESSAGE,GsonUtils.toJsonString(message))
                            .collect(Collectors.toList());
                    bus.<Boolean>request(MessageHandler.REDIS_MESSAGE_SEND,saveMessage,msgReply ->{
                        if (msgReply.succeeded() && msgReply.result() != null && msgReply.result().body()){
                            out(context,"successful operation");
                        }else{
                            sendError(context,"Invalid input");
                        }
                    });
                } else {
                    sendError(context, "Invalid input");
                }
            });

        }catch (Exception e){
            context.fail(400,e);
        }
    }


    /**
     * 1.获取当前用户username
     * 2.获取用户信息
     * 3.指定房间roomid拉取消息
     * @param context
     */
    public void pullMessages(RoutingContext context){

        String json = context.getBody().toString();
        QueryControlData controlData = GsonUtils.jsonToBean(json,QueryControlData.class);

        //获取当前用户username
        String username = JwtUtils.parseUsername(context.request().getHeader("Authorization"));
        EventBus bus = Main.vertx.eventBus();
        bus.<UserDto>request(UserHandler.REDIS_USER_QUERY,username,userReply ->{
            if (userReply.succeeded() && userReply.result() != null && userReply.result().body() != null) {
                UserDto userDto = userReply.result().body();
                if(userDto.getRoomId() <= 0){
                    sendError(context,"current user not enter room");
                    return ;
                }
                //与其他key区别开
                String startIndex = String.valueOf( (-1-controlData.getPageIndex()) * controlData.getPageSize());
                List<String> saveMessage = Stream.of(userDto.getRoomId() + MESSAGE, startIndex,
                        String.valueOf(controlData.getPageSize() - 1)).collect(Collectors.toList());
                bus.<String>request(MessageHandler.REDIS_MESSAGE_RETRIEVE,saveMessage,msgReply ->{
                    if (msgReply.succeeded() && msgReply.result() != null && msgReply.result().body() != null){
                        out(context,msgReply.result().body());
                    }else{
                        sendError(context,"Invalid input");
                    }
                });
            } else {
                sendError(context, "Invalid input");
            }
        });

    }



}
