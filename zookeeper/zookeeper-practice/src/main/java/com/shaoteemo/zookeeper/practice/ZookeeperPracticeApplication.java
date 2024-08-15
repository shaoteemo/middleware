package com.shaoteemo.zookeeper.practice;

import com.shaoteemo.zookeeper.entity.Constants;
import com.shaoteemo.zookeeper.handler.WatchHandler;
import com.shaoteemo.zookeeper.practice.service.impl.RmiMethodServiceImpl;
import com.shaoteemo.zookeeper.service.RmiMethodService;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * 所属项目：middleware
 * 所属包：com.shaoteemo.zookeeper.practice
 *
 * @author ShaoTeemo
 * @since 1.0
 */
@SpringBootApplication
public class ZookeeperPracticeApplication {

    private final Logger logger = LoggerFactory.getLogger(ZookeeperPracticeApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ZookeeperPracticeApplication.class, args);
    }

    /**
     * 定义一个rmi服务器
     */
    @Bean
    public void rmiServer() throws RemoteException, MalformedURLException, AlreadyBoundException {
        //实例化"Object"
        RmiMethodService service = new RmiMethodServiceImpl();

        //绑定对象映射至注册表
        Registry registry = LocateRegistry.createRegistry(Constants.RMI_SERVER_PORT);// 创建注册表

        /*
         * name : "rmi://[服务端HOST]:[端口]/[唯一的名称];
         */
        Naming.bind(Constants.RMI_NAME_URL, service);

        //将url信息存放至zookeeper中
        this.putRmiUrlInfo(RmiMethodService.class.getSimpleName(), Constants.RMI_NAME_URL);

        logger.info("RMI Server启动成功！");
    }

    /**
     * 将服务注册至Zookeeper
     *
     * @param path 存放路径
     * @param data 存放数据
     */
    private void putRmiUrlInfo(String path, String data) {
        path = "/" + path;
        try {
            ZooKeeper zooKeeper = new ZooKeeper(Constants.HOST, Constants.TIME_OUT, new WatchHandler());
            while (zooKeeper.exists(path, null) != null) {
                logger.error("{} 服务已存在，10秒后尝试重新发布！", path);
                Thread.sleep(10000);
            }
            zooKeeper.create(path, data.getBytes(StandardCharsets.UTF_8), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            logger.info("{}服务已发布！", path);
        } catch (IOException | KeeperException | InterruptedException e) {
            logger.error("", e);
        }
    }

}
