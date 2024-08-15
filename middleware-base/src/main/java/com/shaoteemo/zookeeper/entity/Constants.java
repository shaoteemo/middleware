package com.shaoteemo.zookeeper.entity;

/**
 * 所属项目：middleware
 * 所属包：com.shaoteemo.zookeeper.entity
 *
 * @author ShaoTeemo
 * @since 1.0
 */
public class Constants {

    /*Zookeeper连接信息*/
    public static final String HOST = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";

    /*Zookeeper连接超时时间*/
    public static final int TIME_OUT = 2000000;

    /*RmiServerPort*/
    public static final int RMI_SERVER_PORT = 8081;

    /*RmiName*/
    public static final String RMI_NAME_URL = "rmi://127.0.0.1:"+ RMI_SERVER_PORT + "/RmiMethodService";

}
