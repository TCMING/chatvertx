package com.chat.utils;

import io.vertx.core.Vertx;
import io.vertx.redis.client.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class RedisClientUtil {

    public static Redis redisClient;

    private static Vertx vertxStatic;

    private static String[] serverIpsStatic;

    public static void init(){

//        初始化主从关系
        Redis.createClient(
                vertxStatic,
                new RedisOptions()
                        .setType(RedisClientType.STANDALONE)
                        .addConnectionString("redis://127.0.0.1:6380")
//                        .addConnectionString("redis://"+serverIpsStatic[1]+"6379")
                )
                .connect()
                .onSuccess(conn -> {
                    conn.send(Request.cmd(Command.SLAVEOF).arg("127.0.0.1").arg("6379"))
//                    conn.send(Request.cmd(Command.SLAVEOF).arg(serverIpsStatic[0]).arg("6379"))
                            .onSuccess(info -> {
                                // do something...
                                System.out.println("----set slave1");
                            });
                });

        Redis.createClient(
                vertxStatic,
                new RedisOptions()
                        .setType(RedisClientType.STANDALONE)
                        .addConnectionString("redis://127.0.0.1:6381")
//                        .addConnectionString("redis://"+serverIpsStatic[2]+"6379")
                )
                .connect()
                .onSuccess(conn -> {
                    conn.send(Request.cmd(Command.SLAVEOF).arg("127.0.0.1").arg("6379"))
//                    conn.send(Request.cmd(Command.SLAVEOF).arg(serverIpsStatic[0]).arg("6379"))
                            .onSuccess(info -> {
                                // do something...
                                System.out.println("----set slave2");
                            });
                });

//        初始化哨兵，指定受监控的主节点
        Redis.createClient(
                vertxStatic,
                new RedisOptions()
                        .setType(RedisClientType.STANDALONE)
                        .addConnectionString("redis://127.0.0.1:26379")
//                        .addConnectionString("redis://"+serverIpsStatic[0]+":26379")
                )
                .connect()
                .onSuccess(conn -> {
                    conn.send(Request.cmd(Command.SENTINEL).arg("monitor")
                            .arg("mymaster")
                            .arg("127.0.0.1")
//                            .arg(serverIpsStatic[0])
                            .arg("6379")
                            .arg("2"))
                            .onSuccess(info -> {
                                // do something...
                                System.out.println("----set sentinel1");
                            });
                });

        Redis.createClient(
                vertxStatic,
                new RedisOptions()
                        .setType(RedisClientType.STANDALONE)
                        .addConnectionString("redis://127.0.0.1:26380")
//                        .addConnectionString("redis://"+serverIpsStatic[1]+":26379")
                )
                .connect()
                .onSuccess(conn -> {
                    conn.send(Request.cmd(Command.SENTINEL).arg("monitor")
                            .arg("mymaster")
                            .arg("127.0.0.1")
//                            .arg(serverIpsStatic[0])
                            .arg("6379")
                            .arg("2"))
                            .onSuccess(info -> {
                                // do something...
                                System.out.println("----set sentinel2");
                            });
                });

        Redis.createClient(
                vertxStatic,
                new RedisOptions()
                        .setType(RedisClientType.STANDALONE)
                        .addConnectionString("redis://127.0.0.1:26381")
//                        .addConnectionString("redis://"+serverIpsStatic[2]+":26379")
                )
                .connect()
                .onSuccess(conn -> {
                    conn.send(Request.cmd(Command.SENTINEL).arg("monitor")
                            .arg("mymaster")
                            .arg("127.0.0.1")
//                            .arg(serverIpsStatic[0])
                            .arg("6379")
                            .arg("2"))
                            .onSuccess(info -> {
                                // do something...
                                System.out.println("----set sentinel3");
                            });
                });
    }

    public static String[] initRedis(Vertx vertx , String[] serverIps){
        try {
            InetAddress addr = InetAddress.getLocalHost();
            String localIp = addr.getHostAddress();
            String[] ipsNew  = new String[3];
            ipsNew[0] = localIp;
            int i = 1;
            for(String ip : serverIps){
                if(i<3 && !ip.equals(localIp)){
                    ipsNew[i] = ip;
                    i++;
                }
            }
            vertxStatic = vertx;
            serverIpsStatic = ipsNew;
            init();

        } catch (UnknownHostException e) {

        }
        return serverIpsStatic;
    }

    /**
     * 初始化原始客户端
     * @param vertx
     * @param serverIps
     * @return
     */
    public static Redis initRedisClient(Vertx vertx , String[] serverIps){
        if(redisClient != null){
            return redisClient;
        }else{
            synchronized (RedisClientUtil.class){
                if(redisClient == null){
                    //        初始化RedisClient
                    redisClient = Redis.createClient(
                            vertxStatic,
                            new RedisOptions()
                            .setType(RedisClientType.SENTINEL)
                            .addConnectionString("redis://127.0.0.1:26379")
                            .addConnectionString("redis://127.0.0.1:26380")
                            .addConnectionString("redis://127.0.0.1:26381")

//                            .addConnectionString("redis://"+serverIpsStatic[0]+":26379")
//                            .addConnectionString("redis://"+serverIpsStatic[1]+":26379")
//                            .addConnectionString("redis://"+serverIpsStatic[2]+":26379")
                            .setMasterName("mymaster")
                            .setRole(RedisRole.MASTER)
                            .setMaxPoolSize(8)
                            .setMaxWaitingHandlers(8));

                    return redisClient;
                }
            }
        }
        return redisClient;

    }


}
