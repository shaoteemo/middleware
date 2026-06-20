package com.shaoteemo.util;

import redis.clients.jedis.Jedis;

/**
 * Create Info:
 * <br>Change Info:
 * <br>Create On 2023/8/8 10:55
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
public class JedisUtil {

    private static final String URL = "192.168.2.20";

    private static final int PORT = 6379;

    public static Jedis getConnection() {
        return new Jedis(URL, PORT);
    }
}
