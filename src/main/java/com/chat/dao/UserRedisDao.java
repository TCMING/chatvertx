package com.chat.dao;

import com.chat.Main;
import com.chat.handler.UserHandler;
import com.chat.model.UserDto;
import com.chat.utils.BizCheckUtils;
import com.chat.utils.GsonUtils;
import com.chat.verticle.RedisVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserRedisDao {

    private static final Logger logger = LoggerFactory.getLogger(RedisVerticle.class);

    private RedisAPI redisAPI;

    private String userListKey = "userList";

    private String userMapKey = "UserMapKey";

    private String userIdKey = "UserIdKey";

    public UserRedisDao(RedisAPI redisAPI) {
        BizCheckUtils.checkNull(redisAPI,"DAO初始化redisAPI不能为空");
        this.redisAPI = redisAPI;
        this.baseOperate();
    }

    public void baseOperate() {
        EventBus bus = Main.vertx.eventBus();

        //查询单个用户id
        bus.<Integer>consumer(UserHandler.REDIS_USER_ID_GET).handler(msg ->{
            //默认增加 1,其实可以先申请若干个,缓存起来减少申请次数,暂时不做修改
            redisAPI.incrby(userIdKey, "1", res -> {
                try {
                    if (res.succeeded() && res.result() != null && res.result().type() == ResponseType.NUMBER) {
                        Integer userId = res.result().toInteger();
                        logger.info("查询用户id完成,userid={} ", userId );
                        msg.reply(userId);
                    } else {
                        logger.info("查询用户id失败 ");
                        msg.reply(-1);
                    }
                } catch (Exception e) {
                    msg.fail(400, e.getMessage());
                }
            });
        });

        //查询单个用户
        bus.<String>consumer(UserHandler.REDIS_USER_SINGLE_QUERY).handler(msg ->{
            String username = msg.body();

            redisAPI.hget(userMapKey, username, res -> {
                try {
                    if (res.succeeded() && res.result() != null && res.result().type() == ResponseType.BULK) {
                        String value = res.result().toString();
                        logger.info("查询用户信息完成,value={} ", value);
                        msg.reply(GsonUtils.jsonToBean(value, UserDto.class));
                    } else {
                        logger.info("查询用户信息失败 ");
                        msg.reply(null);
                    }
                } catch (Exception e) {
                    msg.fail(400, e.getMessage());
                }
            });
        });

        //保存用户
        bus.<UserDto>consumer(UserHandler.REDIS_USER_CREATE).handler(msg ->{
            UserDto userDto = msg.body();

            List<String> args = Stream.of(userMapKey, userDto.getUsername(), GsonUtils.toJsonString(userDto)).collect(Collectors.toList());
            redisAPI.hset(args, res -> {
                try {
                    if (res.succeeded() && res.result() != null &&  res.result().type() == ResponseType.NUMBER) {
                        logger.info("保存用户信息完成 " + userDto.getUsername());
                        msg.reply(true);
                    } else {
                        logger.info("保存用户信息失败 " + userDto.getUsername());
                        msg.reply(false);
                    }
                } catch (Exception e) {
                    msg.fail(400, e.getMessage());
                }
            });

        });

    }


}
