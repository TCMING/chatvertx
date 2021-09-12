package com.chat.handler;

import com.chat.Main;
import com.chat.model.*;
import com.chat.utils.BizCheckUtils;
import com.chat.utils.ChatException;
import com.chat.utils.GsonUtils;
import com.chat.utils.JwtUtils;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chat.verticle.ChatVerticle.out;
import static com.chat.verticle.ChatVerticle.sendError;


public class RoomHandler {

    private static final Logger logger = LoggerFactory.getLogger(RoomHandler.class);

    public static final String REDIS_ROOM_ID_INIT = "redis.room.id.init";
    //单个房间信息
    public static final String REDIS_ROOM_ID_NAME_QUERY = "redis.room.id.name";

    public static final String REDIS_ROOM_ID_NAME_SAVE = "redis.room.id.name.save";
    //房间列表
    public static final String REDIS_ROOM_DTO_LIST_QUERY = "redis.room.dto.name";

    public static final String REDIS_ROOM_DTO_LIST_SAVE = "redis.room.dto.list.save";

    //roomid -> username关系 添加成员
    public static final String REDIS_ROOM_ID_USER_NAME_SADD = "redis.roomid,username.sadd";
    //roomid -> username关系 移除成员
    public static final String REDIS_ROOM_ID_USER_NAME_SREM = "redis.roomid,username.srem";
    //roomid -> username关系 返回所有成员
    public static final String REDIS_ROOM_ID_USER_NAME_SMEMBERS = "redis.roomid,username.smembers";

    /**
     * 目标：
     * 1.房间内用户列表快速查询 并 退出
     * 2.用户所在房间可以查询 并 退出
     *
     * 存储
     * 1.利用集合 key(roomId) values(username)
     * 添加成员 SADD key member1 [member2]
     * 移除成员 SREM key member1 [member2]
     * 返回所有成员 SMEMBERS key
     *
     * 2.用户信息 存储当前登录房间
     * 2.1 更新用户信息
     * 2.2 查询用户信息
     *
     * @param context
     */
    public void roomEnter(RoutingContext context) {
        try {
            String roomid = context.request().getParam("roomid");
            String username = JwtUtils.parseUsername(context.request().getHeader("Authorization"));
            if(StringUtils.isBlank(username)){
                sendError(context,"Invalid token");
                return;
            }
            EventBus bus = Main.vertx.eventBus();
            //1.校验用户是否存在
            bus.<UserDto>request(UserHandler.REDIS_USER_QUERY,username,userReply ->{
                if(userReply.succeeded() && userReply.result() != null && userReply.result().body() != null){
                    //2.校验房间是否存在
                    UserDto userDto = userReply.result().body();
                    bus.<String>request(RoomHandler.REDIS_ROOM_ID_NAME_QUERY,roomid,roomReply ->{
                        if(roomReply.succeeded() && roomReply.result() != null && roomReply.result().body() != null){
                            //3.离开原来的房间
                            if(userDto.getRoomId() > 0){
                                List<String> saveInfo = Stream.of(String.valueOf(userDto.getRoomId()),username).collect(Collectors.toList());
                                bus.<Boolean>request(RoomHandler.REDIS_ROOM_ID_USER_NAME_SREM,saveInfo,remReply ->{
                                    if(remReply.succeeded() && remReply.result() != null && remReply.result().body()){
                                        //4.保存用户到 roomid -> username集合中
                                        List<String> newSaveInfo = Stream.of(roomid,username).collect(Collectors.toList());
                                        bus.<Boolean>request(RoomHandler.REDIS_ROOM_ID_USER_NAME_SADD,newSaveInfo,setReply ->{
                                            if(setReply.succeeded() && setReply.result() != null && setReply.result().body()){
                                                //5.更新用户房间信息
                                                userDto.setRoomId(Integer.valueOf(roomid));
                                                bus.<Boolean>request(UserHandler.REDIS_USER_ROOM_ID_UPDATE,userDto,updateReply ->{
                                                    if(updateReply.succeeded() && updateReply.result() != null && updateReply.result().body()){
                                                        out(context,"Enter the Room");
                                                        return ;
                                                    }
                                                    sendError(context,"save roomid username error");
                                                });
                                            }else{
                                                sendError(context,"save roomid username error");
                                            }
                                        });
                                    }
                                });
                            }else{
                                //4.保存用户到 roomid -> username集合中
                                List<String> newSaveInfo = Stream.of(roomid,username).collect(Collectors.toList());
                                bus.<Boolean>request(RoomHandler.REDIS_ROOM_ID_USER_NAME_SADD,newSaveInfo,setReply ->{
                                    if(setReply.succeeded() && setReply.result() != null && setReply.result().body()){
                                        //5.更新用户房间信息
                                        userDto.setRoomId(Integer.valueOf(roomid));
                                        bus.<Boolean>request(UserHandler.REDIS_USER_ROOM_ID_UPDATE,userDto,updateReply ->{
                                            if(updateReply.succeeded() && updateReply.result() != null && updateReply.result().body()){
                                                out(context,"Enter the Room");
                                                return ;
                                            }
                                            sendError(context,"save roomid username error");
                                        });
                                    }else{
                                        sendError(context,"save roomid username error");
                                    }
                                });
                            }
                        }else{
                            sendError(context,"Invalid roomid");
                        }
                    });
                }else{
                    sendError(context,"Invalid username");
                }
            });

        } catch (Exception e) {
            context.fail(400,e);
        }
    }

