//package com.chat.dao;
//
//import com.chat.Main;
//import com.chat.handler.RoomHandler;
//import com.chat.handler.UserHandler;
//import com.chat.model.QueryControlData;
//import com.chat.model.RoomDto;
//import com.chat.model.UserDto;
//import com.chat.utils.GsonUtils;
//import com.chat.utils.RedisClientUtil;
//import com.chat.verticle.RedisVerticle;
//import io.vertx.core.eventbus.EventBus;
//import io.vertx.redis.client.RedisAPI;
//import io.vertx.redis.client.ResponseType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//public class RoomRedisDao {
//
//    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);
//
//    private RedisAPI redisAPI;
//
//    private final String RoomDtoList = "RoomDtoList";
//
//    private final String IdRoomMap = "IdRoomMap";
//
//    private final String RoomIdKey = "RoomIdKey";
//
//    @Deprecated
//    public RoomRedisDao(RedisAPI redisAPI) {
//        this.redisAPI = redisAPI;
//        this.baseOperate();
//    }
//
//    public RoomRedisDao() {
//        this.baseOperate();
//    }
//
//    public void baseOperate() {
//        EventBus bus = Main.vertx.eventBus();
//
//        bus.<Integer>consumer(RoomHandler.REDIS_ROOM_ID_INIT).handler(msg ->{
//            //默认增加 1,其实可以先申请若干个,缓存起来减少申请次数,暂时不做修改
//            RedisClientUtil.getRedisAPI().incrby(RoomIdKey, "1", res -> {
//                try {
//                    if (res.succeeded() && res.result() != null && res.result().type() == ResponseType.NUMBER) {
//                        Integer userId = res.result().toInteger();
//                        logger.info("查询房间id完成,userid={} ", userId );
//                        msg.reply(userId);
//                    } else {
//                        logger.info("查询房间id失败 ");
//                        msg.reply(-1);
//                    }
//                } catch (Exception e) {
//                    msg.fail(400, e.getMessage());
//                }
//            });
//        });
//
//        //保存房间id-name关系
//        bus.<RoomDto>consumer(RoomHandler.REDIS_ROOM_ID_NAME_SAVE).handler(msg ->{
//            RoomDto roomDto = msg.body();
//            List<String> args = Stream.of(IdRoomMap, String.valueOf(roomDto.getId()), roomDto.getName()).collect(Collectors.toList());
//            RedisClientUtil.getRedisAPI().hset(args, res -> {
//                try {
//                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.NUMBER) {
//                        if(res.result().toInteger() == 1){
//                            logger.info("保存房间信息完成 " + roomDto.getId());
//                        }else{
//                            logger.info("保存房间信息已存在 " + roomDto.getId());
//                        }
//                        msg.reply(true);
//                    } else {
//                        logger.info("保存房间信息失败 " + roomDto.getId());
//                        msg.reply(false);
//                    }
//                } catch (Exception e) {
//                    msg.fail(400, e.getMessage());
//                }
//            });
//        });
//
//        //查询单个房间信息
//        bus.<String>consumer(RoomHandler.REDIS_ROOM_ID_NAME_QUERY).handler(msg ->{
//            RedisClientUtil.getRedisAPI().hget(IdRoomMap, msg.body(), res -> {
//                try {
//                    if (res.succeeded() && res.result() != null && res.result().type() == ResponseType.BULK) {
//                        String roomname = res.result().toString();
//                        logger.info("查询房间信息完成,key={} ", msg.body());
//                        msg.reply(roomname);
//                    } else {
//                        logger.info("查询房间信息失败,key={} ", msg.body());
//                        msg.reply(null);
//                    }
//                } catch (Exception e) {
//                    msg.fail(400, e.getMessage());
//                }
//            });
//        });
//
//        //保存房间 dtoList 信息
//        bus.<RoomDto>consumer(RoomHandler.REDIS_ROOM_DTO_LIST_SAVE).handler(msg ->{
//            RoomDto roomDto = msg.body();
//            List<String> args = Stream.of(RoomDtoList, GsonUtils.toJsonString(roomDto)).collect(Collectors.toList());
//            RedisClientUtil.getRedisAPI().rpush(args, res -> {
//                try {
//                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.NUMBER) {
//                        logger.info("保存房间信息完成 " + roomDto.getId());
//                        msg.reply(true);
//                    } else {
//                        logger.info("保存房间信息失败 " + roomDto.getId());
//                        msg.reply(false);
//                    }
//                } catch (Exception e) {
//                    msg.fail(400, e.getMessage());
//                }
//            });
//        });
//
//        //查询房间 dtoList 信息
//        bus.<QueryControlData>consumer(RoomHandler.REDIS_ROOM_DTO_LIST_QUERY).handler(msg ->{
//            QueryControlData controlData = msg.body();
//            int startIndex = (controlData.getPageIndex()) * controlData.getPageSize();
//            int pageSize = controlData.getPageSize() + startIndex - 1;
//
//            RedisClientUtil.getRedisAPI().lrange(RoomDtoList,String.valueOf(startIndex), String.valueOf(pageSize), res -> {
//                try {
//                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.MULTI) {
//                        logger.info("查询房间列表完成 value={};" + res.result().toString());
//                        //List<RoomDto> roomDtos = GsonUtils.jsonToList(res.result().toString(),RoomDto.class);
//                        msg.reply(res.result().toString());
//                    } else {
//                        logger.info("查询房间列表失败");
//                        msg.reply(null);
//                    }
//                } catch (Exception e) {
//                    msg.fail(400, e.getMessage());
//                }
//            });
//        });
//
//        //保存 roomid -> username 添加成员
//        bus.<List<String>>consumer(RoomHandler.REDIS_ROOM_ID_USER_NAME_SADD).handler(msg ->{
//            List<String> saveInfo = msg.body();
//            RedisClientUtil.getRedisAPI().sadd(saveInfo, res -> {
//                        try {
//                            if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.NUMBER) {
//                                logger.info("房间保存用户信息完成 value={};" + GsonUtils.toJsonString(saveInfo));
//                                msg.reply(true);
//                            } else {
//                                logger.info("房间保存用户信息失败 value={}" + GsonUtils.toJsonString(saveInfo));
//                                msg.reply(false);
//                            }
//                        } catch (Exception e) {
//                            msg.fail(400, e.getMessage());
//                        }
//                    });
//        });
//        //保存 roomid -> username 移除成员
//        bus.<List<String>>consumer(RoomHandler.REDIS_ROOM_ID_USER_NAME_SREM).handler(msg ->{
//            List<String> saveInfo = msg.body();
//            RedisClientUtil.getRedisAPI().srem(saveInfo, res -> {
//                try {
//                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.NUMBER) {
//                        logger.info("房间移除用户信息完成 value={};" + GsonUtils.toJsonString(saveInfo));
//                        msg.reply(true);
//                    } else {
//                        logger.info("房间移除用户信息失败 value={}" + GsonUtils.toJsonString(saveInfo));
//                        msg.reply(false);
//                    }
//                } catch (Exception e) {
//                    msg.fail(400, e.getMessage());
//                }
//            });
//        });
//        //保存 roomid -> username 查询所有成员 TODO 调试过程中 调整返回结果
//        bus.<String>consumer(RoomHandler.REDIS_ROOM_ID_USER_NAME_SMEMBERS).handler(msg ->{
//            String roomid = msg.body();
//            RedisClientUtil.getRedisAPI().smembers(roomid, res -> {
//                try {
//                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.MULTI) {
//                        logger.info("房间查询用户信息完成 value={};" + roomid);
//                        msg.reply(res.result().toString());
//                    } else {
//                        logger.info("房间查询用户信息失败 value={}" + roomid);
//                        msg.reply(null);
//                    }
//                } catch (Exception e) {
//                    msg.fail(400, e.getMessage());
//                }
//            });
//        });
//
//    }
//
//
//}
