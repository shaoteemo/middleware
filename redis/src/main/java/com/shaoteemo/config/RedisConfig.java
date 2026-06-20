package com.shaoteemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;

/**
 * Create Info: 不同的序列化方式
 * <br>Change Info:
 * <br>Create On 2023/8/7 11:03
 * 常见的Redis序列化器：
 * 1.简单的字符串序列化器{@link org.springframework.data.redis.serializer.StringRedisSerializer}
 * 2.可以将任何对象泛化为字符串并序列化(对象必须有无参构造方法){@link org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer}
 * 3.序列化java对象（需要序列化的类必须实现Serializable接口){@link org.springframework.data.redis.serializer.JdkSerializationRedisSerializer}
 * 4.序列化对象（序列化带泛型的数据时，会以map的结构进行存储,反序列化是不能将map解析成对象){@link org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer}
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
@Configuration
public class RedisConfig {

    /**
     * Redis Template装配
     *
     * @param connectionFactory 连接工厂
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

}
