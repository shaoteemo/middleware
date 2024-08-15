package com.shaoteemo.zookeeper.handler;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 所属项目：middleware
 * 所属包：com.shaoteemo.zookeeper.handler
 * Zookeeper事件通知处理器
 *
 * @author ShaoTeemo
 * @since 1.0
 */
public class WatchHandler implements Watcher {

    private final Logger log = LoggerFactory.getLogger(WatchHandler.class);

    @Override
    public void process(WatchedEvent event) {

        //获取连接事件
        Event.KeeperState state = event.getState();

        switch (state){
            case SyncConnected:
                log.info("Zookeeper连接成功！");
                break;
            case Closed:
            case Expired:
                log.error("连接断开，Session失效！");
        }

    }
}
