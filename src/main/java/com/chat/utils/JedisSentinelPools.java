package com.chat.utils;

import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JedisSentinelPools {

    private volatile static JedisSentinelPool pool = null;

    private volatile static JedisPool localPool = null;

    private static String[] serverIpsStatic;

    private static JedisPoolConfig config = new JedisPoolConfig();

    private static String masterName = "mymaster";

    private static Set<String> sentinels = new HashSet<String>();

    //可用连接实例的最大数目，默认为8；
    //如果赋值为-1，则表示不限制，如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)
    private static Integer MAX_TOTAL = 100;
    //控制一个pool最多有多少个状态为idle(空闲)的jedis实例，默认值是8
    private static Integer MAX_IDLE = 8;
    //等待可用连接的最大时间，单位是毫秒，默认值为-1，表示永不超时。
    //如果超过等待时间，则直接抛出JedisConnectionException
    private static Integer MAX_WAIT_MILLIS = 10000;
    //客户端超时时间配置
    private static Integer TIMEOUT = 10000;
    //在borrow(用)一个jedis实例时，是否提前进行validate(验证)操作；
    //如果为true，则得到的jedis实例均是可用的
    private static Boolean TEST_ON_BORROW = true;
    //在空闲时检查有效性, 默认false
    private static Boolean TEST_WHILE_IDLE = true;
    //是否进行有效性检查
    private static Boolean TEST_ON_RETURN = true;

    public static Jedis getJedis() {
        if (pool == null) {
            synchronized (JedisSentinelPools.class) {
                if (pool == null) {
                    initPool();
                }
            }
        }
        return pool.getResource();
    }

    public static Jedis getLocalJedis() {
        if (localPool == null) {
            synchronized (JedisSentinelPools.class) {
                if (localPool == null) {
                    initLocalPool();
                }
            }
        }
        return localPool.getResource();
    }

    private static void initLocalPool(){
        config.setMaxTotal(MAX_TOTAL);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT_MILLIS);
        config.setTestOnBorrow(TEST_ON_BORROW);
        config.setTestWhileIdle(TEST_WHILE_IDLE);
        config.setTestOnReturn(TEST_ON_RETURN);
        localPool = new JedisPool(config, "127.0.0.1", 6379, TIMEOUT);
    }

    private static void initPool() {

        if (serverIpsStatic == null) {
            Jedis jedis = new Jedis("127.0.0.1" , 6379);
//            Jedis jedis = new Jedis("47.94.19.223", 6379);
            String json = jedis.get("ips");
            jedis.close();
            serverIpsStatic = convertIp(json);
        }

        config.setMaxTotal(MAX_TOTAL);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT_MILLIS);
        config.setTestOnBorrow(TEST_ON_BORROW);
        config.setTestWhileIdle(TEST_WHILE_IDLE);
        config.setTestOnReturn(TEST_ON_RETURN);

        sentinels.add(new HostAndPort(serverIpsStatic[0], 26379).toString());
        sentinels.add(new HostAndPort(serverIpsStatic[1], 26379).toString());
        sentinels.add(new HostAndPort(serverIpsStatic[2], 26379).toString());
        pool = new JedisSentinelPool(masterName, sentinels, config, TIMEOUT);
    }

    public static String[] convertIp(String json) {
        List<String> ipList = GsonUtils.jsonToList(json, String.class);
        String[] serverIps = new String[3];
        for (int index = 0; index < 3; index++) {
            serverIps[index] = ipList.get(index);
        }
        return serverIps;
    }

    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    public static void returnLocalResource(Jedis jedis) {
        localPool.returnResource(jedis);
    }
}
