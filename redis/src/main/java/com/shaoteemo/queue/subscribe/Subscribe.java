package com.shaoteemo.queue.subscribe;

import com.shaoteemo.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

/**
 * Create Info: 订阅端。消费者
 * 此方式比list性能更好，但消息可能因为外部原因丢失，如宕机，网络等
 * <br>Change Info:
 * <br>Create On 2023/8/8 10:49
 *
 * @author XiaoMo
 * @since 0.1-alpha
 */
@Slf4j
public class Subscribe extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        log.info(channel);
        log.info("New msg received: {}", message);
    }

    public static void main(String[] args) {
        try (Jedis connection = JedisUtil.getConnection()){
            connection.subscribe(new Subscribe(), "msg");
        }
    }


}
