package com.shaoteemo.queue.list;

import com.shaoteemo.util.JedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Scanner;

/**
 * Create Info: 消息队列-生产端。服务端使用lpush则消费端应使用rpop反之亦然。
 * 消费者在单元测试
 * <br>Change Info:
 * <br>Create On 2023/8/8 10:38
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
public class QueueProvider {

    private static final Logger log = LoggerFactory.getLogger(QueueProvider.class);

    public static void main(String[] args) {
        try (Jedis jedis = JedisUtil.getConnection()) {
            log.info("Redis queue listening ...");
            Scanner sc = new Scanner(System.in);
            while (true) {
                /*阻塞式监听防止循环资源浪费*/
                String msg = sc.next();
                jedis.lpush("msg", msg);

            }

        }
    }
}