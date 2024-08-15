package com.shaoteemo.zookeeper;

import com.shaoteemo.zookeeper.entity.Constants;
import com.shaoteemo.zookeeper.handler.WatchHandler;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

/**
 * 所属项目：middleware
 * 所属包：com.shaoteemo.zookeeper
 * <p>
 * 本案例讲解ZNode的操作
 * 注意：有个能在运行测试类的时候会报错。如果为连接丢失、会话失效请重新运行。
 *
 * @author ShaoTeemo
 * @since 1.0
 */
public class ZNodeOperate {

    private final Logger log = LoggerFactory.getLogger(ZNodeOperate.class);

    /**
     * 创建一个ZNode
     */
    @Test
    public void createZNode() throws InterruptedException, KeeperException {
        ZooKeeper zkConn = getZookeeperConnection();
        /*
         * path: 路径
         * data：存放的数据
         * acl：权限
         * createModel：创建的节点类型
         * */
        String shaoteemo = zkConn.create("/my/test", getBytes("shaoteemo"), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        log.info("返回的消息：{}", shaoteemo);

    }

    /**
     * 获取指定节点的数据
     */
    @Test
    public void getZNodeData() throws InterruptedException, KeeperException {
        ZooKeeper zkConn = getZookeeperConnection();
        byte[] data = zkConn.getData("/my/test", getWatcher(), getStat());

        log.info("获取的节点（/my/test）数据：{}", getString(data));

    }

    /**
     * 获取指定节点的所有的子节点数据
     */
    @Test
    public void getZNodeAllChildData() throws InterruptedException, KeeperException {
        ZooKeeper zkConn = getZookeeperConnection();
        zkConn.getChildren("/my", getWatcher()).forEach(item -> {
            try {
                byte[] data = zkConn.getData("/my/" + item, getWatcher(), getStat());
                log.info("获取的节点{}数据：{}", "/my/" + item, getString(data));
            } catch (KeeperException | InterruptedException e) {
                log.error("", e);
            }
        });

    }

    /**
     * 获取节点状态信息
     */
    @Test
    public void getDataStat() {
        ZooKeeper zkConn = getZookeeperConnection();
        String result = null;
        Stat stat = new Stat();
        try {
            byte[] data = zkConn.getData("/itbaizhan", null, stat);
            result = new String(data);
        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
        }
        log.info("getData result------{{}}", result);
        log.info("getData stat------{{}}", stat);
    }

    /**
     * 获取子节点及数据
     */
    @Test
    public void getChildrenData() {
        try {
            ZooKeeper zkConn = getZookeeperConnection();
            List<String> childrenList = zkConn.getChildren("/", true);
            String data = null;
            //遍历子节点
            for (String child : childrenList) {
                data = new String(zkConn.getData("/" + child, null, null));
                log.info("child:{{}},value:{{}}", child, data);
            }
        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 设置ZNode的值
     */
    @Test
    public void setValueToZNode() throws InterruptedException, KeeperException {
        ZooKeeper zkConn = getZookeeperConnection();
        /*
         *  如果存在这样的节点并且给定版本与节点的版本匹配（如果给定版本为 -1，则它与任何节点的版本匹配），则为给定路径的节点设置数据。 返回节点的状态。
         *  path – 节点的路径
         *  data – 要设置的数据
         *  version – 预期的匹配版本(注：-1与任何节点的版本匹配)
         * */
        Stat shaoteemo_update = zkConn.setData("/my/test", getBytes("shaoteemo_update"), -1);
        log.info("返回的节点详细信息：{}", shaoteemo_update);
    }

    /**
     * 删除节点
     */
    @Test
    public void deleteZNode() throws InterruptedException, KeeperException {
        ZooKeeper zkConn = getZookeeperConnection();
        zkConn.delete("/my", -1);
        log.info("节点 /my 删除成功！");
    }

    /*获取连接信息*/
    private ZooKeeper getZookeeperConnection() {
        try {
            return new ZooKeeper(Constants.HOST, Constants.TIME_OUT, getWatcher());
        } catch (IOException e) {
            log.error("Create Connection error:", e);
            throw new RuntimeException();
        }
    }

    /*获取Zookeeper事件通知处理器*/
    private Watcher getWatcher() {
        return new WatchHandler();
    }

    byte[] getBytes(Object data) {
        return data.toString().getBytes();
    }

    String getString(byte[] data) {
        return new String(data);
    }

    /*
     * 获取Zookeeper统计信息对象
     * */
    private Stat getStat() {
        return new Stat();
    }

}
