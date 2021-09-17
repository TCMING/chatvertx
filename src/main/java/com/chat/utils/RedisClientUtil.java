package com.chat.utils;

import com.chat.Main;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.redis.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.*;

public class RedisClientUtil {

    private static Logger logger = LoggerFactory.getLogger(RedisClientUtil.class);

    private static volatile RedisAPI redisAPI;

    private static RedisConnection connection;

    private static String[] serverIpsStatic;

    private static String lock_key = "redis_lock";

    private static String isServerInited = "isServerInited";

    private static final int MAX_RECONNECT_RETRIES = 16;

    public static void init(RedisAPI lockAPI , String setValue , String json){

//        初始化主从关系
        Redis slaveClient1 = Redis.createClient(
                Main.vertx,
            new RedisOptions()
                    .setType(RedisClientType.STANDALONE)
                    //.addConnectionString("redis://39.107.249.226:6379")
                    //.addConnectionString("redis://127.0.0.1:6380")
                    .addConnectionString("redis://"+serverIpsStatic[1]+":6379")
            );
        slaveClient1
            .connect()
                .onFailure(f2->{
                    logger.error("",f2.getCause().getStackTrace());
                })
            .onSuccess(conn -> {
                //conn.send(Request.cmd(Command.SLAVEOF).arg("123.56.115.153").arg("6379"))
                //conn.send(Request.cmd(Command.SLAVEOF).arg("127.0.0.1").arg("6379"))
                conn.send(Request.cmd(Command.SLAVEOF).arg(serverIpsStatic[0]).arg("6379"))
                        .onFailure(fail->{
                            logger.error("",fail.getCause());
                        })
                        .onSuccess(info -> {
                            // do something...
                            System.out.println("----set slave1");

                            //slave2-----------------------------------------
                            Redis slaveClient2 = Redis.createClient(
                                    Main.vertx,
                                    new RedisOptions()
                                            .setType(RedisClientType.STANDALONE)
                                            //.addConnectionString("redis://39.105.154.114:6379")
                                            //.addConnectionString("redis://127.0.0.1:6381")
                                            .addConnectionString("redis://"+serverIpsStatic[2]+":6379")
                            );
                            slaveClient2
                                    .connect()
                                    .onSuccess(conn2 -> {
                                        //conn2.send(Request.cmd(Command.SLAVEOF).arg("123.56.115.153").arg("6379"))
                                        //conn.send(Request.cmd(Command.SLAVEOF).arg("127.0.0.1").arg("6379"))
                                        conn2.send(Request.cmd(Command.SLAVEOF).arg(serverIpsStatic[0]).arg("6379"))
                                                .onSuccess(info2 -> {
                                                    // do something...
                                                    System.out.println("----set slave2");

                                                    //初始化哨兵，指定受监控的主节点
                                                    //senti1---------------------------------
                                                    Redis sentiClient1 = Redis.createClient(
                                                            Main.vertx,
                                                            new RedisOptions()
                                                                    .setType(RedisClientType.STANDALONE)
                                                                    //.addConnectionString("redis://39.107.249.226:26379")
                                                                    //.addConnectionString("redis://127.0.0.1:26379")
                                                                    .addConnectionString("redis://"+serverIpsStatic[0]+":26379")
                                                    );
                                                    sentiClient1
                                                            .connect()
                                                            .onSuccess(conn3 -> {
                                                                conn3.send(Request.cmd(Command.SENTINEL).arg("monitor")
                                                                        .arg("mymaster")
                                                                        //.arg("123.56.115.153")
                                                                        //.arg("127.0.0.1")
                                                                        .arg(serverIpsStatic[0])
                                                                        .arg("6379")
                                                                        .arg("2"))
                                                                        .onSuccess(info3 -> {
                                                                            // do something...
                                                                            System.out.println("----set sentinel1");

                                                                            //senti2-----------------------
                                                                            Redis sentiClient2 = Redis.createClient(
                                                                                    Main.vertx,
                                                                                    new RedisOptions()
                                                                                            .setType(RedisClientType.STANDALONE)
                                                                                            //.addConnectionString("redis://39.105.154.114:26379")
                                                                                            //.addConnectionString("redis://127.0.0.1:26380")
                                                                                            .addConnectionString("redis://"+serverIpsStatic[1]+":26379")
                                                                            );
                                                                            sentiClient2
                                                                                    .connect()
                                                                                    .onSuccess(conn4 -> {
                                                                                        conn4.send(Request.cmd(Command.SENTINEL).arg("monitor")
                                                                                                .arg("mymaster")
                                                                                                //.arg("123.56.115.153")
                                                                                                //.arg("127.0.0.1")
                                                                                                .arg(serverIpsStatic[0])
                                                                                                .arg("6379")
                                                                                                .arg("2"))
                                                                                                .onSuccess(info4 -> {
                                                                                                    // do something...
                                                                                                    System.out.println("----set sentinel2");


                                                                                                    //senti3------------------
                                                                                                    Redis sentiClient3 = Redis.createClient(
                                                                                                            Main.vertx,
                                                                                                            new RedisOptions()
                                                                                                                    .setType(RedisClientType.STANDALONE)
                                                                                                                    //.addConnectionString("redis://123.56.115.153:26379")
                                                                                                                    //.addConnectionString("redis://127.0.0.1:26381")
                                                                                                                    .addConnectionString("redis://"+serverIpsStatic[2]+":26379")
                                                                                                    );
                                                                                                    sentiClient3
                                                                                                            .connect()
                                                                                                            .onSuccess(conn5 -> {
                                                                                                                conn5.send(Request.cmd(Command.SENTINEL).arg("monitor")
                                                                                                                        .arg("mymaster")
                                                                                                                        //.arg("123.56.115.153")
                                                                                                                        //.arg("127.0.0.1")
                                                                                                                        .arg(serverIpsStatic[0])
                                                                                                                        .arg("6379")
                                                                                                                        .arg("2"))
                                                                                                                        .onSuccess(info5 -> {
                                                                                                                            // do something...
                                                                                                                            System.out.println("----set sentinel3");
                                                                                                                            try {
                                                                                                                                Thread.sleep(10000);
                                                                                                                            } catch (InterruptedException e) {
                                                                                                                                logger.error("--",e);
                                                                                                                            }

                                                                                                                            //标记已完成哨兵架构初始化
                                                                                                                            lockAPI.set(Arrays.asList(isServerInited,"1")).onSuccess(value3 ->{
                                                                                                                                //保存ip到主redis，即serverIpsStatic[0]，为了重启后初始化高可用客户端
                                                                                                                                lockAPI.set(Arrays.asList("ips", json)).onSuccess(value4 -> {
                                                                                                                                    logger.info("---ips save successed");
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

                                                                                                                                        logger.info("---删除锁成功，配置哨兵主观下线timeout");

                                                                                                                                        conn3.send(Request.cmd(Command.SENTINEL).arg("set")
                                                                                                                                                .arg("mymaster")
                                                                                                                                                .arg("down-after-milliseconds")
                                                                                                                                                .arg("1000"))
                                                                                                                                                .onFailure(f1->{
                                                                                                                                                    logger.error("--"+f1.getCause());
                                                                                                                                                })
                                                                                                                                                .onSuccess(timeoutInfo1->{
                                                                                                                                                    System.out.println("timeoutInfo1 set");

                                                                                                                                                    conn4.send(Request.cmd(Command.SENTINEL).arg("set")
                                                                                                                                                            .arg("mymaster")
                                                                                                                                                            .arg("down-after-milliseconds")
                                                                                                                                                            .arg("1000"))
                                                                                                                                                            .onSuccess(timeoutInfo2->{
                                                                                                                                                                System.out.println("timeoutInfo2 set");

                                                                                                                                                                conn5.send(Request.cmd(Command.SENTINEL).arg("set")
                                                                                                                                                                        .arg("mymaster")
                                                                                                                                                                        .arg("down-after-milliseconds")
                                                                                                                                                                        .arg("1000"))
                                                                                                                                                                        .onSuccess(timeoutInfo3->{
                                                                                                                                                                            System.out.println("timeoutInfo3 set");

                                                                                                                                                                            logger.info("---开始关闭连接");
                                                                                                                                                                            sentiClient3.close();
                                                                                                                                                                            sentiClient2.close();
                                                                                                                                                                            sentiClient1.close();
                                                                                                                                                                            slaveClient2.close();
                                                                                                                                                                            slaveClient1.close();
                                                                                                                                                                            lockAPI.close();
                                                                                                                                                                        });
                                                                                                                                                            });
                                                                                                                                                });


                                                                                                                                    });
                                                                                                                                });
                                                                                                                            });

                                                                                                                        });
                                                                                                            });
                                                                                                });
                                                                                    });
                                                                        });
                                                            });

                                                });

                                    });
                        });
            });
    }

    //只受updateCluster调用
    //需要获取分布式锁
    public static boolean initRedisServer(String json){

        serverIpsStatic = convertIp(json);

        //保存ip到redis.properties,为了重启后初始化高可用客户端
        /*FileOutputStream oFile = null;//true表示追加打开
        try {
            oFile = new FileOutputStream("redis.properties", false);
            prop.setProperty("ips", json);
            prop.store(oFile, "The redis properties file");
            oFile.close();
        } catch (IOException e) {
        }*/

        //初始化redis server
        Redis lockClient = Redis.createClient(
                Main.vertx,
                new RedisOptions()
                        .setType(RedisClientType.STANDALONE)
                        //.addConnectionString("redis://123.56.115.153:6379")
                        //.addConnectionString("redis://127.0.0.1:6381")
                        .addConnectionString("redis://"+serverIpsStatic[0]+":6379")
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

        lockAPI.set(args)
                .onFailure(fa->{
                    fa.toString();
                })
                .onSuccess(value->{
            logger.info("----获取锁成功");
            List isInitedArgs = new ArrayList<String>();
            isInitedArgs.add(isServerInited);
            lockAPI.exists(isInitedArgs).onSuccess(exsist->{
                if(exsist.toString().equals("0")){
                    // TODO: 2021/8/31 注释了初始化redis server
                    //初始化redis server核心逻辑
                    init(lockAPI , setValue , json);
                }else{
                    logger.info("----need not to init");
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
    public static void initRedisClient(Handler<AsyncResult<RedisConnection>> handler){
        //重新加载serverIpsStatic
        if (serverIpsStatic == null) {
            // TODO: 2021/9/11 采用jedis访问redis
            Jedis jedis = new Jedis("127.0.0.1" , 6379);
//            Jedis jedis = new Jedis("101.201.79.214" , 6379);
            String json = jedis.get("ips");
            jedis.close();
            serverIpsStatic = convertIp(json);
        }
        BizCheckUtils.checkNull(serverIpsStatic,"serverIpsStatic初始化失败");
        //初始化RedisClient
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Redis.createClient(Main.vertx,
                        new RedisOptions()
                                .setType(RedisClientType.SENTINEL)
        //                      .addConnectionString("redis://127.0.0.1:26379")
        //                      .addConnectionString("redis://127.0.0.1:26380")
        //                      .addConnectionString("redis://127.0.0.1:26381")
                                .addConnectionString("redis://" + serverIpsStatic[0] + ":26379")
                                .addConnectionString("redis://" + serverIpsStatic[1] + ":26379")
                                .addConnectionString("redis://" + serverIpsStatic[2] + ":26379")
                                .setMasterName("mymaster")
                                .setRole(RedisRole.MASTER)
                                .setPoolCleanerInterval(-1)
                                .setPoolRecycleTimeout(120000)
                                .setMaxPoolSize(8)
                                .setMaxWaitingHandlers(32)).connect(onConnect ->{
                    if (onConnect.succeeded()) {
                        connection = onConnect.result();
                        // make sure the client is reconnected on error
                        connection.exceptionHandler(e -> {
                            // attempt to reconnect
                            attemptReconnect(0);
                        });
                    }
                    // allow further processing
                    handler.handle(onConnect);
                });
            }
        });
        thread.start();
    }

    /**
     * Attempt to reconnect up to MAX_RECONNECT_RETRIES
     */
    private static void attemptReconnect(int retry) {
        logger.info("第" + retry + "次尝试重连 Redis");
        // retry with backoff up to 10240 ms
        long backoff = (long) (Math.pow(2, Math.min(retry, 10)) * 10);
        Main.vertx.setTimer(backoff, timer -> initRedisClient(onReconnect -> {
            if (onReconnect.succeeded()) {
               redisAPI = RedisAPI.api(onReconnect.result());
            } else if (onReconnect.failed()) {
                attemptReconnect(retry+1);
            }
        }));
    }

    public static RedisAPI getRedisAPI(){
        if(redisAPI != null){
            return redisAPI;
        }else{
            synchronized (RedisClientUtil.class){
                if(redisAPI == null){
                    initRedisClient(onCreate -> {
                        if (onCreate.succeeded()) {
                            redisAPI = RedisAPI.api(onCreate.result());
                            logger.info("Redis 连接成功！");
                        }else if(onCreate.failed()) {
                            logger.error("Redis 连接失败！");
                        }
                    });
                    while(redisAPI == null){
                        try {
                            Thread.sleep(10);
                        }catch (Exception e){
                        }
                    }

                    // 本地客户端
//                    SingleRedisClient singleRedisClient = new SingleRedisClient();
//                    RedisConnection connection = singleRedisClient.init();
//                    redisAPI = RedisAPI.api(connection);
                }
            }
            return redisAPI;
        }
    }

    public static void setRedisAPI(RedisAPI newRedisAPI){
        redisAPI = newRedisAPI;
    }

    public static String[] convertIp(String json){
        List<String> ipList = GsonUtils.jsonToList(json , String.class);
        String[] serverIps = new String[3];
        for(int index=0 ; index < 3 ; index++){
            serverIps[index] = ipList.get(index);
        }
        return serverIps;
    }
}