    public void roomLeave(RoutingContext context) {
        try {
            String username = JwtUtils.parseUsername(context.request().getHeader("Authorization"));
            if(StringUtils.isBlank(username)){
                sendError(context,"Invalid token");
                return;
            }
            EventBus bus = Main.vertx.eventBus();
            //1.校验用户是否存在
            bus.<UserDto>request(UserHandler.REDIS_USER_QUERY,username,userReply ->{
                if(userReply.succeeded() && userReply.result() != null && userReply.result().body() != null){
                    //2.校验房间是否存在
                    UserDto curUserDto = userReply.result().body();
                    if(curUserDto.getRoomId() <=0 ){
                        out(context,"User has been exit the room");
                        return ;
                    }
                    bus.<String>request(RoomHandler.REDIS_ROOM_ID_NAME_QUERY,String.valueOf(curUserDto.getRoomId()),roomReply ->{
                        if(roomReply.succeeded() && roomReply.result() != null && roomReply.result().body() != null){
                            //3.删除用户到 roomid -> username集合中
                            List<String> saveInfo = Stream.of(String.valueOf(curUserDto.getRoomId()),username).collect(Collectors.toList());
                            bus.<Boolean>request(RoomHandler.REDIS_ROOM_ID_USER_NAME_SREM,saveInfo,remReply ->{
                                if(remReply.succeeded() && remReply.result() != null && remReply.result().body()){
                                    //4.更新用户房间信息
                                    curUserDto.setRoomId(0);
                                    bus.<Boolean>request(UserHandler.REDIS_USER_ROOM_ID_UPDATE,curUserDto,updateReply ->{
                                        if(updateReply.succeeded() && updateReply.result() != null && updateReply.result().body()){
                                            out(context,"User has been exit the room");
                                            return ;
                                        }
                                        sendError(context,"update the user success error");
                                    });
                                }else{
                                    sendError(context,"leave roomid username error");
                                }
                            });
                        }else{
                            sendError(context,"Invalid roomid");
                        }
                    });
                }else{
                    sendError(context,"Invalid username");
                }
            });
        } catch (Exception e) {
            context.fail(400,e);
        }
    }

    public void roomUsers(RoutingContext context) {
        try {
            String roomid = context.request().getParam("roomid");
            if(StringUtils.isBlank(roomid)){
                sendError(context,"Invalid Room ID");
                return ;
            }
            EventBus bus = Main.vertx.eventBus();
            //1.校验用户是否存在
            bus.<String>request(RoomHandler.REDIS_ROOM_ID_NAME_QUERY,roomid,nameReply ->{
                if(nameReply.succeeded() && nameReply.result() != null && nameReply.result().body() != null){
                    bus.<String>request(RoomHandler.REDIS_ROOM_ID_USER_NAME_SMEMBERS,roomid,memberReply ->{
                        if(memberReply.succeeded() && memberReply.result() != null){
                            String memberStr = memberReply.result().body();
                            List<String> memberList = GsonUtils.jsonToList(memberStr,String.class);
                            List<UserRO> userList = new LinkedList<>();
                            memberList.forEach(str ->{
                                userList.add(new UserRO(str));
                            });
                            out(context,GsonUtils.toJsonString(userList));
                            return ;
                        }
                        sendError(context,"Invalid Room ID");
                    });
                }else{
                    sendError(context,"Invalid Room ID");
                }
            });
        } catch (Exception e) {
            context.fail(400,e);
        }
    }

