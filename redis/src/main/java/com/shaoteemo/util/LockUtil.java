package com.shaoteemo.util;

import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.UUID;

/**
 * Create Info: Redis分布式锁
 * <br>Change Info:
 * <br>Create On 2023/8/8 9:47
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
@Slf4j
public final class LockUtil {

    private static final String LOCK_PREFIX = "tm:lock";

    private static final long TIMEOUT = 10;

    public static String tryLock(long timeout) {
        try (Jedis jedis = new Jedis("192.168.2.20", 6379)) {
            return jedis.set(LOCK_PREFIX, UUID.randomUUID().toString(), SetParams.setParams().ex(timeout).nx());
        } catch (Exception e) {
            log.error("分布式锁获取失败", e);
        }
        return null;
    }

    private LockUtil() {
    }
}
