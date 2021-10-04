package com.shaoteemo.zookeeper.practice;

import com.shaoteemo.zookeeper.entity.Constants;
import com.shaoteemo.zookeeper.handler.WatchHandler;
import com.shaoteemo.zookeeper.service.RmiMethodService;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * 所属项目：middleware
 * 所属包：com.shaoteemo.zookeeper.practice
 * Rmi客户端
 *
 * @author ShaoTeemo
 * @date 2021/10/4
 * @since 1.0
 */
public class RmiClient {

    private final Logger logger = LoggerFactory.getLogger(RmiClient.class);

    /**
     * 一个测试rmiClient，在测试类中调用。
     */
    public void rmiClient(int arg1, int arg2) throws MalformedURLException, NotBoundException, RemoteException {
        RmiMethodService service = (RmiMethodService) Naming.lookup(getRmiUrl(RmiMethodService.class.getSimpleName()));
        int cul = service.cul(arg1, arg2);
        logger.info("服务端返回的计算结果：{}", cul);
    }

    private String getRmiUrl(String serviceBeanName) {
        try {
            ZooKeeper zooKeeper = new ZooKeeper(Constants.HOST, Constants.TIME_OUT, new WatchHandler());
            byte[] bytes = zooKeeper.getData("/" + serviceBeanName, new WatchHandler(), new Stat());
            String data = new String(bytes);
            logger.info("服务" + serviceBeanName + "的连接信息: {}", data);
            return data;
        } catch (IOException | KeeperException | InterruptedException e) {
            e.printStackTrace();
            logger.error("获取服务" + serviceBeanName + "的连接信息失败!");
            return "";
        }
    }

}
