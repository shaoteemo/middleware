package com.shaoteemo;

import com.shaoteemo.config.RedisConfig;
import com.shaoteemo.dto.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.Objects;

/**
 * Create Info:
 * <br>Change Info:
 * <br>Create On 2023/8/7 10:45
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
@DataRedisTest
@ContextConfiguration(classes = {RedisConfig.class})
@EnableAutoConfiguration
@DisplayName("spring-data-redis")
@Slf4j
public class SpringDataRedisTest {

    @Resource
    private RedisTemplate<String, Serializable> redisTemplate;

    /*单机*/
    @Test
    void testRedis() {
        User user = new User(12, "lisi");
        /*String*/
        redisTemplate.opsForValue().set("string:user", user);
        User userValue = (User) redisTemplate.opsForValue().get("string:user");
        log.info("get key string:user -> {}", userValue);
        /*List*/
        redisTemplate.opsForList();

        /*Hash*/
        redisTemplate.opsForHash();

        /*Set、ZSet、Geo、Hyperloglog、Bitmaps同上...*/

        /*清空测试数据*/
        redisTemplate.delete(Objects.requireNonNull(redisTemplate.keys("*")));
    }
}