    /**
     * 1.查询当前可用roomId
     * 2.维护 roomId和roomName  (roomInfo 查询使用)
     * 3.维护 roomDtoList  （roomList 查询使用）
     * @param context
     */
    public void createRoom(RoutingContext context) {
        try {
            //TODO 校验用户信息
            String json = context.getBody().toString();
            Room room = GsonUtils.jsonToBean(json, Room.class);
            if(room == null || StringUtils.isBlank(room.getName())){
                sendError(context,"Invalid input");
            }
            String username = JwtUtils.parseUsername(context.request().getHeader("Authorization"));
            if(StringUtils.isBlank(username)){
                sendError(context,"Invalid token");
                return;
            }

            EventBus bus = Main.vertx.eventBus();
            //1.校验用户已存在
            bus.<UserDto>request(UserHandler.REDIS_USER_QUERY,username,userReply ->{
                if(userReply.succeeded() && userReply.result() != null && userReply.result().body() != null){
                    Integer queryNum = 1;
                    //2.获取可用房间id
                    bus.<Integer>request(REDIS_ROOM_ID_INIT, queryNum, idGetReply ->{
                        if(idGetReply.succeeded() && idGetReply.result() != null &&  idGetReply.result().body() > 0){
                            Integer roomId = idGetReply.result().body();
                            RoomDto roomDto = new RoomDto(roomId,room.getName());
                            //3.保存房间 id与name信息
                            bus.<Boolean>request(REDIS_ROOM_ID_NAME_SAVE, roomDto, saveIdNameReply -> {
                                if (saveIdNameReply.succeeded() && saveIdNameReply.result() != null && saveIdNameReply.result().body()) {
                                    //4.保存房间roomDto列表
                                    bus.<Boolean>request(REDIS_ROOM_DTO_LIST_SAVE, roomDto, saveReply -> {
                                        if (saveReply.succeeded() && saveReply.result() != null && saveReply.result().body()) {
                                            out(context, Json.encodePrettily(roomDto.getId()));
                                        } else {
                                            context.fail(400);
                                        }
                                    });
                                } else {
                                    context.fail(400);
                                }
                            });

                        }else{
                            context.fail(400);
                        }
                    });
                }
            });
        } catch (Exception e) {
            sendError(context,e.getMessage());
        }
    }

    public void queryRoomById(RoutingContext context) {
        String roomid = context.request().getParam("roomId");
        EventBus bus = Main.vertx.eventBus();
        bus.<String>request(REDIS_ROOM_ID_NAME_QUERY, roomid, queryReply ->{
            if(queryReply.succeeded() && queryReply.result() != null  ){
                String roomName = queryReply.result().body();
                if(StringUtils.isNotBlank(roomName)){
                    out(context,roomName);
                    return;
                }
            }
            context.fail(400);
        });
    }

    public void queryRoomList(RoutingContext context) {
        String json = context.getBody().toString();
        QueryControlData roomControl = GsonUtils.jsonToBean(json,QueryControlData.class);
        if(roomControl.getPageIndex() < 0 || roomControl.getPageSize() < 0){
            context.fail(400);
            return ;
        }
        EventBus bus = Main.vertx.eventBus();
        bus.<String>request(REDIS_ROOM_DTO_LIST_QUERY, roomControl, queryReply ->{
            if(queryReply.succeeded() && queryReply.result() != null ){
                String roomList = queryReply.result().body();
                if(StringUtils.isNotBlank(roomList)){
                    out(context,roomList);
                    return;
                }
            }
            context.fail(400);
        });
    }

}
