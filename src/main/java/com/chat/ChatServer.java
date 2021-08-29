package com.chat;

import com.chat.model.*;
import com.chat.repository.MessageRepository;
import com.chat.repository.RoomRepository;
import com.chat.repository.UserRepository;
import com.chat.service.MessageService;
import com.chat.service.RoomService;
import com.chat.service.UserService;
import com.chat.utils.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

class ChatServer extends AbstractVerticle {

    private UserService userService;
    private RoomService roomService;
    private MessageService messageService;
    private MessageRepository messageRepository;
    private RoomRepository roomRepository;
    private UserRepository userRepository;

    public static void main(String[] args){
        BeanFactory.init();
        Vertx.vertx().deployVerticle(new ChatServer());
        // int loopNum = 8;
        // VertxOptions vo = new VertxOptions();
        // vo.setEventLoopPoolSize(loopNum);
        // Vertx vertx = Vertx.vertx(vo);
        // for(int i=0; i<loopNum; i++)
        //     vertx.deployVerticle(new ChatServer());
    }

    public ChatServer() {
        this.messageRepository = BeanFactory.getInstance(MessageRepository.class);
        this.roomRepository = BeanFactory.getInstance(RoomRepository.class);
        this.userRepository = BeanFactory.getInstance(UserRepository.class);
        this.userService = BeanFactory.getInstance(UserService.class);
        this.roomService = BeanFactory.getInstance(RoomService.class);
        this.messageService = BeanFactory.getInstance(MessageService.class);
    }

    @Override
    public void start() throws Exception {
//        System.out.println("---------"+Thread.currentThread().getName());
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        // user
        router.post("/user").blockingHandler(this::addUser);
        router.get("/userLogin").blockingHandler(this::userLogin);
        router.get("/user/:username").blockingHandler(this::getUserInfo);

        //room
        router.post("/room").blockingHandler(this::room);
        router.put("/room/:roomId/enter").blockingHandler(this::roomEnter);
        router.put("/room/roomLeave").blockingHandler(this::roomLeave);
        router.get("/room/:roomId").blockingHandler(this::roomId);
        router.post("/roomList").blockingHandler(this::roomList);
        router.get("/room/:roomId/users").blockingHandler(this::roomUserList);

        // message
        router.post("/message/send").blockingHandler(this::msgSend);
        router.post("/message/retrieve").blockingHandler(this::getMsgList);
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

            UserDto userDto = userRepository.queryUser(username);
            BizCheckUtils.check(userDto != null && userDto.getRoomId() > 0,"Invalid input");

            List<MessageRetrive> messageRetrives = messageService.pullMessage(userDto.getRoomId() , queryControlData);
            out(routingContext, Json.encodePrettily(messageRetrives));
        }catch (Exception e){
            routingContext.fail(400,e);
        }
        // 模拟service调用

    }

    private void getUserInfo(RoutingContext routingContext) {
        String username = routingContext.request().getParam("username");

        try {
            BizCheckUtils.checkNull(username, "Invalid username supplied");
            UserDto userDto = userService.queryUserByName(username);
            BizCheckUtils.checkNull(userDto,"Invalid username supplied");
            UserResponse userResponse = new UserResponse(userDto.getFirstName(),userDto.getLastName(),
                    userDto.getEmail(),userDto.getPhone());
            out(routingContext, Json.encodePrettily(userResponse));
        }catch (Exception e){
            routingContext.fail(400,e);
        }

    }

    private void userLogin(RoutingContext routingContext) {
        try {
            String username = routingContext.request().getParam("username");
            String password = routingContext.request().getParam("password");
            System.out.println("userLogin " + username +" "+ password);
            BizCheckUtils.check(userService.userPasswordCheck(username,password),"Invalid username or password.");
            String jwtToken = JwtUtils.createToken(username);
            out(routingContext, Json.encodePrettily(jwtToken));
        }catch (Exception e){
            routingContext.fail(400,e);
        }
    }

    private void addUser(RoutingContext routingContext) {
        try {
            String json = routingContext.getBody().toString();
            UserRequest userRequest = GsonUtils.jsonToBean(json, UserRequest.class);
            BizCheckUtils.check(userService.registryUser(userRequest),"保存用户异常");
            // 模拟service调用
            out(routingContext, Json.encodePrettily(true));
        }catch (Exception e){
            routingContext.fail(400,e);
        }
    }

    private void out(RoutingContext ctx, String msg) {
        ctx.response().putHeader("Content-Type", "application/json; charset=utf-8").end(msg);
    }
}

