package com.shaoteemo;

import com.shaoteemo.util.JedisUtil;
import com.shaoteemo.util.LockUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.params.SetParams;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Create Info:
 * <br>Change Info:
 * <br>Create On 2023/8/6 10:23
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */

@SpringBootTest
@SpringBootConfiguration
@DisplayName("Jedis")
@Slf4j
public class JedisTest {

    private static final String URL = "192.168.2.20";

    private static final int PORT = 6379;

    /*单机版*/
    @Test
    public void singleConnect() {
        Jedis jedis = new Jedis(URL, PORT);
        log.info("get msg:{}", jedis.ping());

        /*String*/
        log.info("Add String key 'my:key' : {} ", jedis.set("my:key", "1"));
        log.info("Get String key 'my:key' : {} ", jedis.get("my:key"));
        log.info("increment String key 'my:key' : {} ", jedis.incr("my:key"));
        //获取所有key
        log.info("Get all key : {} ", jedis.keys("*"));
        // 设置过期时间及成功设置不存在的Key
        log.info("Set String key 'my:key' NX EX : {} ", jedis.set("my:key1", "3", SetParams.setParams().nx().ex(5L)));
        log.info("Get String key 'my:key' TTL : {} ", jedis.ttl("my:key1"));

        /*List*/
        log.info("Add List key 'my:list' : {} ", jedis.rpush("my:list", "1", "2", "3", "4", "5", "6", "7"));
        log.info("Get List key 'my:list' : {} ", jedis.lrange("my:list", 0, -1));

        /*Set*/
        log.info("Add Set key 'my:set' : {} ", jedis.sadd("my:set", "1", "2", "2", "3", "4", "6"));
        log.info("Get Set key 'my:set' : {} ", jedis.smembers("my:set"));

        /*Hash*/
        log.info("Add Hash key 'my:hash' : {} ", jedis.hset("my:hash", "name", "ZhangSan"));
        log.info("Get Hash key 'my:hash' : {} ", jedis.hget("my:hash", "name"));
        log.info("Get Hash key 'my:hash' values : {} ", jedis.hvals("my:hash"));

        /*ZSet*/
        log.info("Add ZSet key 'my:zset' : {} ", jedis.zadd("my:zset", 1.2, "1"));
        log.info("Get ZSet key 'my:zset' : {} ", jedis.zrange("my:zset", 0, -1));

        /*Bitmaps*/
        log.info("Add Bitmaps key 'my:bitmaps' : {} ", jedis.setbit("my:bitmaps", 0, true));
        log.info("Get Bitmaps key 'my:bitmaps' : {} ", jedis.getbit("my:bitmaps", 0));

        /*Geospatial*/
        log.info("Add Geospatial key 'my:geospatial' : {} ", jedis.geoadd("my:geospatial", 105, 25, "N/A"));
        log.info("Get Geospatial key 'my:geospatial' : {} ", jedis.geopos("my:geospatial", "N/A"));

        /*Hyperloglog*/
        log.info("Add Hyperloglog key 'my:hyperloglog' : {} ", jedis.pfadd("my:hyperloglog", "java", "C++", "php", "R"));
        log.info("Get Hyperloglog key 'my:hyperloglog' : {} ", jedis.pfcount("my:hyperloglog"));
        jedis.close();
    }

    @Test
    /*集群版*/
    public void clusterConnect() {
        /*声明一个集群中的IP*/
        HostAndPort server = new HostAndPort("", 6379);
        /*连接任意集群IP即可*/
        JedisCluster cluster = new JedisCluster(server);

        /*String*/
        log.info("Add String key 'my:key' : {} ", cluster.set("my:key", "1"));
        log.info("Get String key 'my:key' : {} ", cluster.get("my:key"));
        log.info("increment String key 'my:key' : {} ", cluster.incr("my:key"));
        //获取所有key
        log.info("Get all key : {} ", cluster.keys("*"));
        // 设置过期时间及成功设置不存在的Key
        log.info("Set String key 'my:key' NX EX : {} ", cluster.set("my:key1", "3", SetParams.setParams().nx().ex(5L)));
        log.info("Get String key 'my:key' TTL : {} ", cluster.ttl("my:key1"));

        /*List*/
        log.info("Add List key 'my:list' : {} ", cluster.rpush("my:list", "1", "2", "3", "4", "5", "6", "7"));
        log.info("Get List key 'my:list' : {} ", cluster.lrange("my:list", 0, -1));

        /*Set*/
        log.info("Add Set key 'my:set' : {} ", cluster.sadd("my:set", "1", "2", "2", "3", "4", "6"));
        log.info("Get Set key 'my:set' : {} ", cluster.smembers("my:set"));

        /*Hash*/
        log.info("Add Hash key 'my:hash' : {} ", cluster.hset("my:hash", "name", "ZhangSan"));
        log.info("Get Hash key 'my:hash' : {} ", cluster.hget("my:hash", "name"));
        log.info("Get Hash key 'my:hash' values : {} ", cluster.hvals("my:hash"));

        /*ZSet*/
        log.info("Add ZSet key 'my:zset' : {} ", cluster.zadd("my:zset", 1.2, "1"));
        log.info("Get ZSet key 'my:zset' : {} ", cluster.zrange("my:zset", 0, -1));

        /*Bitmaps*/
        log.info("Add Bitmaps key 'my:bitmaps' : {} ", cluster.setbit("my:bitmaps", 0, true));
        log.info("Get Bitmaps key 'my:bitmaps' : {} ", cluster.getbit("my:bitmaps", 0));

        /*Geospatial*/
        log.info("Add Geospatial key 'my:geospatial' : {} ", cluster.geoadd("my:geospatial", 105, 25, "N/A"));
        log.info("Get Geospatial key 'my:geospatial' : {} ", cluster.geopos("my:geospatial", "N/A"));

        /*Hyperloglog*/
        log.info("Add Hyperloglog key 'my:hyperloglog' : {} ", cluster.pfadd("my:hyperloglog", "java", "C++", "php", "R"));
        log.info("Get Hyperloglog key 'my:hyperloglog' : {} ", cluster.pfcount("my:hyperloglog"));

        cluster.close();
    }


    /*分布式锁测试*/
    @Test
    void lock() {
        log.info(LockUtil.tryLock(10));
    }

    /*消息队列-消费者端。服务端使用lpush则消费端应使用rpop反之亦然。*/
    @Test
    void queueConsumer() {
        Jedis jedis = new Jedis(URL, PORT);
        log.info("Redis queue listening ...");
        while (true) {
            /*阻塞式监听防止循环资源浪费*/
            List<String> msg = jedis.brpop(0, "msg");
            log.info("New msg received: {}", msg);
            if (msg.isEmpty()) break;
        }
        jedis.close();
    }

    /*pipeline。类似于JDBC中的批量执行*/
    @Test
    void pipeline() {
        Jedis jedis = JedisUtil.getConnection();
        Pipeline pipelined = jedis.pipelined();
        for (int i = 0; i < 10000; i++) {
            pipelined.set("key:" + i, "v" + i);
        }
        log.info("Error count: {}" , pipelined.syncAndReturnAll().stream().filter(item -> !"OK".equals(item)).count());
        log.info("Current key count in database: {}" , jedis.dbSize());
        /*清空缓存*/
        jedis.flushDB();
    }

}
