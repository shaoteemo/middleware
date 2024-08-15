package com.shaoteemo.zookeeper.practice.service.impl;

import com.shaoteemo.zookeeper.service.RmiMethodService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * 所属项目：middleware
 * 所属包：com.shaoteemo.zookeeper.practice.service.impl
 * Rmi提供远程调用具体实现类。
 *
 * @author ShaoTeemo
 * @since 1.0
 */
public class RmiMethodServiceImpl extends UnicastRemoteObject implements RmiMethodService {

    public RmiMethodServiceImpl() throws RemoteException {
    }

    @Override
    public int cul(int arg1, int arg2) throws RemoteException {
        return arg1 + arg2;
    }
}
