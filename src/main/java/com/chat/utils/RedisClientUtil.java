package com.chat.utils;

import com.chat.Main;
import io.vertx.redis.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class RedisClientUtil {

    private static Logger logger = LoggerFactory.getLogger(RedisClientUtil.class);

    private static Redis redisClient;

    private static RedisAPI redisAPI;

    private static String[] serverIpsStatic;

    private static String lock_key = "redis_lock";

    private static String isServerInited = "isServerInited";

    static Properties prop = new Properties();

    public static void init(){

//        初始化主从关系
        Redis slaveClient1 = Redis.createClient(
                Main.vertx,
            new RedisOptions()
                    .setType(RedisClientType.STANDALONE)
                    .addConnectionString("redis://10.63.5.164:6380")
//                        .addConnectionString("redis://"+serverIpsStatic[1]+"6379")
            );
        slaveClient1
            .connect()
            .onSuccess(conn -> {
                conn.send(Request.cmd(Command.SLAVEOF).arg("10.63.5.164").arg("6379"))
    //                    conn.send(Request.cmd(Command.SLAVEOF).arg(serverIpsStatic[0]).arg("6379"))
                        .onSuccess(info -> {
                            // do something...
                            System.out.println("----set slave1");
                            slaveClient1.close();
                        });
            });

        Redis slaveClient2 = Redis.createClient(
                Main.vertx,
            new RedisOptions()
                    .setType(RedisClientType.STANDALONE)
                    .addConnectionString("redis://10.63.5.164:6381")
//                        .addConnectionString("redis://"+serverIpsStatic[2]+"6379")
            );
        slaveClient2
            .connect()
            .onSuccess(conn -> {
                conn.send(Request.cmd(Command.SLAVEOF).arg("10.63.5.164").arg("6379"))
//                    conn.send(Request.cmd(Command.SLAVEOF).arg(serverIpsStatic[0]).arg("6379"))
                        .onSuccess(info -> {
                            // do something...
                            System.out.println("----set slave2");
                            slaveClient2.close();
                        });
            });

//        初始化哨兵，指定受监控的主节点
        Redis sentiClient1 = Redis.createClient(
                Main.vertx,
            new RedisOptions()
                    .setType(RedisClientType.STANDALONE)
                    .addConnectionString("redis://10.63.5.164:26379")
//                        .addConnectionString("redis://"+serverIpsStatic[0]+":26379")
            );
        sentiClient1
            .connect()
            .onSuccess(conn -> {
                conn.send(Request.cmd(Command.SENTINEL).arg("monitor")
                        .arg("mymaster")
                        .arg("10.63.5.164")
//                            .arg(serverIpsStatic[0])
                        .arg("6379")
                        .arg("2"))
                        .onSuccess(info -> {
                            // do something...
                            System.out.println("----set sentinel1");
                            sentiClient1.close();
                        });
            });

        System.out.println("-----test asyn4");
        Redis sentiClient2 = Redis.createClient(
                Main.vertx,
            new RedisOptions()
                    .setType(RedisClientType.STANDALONE)
                    .addConnectionString("redis://10.63.5.164:26380")
//                        .addConnectionString("redis://"+serverIpsStatic[1]+":26379")
            );
        sentiClient2
            .connect()
            .onSuccess(conn -> {
                conn.send(Request.cmd(Command.SENTINEL).arg("monitor")
                        .arg("mymaster")
                        .arg("10.63.5.164")
//                            .arg(serverIpsStatic[0])
                        .arg("6379")
                        .arg("2"))
                        .onSuccess(info -> {
                            // do something...
                            System.out.println("----set sentinel2");
                            sentiClient2.close();
                        });
            });

        Redis sentiClient3 = Redis.createClient(
                Main.vertx,
            new RedisOptions()
                    .setType(RedisClientType.STANDALONE)
                    .addConnectionString("redis://10.63.5.164:26381")
//                        .addConnectionString("redis://"+serverIpsStatic[2]+":26379")
            );
        sentiClient3
            .connect()
            .onSuccess(conn -> {
                conn.send(Request.cmd(Command.SENTINEL).arg("monitor")
                        .arg("mymaster")
                        .arg("10.63.5.164")
//                            .arg(serverIpsStatic[0])
                        .arg("6379")
                        .arg("2"))
                        .onSuccess(info -> {
                            // do something...
                            System.out.println("----set sentinel3");
                            sentiClient3.close();
                        });
            });
    }

    //只受updateCluster调用
    //需要获取分布式锁
    public static boolean initRedisServer(String json){
        String[] serverIps= json.split(",");
        String[] ipsNew = convertIp(serverIps);
        serverIpsStatic = ipsNew;
        //保存ip到redis.properties,为了重启后初始化高可用客户端
        FileOutputStream oFile = null;//true表示追加打开
        try {
            oFile = new FileOutputStream("redis.properties", false);
            prop.setProperty("ips", json);
            prop.store(oFile, "The redis properties file");
            oFile.close();
        } catch (IOException e) {
        }

        //初始化redis server
        Redis lockClient = Redis.createClient(
                Main.vertx,
                new RedisOptions()
                        .setType(RedisClientType.STANDALONE)
                        .addConnectionString("redis://10.63.5.164:6381")
//                        .addConnectionString("redis://"+serverIps[0]+"6379")
        );
        RedisAPI lockAPI = RedisAPI.api(lockClient);

        //获取分布式锁，使用单一redis节点，需要在初始化主从、哨兵架构前完成
        List args = new ArrayList<String>();
        args.add(lock_key);
        String setValue = UUID.randomUUID().toString();
        args.add(setValue);
        args.add("NX");
        args.add("EX");
        args.add(String.valueOf(60*60*1));

        lockAPI.set(args).onSuccess(value->{
            logger.info("----获取锁成功");
            List isInitedArgs = new ArrayList<String>();
            isInitedArgs.add(isServerInited);
            lockAPI.exists(isInitedArgs).onSuccess(exsist->{
                if(exsist.toString().equals("0")){
                    // TODO: 2021/8/31 注释了初始化redis server
                    //init();
                    //标记已完成哨兵架构初始化
                    isInitedArgs.add("1");
                    lockAPI.set(isInitedArgs).onSuccess(value3 ->{
                        //释放分布式锁
                        ArrayList delArgs = new ArrayList<String>();
                        String script =
                                "if redis.call('get',KEYS[1]) == ARGV[1] then" +
                                        "   return redis.call('del',KEYS[1]) " +
                                        "else" +
                                        "   return 0 " +
                                        "end";
                        delArgs.add(script);
                        delArgs.add("1");
                        delArgs.add(lock_key);
                        delArgs.add(setValue);
                        lockAPI.eval(delArgs).onSuccess(value2->{
                            logger.info("---删除锁成功");
                            lockAPI.close();
                        });
                    });
                }else{
                    ArrayList delArgs = new ArrayList<String>();
                    String script =
                            "if redis.call('get',KEYS[1]) == ARGV[1] then" +
                                    "   return redis.call('del',KEYS[1]) " +
                                    "else" +
                                    "   return 0 " +
                                    "end";
                    delArgs.add(script);
                    delArgs.add("1");
                    delArgs.add(lock_key);
                    delArgs.add(setValue);
                    lockAPI.eval(delArgs).onSuccess(value2->{
                        logger.info("---删除锁成功");
                        lockAPI.close();
                    });
                }
            });

        });

        return true;
    }


    /**
     * 初始化高可用客户端
     * @return
     */
    public static void initRedisClient(){
        //重新加载serverIpsStatic
//        if (serverIpsStatic == null) {
//            File file = new File("redis.properties");
//            try {
//                prop.load(new FileInputStream(file));
//                String json = prop.getProperty("ips");
//                String[] ips = json.split(",");
//                serverIpsStatic = convertIp(ips);
//            } catch (IOException e) {
//                logger.error("--error", e);
//            }
//        }
//        BizCheckUtils.checkNull(serverIpsStatic,"serverIpsStatic初始化失败");
        //初始化RedisClient
        redisClient = Redis.createClient(
                Main.vertx,
                new RedisOptions()
                        .setType(RedisClientType.SENTINEL)
                        .addConnectionString("redis://10.63.5.164:26379")
                        .addConnectionString("redis://10.63.5.164:26380")
                        .addConnectionString("redis://10.63.5.164:26381")
//                            .addConnectionString("redis://"+serverIpsStatic[0]+":26379")
//                            .addConnectionString("redis://"+serverIpsStatic[1]+":26379")
//                            .addConnectionString("redis://"+serverIpsStatic[2]+":26379")
                        .setMasterName("mymaster")
                        .setRole(RedisRole.MASTER)
                        .setPoolCleanerInterval(-1)
                        .setPoolRecycleTimeout(120000)
                        .setMaxPoolSize(8)
                        .setMaxWaitingHandlers(8));

        redisAPI = RedisAPI.api(redisClient);
        BizCheckUtils.checkNull(redisAPI,"redisAPI初始化失败");
    }

    public static String[] convertIp(String[] serverIps){
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
        }
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
        return ipsNew;
    }

    public static RedisAPI getRedisAPI() {
        BizCheckUtils.checkNull(redisAPI,"redisApi未初始化完成");
        return redisAPI;
    }
}
