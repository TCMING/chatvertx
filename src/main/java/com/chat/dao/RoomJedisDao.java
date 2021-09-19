package com.chat.dao;

import com.chat.Main;
import com.chat.handler.RoomHandler;
import com.chat.model.QueryControlData;
import com.chat.model.RoomDto;
import com.chat.utils.GsonUtils;
import com.chat.utils.RedisClientUtil;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.chat.utils.JedisSentinelPools.getJedis;
import static com.chat.utils.JedisSentinelPools.returnResource;

public class RoomJedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private final String RoomDtoList = "RoomDtoList";

    private final String IdRoomMap = "IdRoomMap";

    private final String RoomIdKey = "RoomIdKey";

    public RoomJedisDao() {
        this.baseOperate();
    }

    private void roomIdInit(Message msg){
        Jedis jedis = getJedis();
        try {
            long userId = jedis.incrBy(RoomIdKey, 1);
            returnResource(jedis);
            msg.reply(userId);
        } catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            roomIdInit(msg);
        }
    }

    private void saveRoomidName(Message msg , RoomDto roomDto){
        Jedis jedis = getJedis();
        try {
            long res = jedis.hset(IdRoomMap, String.valueOf(roomDto.getId()), roomDto.getName());
            returnResource(jedis);
            if (res == 1) {
                logger.info("保存房间信息完成 " + roomDto.getId());
            } else {
                logger.info("保存房间信息已存在 " + roomDto.getId());
            }
            msg.reply(true);
        }catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            saveRoomidName(msg , roomDto);
        }
    }

    private void queryRommidName(Message msg ){
        Jedis jedis = getJedis();
        try{
            String roomname = jedis.hget(IdRoomMap, (String) msg.body());
            returnResource(jedis);
            logger.info("查询房间信息完成,key={} ", msg.body());
            msg.reply(roomname);
        } catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            queryRommidName(msg);
        }
    }

    private void saveRoomList(Message msg , RoomDto roomDto){
        Jedis jedis = getJedis();
        try{
            long res = jedis.rpush(RoomDtoList, GsonUtils.toJsonString(roomDto));
            returnResource(jedis);
            logger.info("保存房间信息完成 " + roomDto.getId());
            msg.reply(true);
        } catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            saveRoomList(msg , roomDto);
        }
    }

    private void queryRoomList(Message msg , QueryControlData controlData){
        Jedis jedis = getJedis();
        try {
            int startIndex = (controlData.getPageIndex()) * controlData.getPageSize();
            int pageSize = controlData.getPageSize() + startIndex - 1;

            List<String> rooms = jedis.lrange(RoomDtoList, startIndex, pageSize);
            returnResource(jedis);
            List<RoomDto> roomDtos = new ArrayList<>();
            for (String str : rooms  ) {
                roomDtos.add(GsonUtils.jsonToBean(str , RoomDto.class));
            }
            String res = GsonUtils.toJsonString(roomDtos);
            logger.info("查询房间列表完成 value={};", res);
            msg.reply(res);
        } catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            queryRoomList(msg , controlData);
        }
    }

    private void addRoomidUsername(Message msg , List<String> saveInfo){
        Jedis jedis = getJedis();
        try {
            long res = jedis.sadd(saveInfo.get(0), saveInfo.get(1));
            returnResource(jedis);
            logger.info("房间保存用户信息完成 value={};" + GsonUtils.toJsonString(saveInfo));
            msg.reply(res == 1);
        } catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            addRoomidUsername(msg , saveInfo);
        }
    }

    private void sremRoomidUsername(Message msg , List<String> saveInfo){
        Jedis jedis = getJedis();
        try {
            long res = jedis.srem(saveInfo.get(0), saveInfo.get(1));
            returnResource(jedis);
            logger.info("房间移除用户信息完成 value={};" + GsonUtils.toJsonString(saveInfo));
            msg.reply(true);
        } catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception");
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            sremRoomidUsername(msg , saveInfo);
        }
    }

    private void smembersRoomidUsername(Message msg , String roomid){
        Jedis jedis = getJedis();
        logger.info("--room users:"+Thread.currentThread().getName());
        try {
            Set<String> users = jedis.smembers(roomid);
            returnResource(jedis);
            logger.info("房间查询用户信息完成 value={};", roomid);
            msg.reply(GsonUtils.toJsonString(users));
        } catch (JedisConnectionException ce){
            logger.error("-- jedis connection exception" , ce);
            jedis.close();
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                logger.error("-- InterruptedException" , ie);;
            }
            smembersRoomidUsername(msg , roomid);
        }
    }

    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        bus.<Integer>consumer(RoomHandler.REDIS_ROOM_ID_INIT).handler(msg ->{
            //默认增加 1,其实可以先申请若干个,缓存起来减少申请次数,暂时不做修改
            try {
                roomIdInit(msg);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存房间id-name关系
        bus.<RoomDto>consumer(RoomHandler.REDIS_ROOM_ID_NAME_SAVE).handler(msg ->{
            RoomDto roomDto = msg.body();
            try {
                saveRoomidName(msg , roomDto);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //查询单个房间信息
        bus.<String>consumer(RoomHandler.REDIS_ROOM_ID_NAME_QUERY).handler(msg -> {
            try {
                queryRommidName(msg);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存房间 dtoList 信息
        bus.<RoomDto>consumer(RoomHandler.REDIS_ROOM_DTO_LIST_SAVE).handler(msg ->{
            RoomDto roomDto = msg.body();
            try {
                saveRoomList(msg , roomDto);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //查询房间 dtoList 信息
        bus.<QueryControlData>consumer(RoomHandler.REDIS_ROOM_DTO_LIST_QUERY).handler(msg -> {
            QueryControlData controlData = msg.body();
            try {
                queryRoomList(msg , controlData);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存 roomid -> username 添加成员
        bus.<List<String>>consumer(RoomHandler.REDIS_ROOM_ID_USER_NAME_SADD).handler(msg -> {
            List<String> saveInfo = msg.body();
            try {
                addRoomidUsername(msg , saveInfo);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存 roomid -> username 移除成员
        bus.<List<String>>consumer(RoomHandler.REDIS_ROOM_ID_USER_NAME_SREM).handler(msg -> {
            List<String> saveInfo = msg.body();
            try {
                sremRoomidUsername(msg , saveInfo);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });

        //保存 roomid -> username 查询所有成员 TODO 调试过程中 调整返回结果
        bus.<String>consumer(RoomHandler.REDIS_ROOM_ID_USER_NAME_SMEMBERS).handler(msg -> {
            String roomid = msg.body();
            try {
                smembersRoomidUsername(msg , roomid);
            } catch (Exception e) {
                msg.fail(400, e.getMessage());
            }
        });
    }
}
