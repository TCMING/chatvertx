package com.chat.dao;

import com.chat.Main;
import com.chat.handler.RoomHandler;
import com.chat.model.QueryControlData;
import com.chat.model.RoomDto;
import com.chat.utils.GsonUtils;
import com.chat.utils.RedisClientUtil;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chat.utils.JedisSentinelPools.getJedis;

public class RoomJedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private final String RoomDtoList = "RoomDtoList";

    private final String IdRoomMap = "IdRoomMap";

    private final String RoomIdKey = "RoomIdKey";

    public RoomJedisDao() {
        this.baseOperate();
    }

    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        bus.<Integer>consumer(RoomHandler.REDIS_ROOM_ID_INIT).handler(msg ->{
            //默认增加 1,其实可以先申请若干个,缓存起来减少申请次数,暂时不做修改
            try {
                long userId = getJedis().incrBy(RoomIdKey, 1);
                msg.reply(userId);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存房间id-name关系
        bus.<RoomDto>consumer(RoomHandler.REDIS_ROOM_ID_NAME_SAVE).handler(msg ->{
            try {
                RoomDto roomDto = msg.body();
                long res = getJedis().hset(IdRoomMap, String.valueOf(roomDto.getId()), roomDto.getName());
                if (res == 1) {
                    logger.info("保存房间信息完成 " + roomDto.getId());
                } else {
                    logger.info("保存房间信息已存在 " + roomDto.getId());
                }
                msg.reply(true);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //查询单个房间信息
        bus.<String>consumer(RoomHandler.REDIS_ROOM_ID_NAME_QUERY).handler(msg -> {
            try {
                String roomname = getJedis().hget(IdRoomMap, msg.body());
                logger.info("查询房间信息完成,key={} ", msg.body());
                msg.reply(roomname);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存房间 dtoList 信息
        bus.<RoomDto>consumer(RoomHandler.REDIS_ROOM_DTO_LIST_SAVE).handler(msg ->{
            try {
                RoomDto roomDto = msg.body();
                long res = getJedis().rpush(RoomDtoList, GsonUtils.toJsonString(roomDto));
                logger.info("保存房间信息完成 " + roomDto.getId());
                msg.reply(true);

            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //查询房间 dtoList 信息
        bus.<QueryControlData>consumer(RoomHandler.REDIS_ROOM_DTO_LIST_QUERY).handler(msg -> {
            try {
                QueryControlData controlData = msg.body();
                int startIndex = (controlData.getPageIndex()) * controlData.getPageSize();
                int pageSize = controlData.getPageSize() + startIndex - 1;

                List<String> rooms = getJedis().lrange(RoomDtoList, startIndex, pageSize);
                List<RoomDto> roomDtos = new ArrayList<>();
                for (String str : rooms  ) {
                    roomDtos.add(GsonUtils.jsonToBean(str , RoomDto.class));
                }
                String res = GsonUtils.toJsonString(roomDtos);
                logger.info("查询房间列表完成 value={};", res);
                msg.reply(res);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存 roomid -> username 添加成员
        bus.<List<String>>consumer(RoomHandler.REDIS_ROOM_ID_USER_NAME_SADD).handler(msg -> {
            try {
                List<String> saveInfo = msg.body();
                long res = getJedis().sadd(saveInfo.get(0), saveInfo.get(1));
                logger.info("房间保存用户信息完成 value={};" + GsonUtils.toJsonString(saveInfo));
                msg.reply(res == 1);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存 roomid -> username 移除成员
        bus.<List<String>>consumer(RoomHandler.REDIS_ROOM_ID_USER_NAME_SREM).handler(msg -> {
            List<String> saveInfo = msg.body();
            try {
                long res = getJedis().srem(saveInfo.get(0), saveInfo.get(1));
                logger.info("房间移除用户信息完成 value={};" + GsonUtils.toJsonString(saveInfo));
                msg.reply(true);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存 roomid -> username 查询所有成员 TODO 调试过程中 调整返回结果
        bus.<String>consumer(RoomHandler.REDIS_ROOM_ID_USER_NAME_SMEMBERS).handler(msg -> {
            try {
                String roomid = msg.body();
                Set<String> users = getJedis().smembers(roomid);
                logger.info("房间查询用户信息完成 value={};", roomid);
                msg.reply(GsonUtils.toJsonString(users));
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });
    }
}
