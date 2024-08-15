package com.shaoteemo.zookeeper.practice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 所属项目：middleware
 * 所属包：com.shaoteemo.zookeeper.practice
 *
 * @author ShaoTeemo
 * @since 1.0
 */
class RmiClientTest {

    /**
     * 测试模拟RPC调用
     *
     */
    @Test
    void rmiClient() throws MalformedURLException, NotBoundException, RemoteException {
        RmiClient client = new RmiClient();
        client.rmiClient(1, 2);
    }
}