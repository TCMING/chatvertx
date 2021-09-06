package com.chat.verticle;

import com.chat.handler.UserHandler;
import com.chat.model.*;
import com.chat.repository.MessageRepository;
import com.chat.repository.RoomRepository;
import com.chat.service.MessageService;
import com.chat.service.RoomService;
import com.chat.service.UserService;
import com.chat.utils.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.redis.client.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ChatServer extends AbstractVerticle {

    private UserService userService;
    private RoomService roomService;
    private MessageService messageService;
    private MessageRepository messageRepository;
    private RoomRepository roomRepository;
    private UserHandler userHandler;

    public ChatServer() {
        this.userHandler = new UserHandler();
        this.messageRepository = BeanFactory.getInstance(MessageRepository.class);
        this.roomRepository = BeanFactory.getInstance(RoomRepository.class);
        this.userService = BeanFactory.getInstance(UserService.class);
        this.roomService = BeanFactory.getInstance(RoomService.class);
        this.messageService = BeanFactory.getInstance(MessageService.class);
    }

    @Override
    public void start() throws Exception {

        System.out.println("---------"+Thread.currentThread().getName());

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.get("/test").handler(this::test);

        router.post("/updateCluster").handler(this::updateCluster);

        // user
        router.post("/user").handler(userHandler::addUser);
        router.get("/userLogin").handler(userHandler::userLogin);
        router.get("/user/:username").handler(userHandler::queryUserInfo);

        //room
        router.post("/room").handler(this::room);
        router.put("/room/:roomId/enter").handler(this::roomEnter);
        router.put("/room/roomLeave").handler(this::roomLeave);
        router.get("/room/:roomId").handler(this::roomId);
        router.post("/roomList").handler(this::roomList);
        router.get("/room/:roomId/users").handler(this::roomUserList);

        // message
        router.post("/message/send").handler(this::msgSend);
        router.post("/message/retrieve").handler(this::getMsgList);

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router);
        server.listen(8080);
    }

    private  void roomList(RoutingContext routingContext) {
        try {
            String json = routingContext.getBody().toString();
            QueryControlData roomControl = GsonUtils.jsonToBean(json,QueryControlData.class);
            if(roomControl.getPageIndex()<0){
                throw new ChatException("invalid input");
            }
            List<RoomDto> roomVoList = roomRepository.queryRoomRecord(roomControl);
            // 模拟service调用
            out(routingContext, Json.encodePrettily(roomVoList));
        }catch (Exception e){
            routingContext.fail(400,e);
        }
    }

    private void roomUserList(RoutingContext routingContext) {
        try {
            String id = routingContext.request().getParam("roomId");
            int roomId = Integer.parseInt(id);
            List<String> users = roomService.queryRoomUsers(roomId);
            out(routingContext, Json.encodePrettily(users));
        }catch (Exception e){
            routingContext.fail(400,e);
        }

    }

    private void roomId(RoutingContext routingContext) {
        String roomid = routingContext.request().getParam("roomId");
        try {
            RoomDto roomDto = roomRepository.queryRoomById(Integer.parseInt(roomid));
            BizCheckUtils.checkNull(roomDto,"invalid roomId");
            out(routingContext, Json.encodePrettily(roomDto));
        }catch (Exception e){
            routingContext.fail(400,e);
        }
    }

   private void roomLeave(RoutingContext routingContext) {
       try {
           String username = routingContext.request().getParam("username");
           BizCheckUtils.check(roomService.roomLeave(username) , "异常");
           // 模拟service调用
           out(routingContext, Json.encodePrettily(true));
       } catch (Exception e) {
           routingContext.fail(400, e);
       }
    }

    private void roomEnter(RoutingContext routingContext) {
        try {
            String roomId = routingContext.request().getParam("roomId");
            String username = routingContext.request().getParam("username");
            BizCheckUtils.check(roomService.enterRoom(Integer.parseInt(roomId) , username) , "房间不存在");
            out(routingContext, Json.encodePrettily(true));
        } catch (Exception e) {
            routingContext.fail(400,e);
        }
    }

    private void room(RoutingContext routingContext) {
        try {
            String json = routingContext.getBody().toString();
            Room room = GsonUtils.jsonToBean(json, Room.class);
            BizCheckUtils.check(room != null && StringUtils.isNotBlank(room.getName()),"Invalid input");
            out(routingContext, Json.encodePrettily(roomRepository.saveRoom(room.getName())));
        } catch (Exception e) {
            routingContext.fail(400,e);
        }
    }

    private void msgSend(RoutingContext routingContext) {
        try {
            String json = routingContext.getBody().toString();
            MessageRetrive message = GsonUtils.jsonToBean(json,MessageRetrive.class);
            String username = routingContext.request().getParam("username");
            BizCheckUtils.check(messageService.sendMessage(username,message.getId(),message.getText()), "Invalid input");

            // 模拟service调用
            out(routingContext, Json.encodePrettily(true));
        }catch (Exception e){
            routingContext.fail(400,e);
        }
    }

    private void getMsgList(RoutingContext routingContext) {
        try {
            String json = routingContext.getBody().toString();
            QueryControlData queryControlData = GsonUtils.jsonToBean(json,QueryControlData.class);
            String username = routingContext.request().getParam("username");

            BizCheckUtils.check(queryControlData.getPageIndex() < 0 && queryControlData.getPageSize()>=0,"无效输入");

//            UserDto userDto = userRepository.queryUser(username);
            UserDto userDto = null;
            BizCheckUtils.check(userDto != null && userDto.getRoomId() > 0,"Invalid input");

            List<MessageRetrive> messageRetrives = messageService.pullMessage(userDto.getRoomId() , queryControlData);
            out(routingContext, Json.encodePrettily(messageRetrives));
        }catch (Exception e){
            routingContext.fail(400,e);
        }
        // 模拟service调用

    }

    private void updateCluster(RoutingContext routingContext){
        String json = routingContext.getBody().toString();
        EventBus bus = vertx.eventBus();
        bus.<Boolean>request(RedisVerticle.UPDATE_CLUSTER_ADD, json, reply -> {
            if( reply.succeeded() ){
                boolean success = reply.result().body().booleanValue();
                out(routingContext, Json.encodePrettily(success));
            }
        });
    }

    private void test(RoutingContext routingContext){
        Redis redis = Redis.createClient(
                vertx,
                new RedisOptions()
                        .setType(RedisClientType.SENTINEL)
                        .addConnectionString("redis://10.63.5.164:26379")
                        .addConnectionString("redis://10.63.5.164:26380")
                        .addConnectionString("redis://10.63.5.164:26381")
                        .setMasterName("mymaster")
                        .setRole(RedisRole.MASTER)
                        .setMaxPoolSize(8)
                        .setMaxWaitingHandlers(8));

        RedisAPI api = RedisAPI.api(redis);
        api.get("test").onSuccess(value->{
            System.out.println("---------"+Thread.currentThread().getName());
            System.out.println(value);
            out(routingContext, value.toString());
        });
    }

    public static void out(RoutingContext ctx, String value) {
        try {
            ctx.response().putHeader("Content-Type", "application/json; charset=utf-8").end(value);
        }catch (Exception ignore){
        }
    }

    public static void sendError(RoutingContext ctx, String msg){
        ctx.response().setStatusCode(400).end(msg);
    }

}

