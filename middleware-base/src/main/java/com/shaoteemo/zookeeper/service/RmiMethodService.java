package com.shaoteemo.zookeeper.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 所属项目：middleware
 * 所属包：com.shaoteemo.zookeeper
 * 定义提供远程调用的接口
 * @author ShaoTeemo
 * @since 1.0
 */
public interface RmiMethodService extends Remote {

    /*加法操作,此方法提供远程调用，因此需要抛异常RemoteException(Server端必须这么做。Client端可以不用)*/
    int cul(int arg1 , int arg2) throws RemoteException;

}
