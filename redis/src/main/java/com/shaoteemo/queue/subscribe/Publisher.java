package com.shaoteemo.queue.subscribe;

import com.shaoteemo.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.util.Scanner;

/**
 * Create Info: 消息发布（生产者）
 * <br>Change Info:
 * <br>Create On 2023/8/8 10:49
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
@Slf4j
public class Publisher {

    public static void main(String[] args) {
        try (Jedis jedis = JedisUtil.getConnection()) {
            log.info("Input msg...");
            Scanner sc = new Scanner(System.in);
            while (true) {
                String msg = sc.next();
                jedis.publish("msg", msg);

            }

        }
    }
}
