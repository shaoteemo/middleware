# Apache Zookeeper

## 写在前面

撰写人：ShaoTeemo

本文档包含了Zookeeper的使用和部分原理。其中包含大量官方文档引用及部分三方文档链接引用。后期会翻译部分zookeeper原理相关内容。

有较详细的项目搭建过程的文件信息，*所有内容均在指定的环境中测试运行通过*，请**注意环境信息的匹配**。

文中出现的译注为本人(译者)观点。

## 环境信息

|           技术栈            |            版本号             |
| :-------------------------: | :---------------------------: |
|         Spring-Boot         |             2.5.5             |
|          Zookeeper          |             3.7.0             |
|         Oracle JDK          |            11.0.12            |
| Linux（CentOS Base AliYun） | CentOS Linux release 8.4.2105 |
|            Maven            |             3.6.3             |

图形绘制：Microsoft Office Visio

## Zookeeper自述

ZooKeeper是一个分布式的，开放源码的分布式应用程序协调服务，是Google的Chubby一个开源的实现，是Hadoop和Hbase、Apache Solr 的重要组件 hadoop项目下的一个子项目。 项目下的一个子。 项。它是一个为分布式应用提供一致性服务的软件，提供的功能包括：配置维护、域名服务、分布式同步、组服务等。

它主要是用来解决分布式应用中经常遇到的一些数据管理问题，如：统一命名服务、状态同步服务、集群管理、分布式应用配置项的管理等。简单来说zookeeper=**文件系统+监听通知机制**。

## 环境组件及信息获取

[SpringBoot](https://spring.io/projects/spring-boot#samples)、[Apache Zookeeper](https://zookeeper.apache.org/releases.html)、[Apache Zookeeper Download（3.7.0）](https://www.apache.org/dyn/closer.lua/zookeeper/zookeeper-3.7.0/apache-zookeeper-3.7.0-bin.tar.gz)、[Apache Zookeeper Document（3.7.0）](https://zookeeper.apache.org/doc/r3.7.0/index.html)、[ZooKeeper Use Cases（zookeeper使用场景案例）](http://zookeeper.apache.org/doc/r3.7.0/zookeeperUseCases.html)

## 环境搭建细节

### 1.Zookeeper目录结构剖析

![](http://rep.shaoteemo.com/zk_content.png)

说明：data目录为自建目录；logs为运行后自动生成的目录。

### 2.安装及使用

#### 2.1前置条件

使用scp或其他工具上传`apache-zookeeper-3.7.0-bin.tar.gz`包、安装好JDK等基础环境：

```powershell
PS scp {file path} {username}@{host}:{remote path}
```

使用命令解压`apache-zookeeper-3.7.0-bin.tar.gz`：

```bash
bash tar -zxf apache-zookeeper-3.7.0-bin.tar.gz、
```

#### 2.2单机版（Standalone）

配置zookeeper：

1. 进入zookeeper目录，在目录中创建任意文件夹存放缓存数据。本文使用`data`

2. 在`conf`目录中将`zoo_sample.cfg`模板配置文件复制一份为`zoo.cfg`（zookeeper启动默认识别该目录下的配置文件名）。vim编辑`zoo.cfg`配置data目录。

   ```bash
   bash cp zoo_sample.cfg zoo.cfg
   ```

   ```properties
   # The number of milliseconds of each tick
   # ZooKeeper 使用的基本时间单位，以毫秒为单位。它用于进行心跳，最小会话超时将是tickTime 的两倍。
   tickTime=2000
   # The number of ticks that the initial 
   # synchronization phase can take
   initLimit=10
   # The number of ticks that can pass between 
   # sending a request and getting an acknowledgement
   syncLimit=5
   # the directory where the snapshot is stored.
   # do not use /tmp for storage, /tmp here is just 
   # example sakes.
   # 存储内存数据库快照的位置，除非另有说明，否则还有数据库更新的事务日志。
   dataDir=[此处填写创建的目录的绝对路径]
   # the port at which the clients will connect
   # 监听客户端连接的端口。默认：2181
   clientPort=2181
   # the maximum number of client connections.
   # increase this if you need to handle more clients
   #maxClientCnxns=60
   #
   # Be sure to read the maintenance section of the 
   # administrator guide before turning on autopurge.
   #
   # http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
   #
   # The number of snapshots to retain in dataDir
   #autopurge.snapRetainCount=3
   # Purge task interval in hours
   # Set to "0" to disable auto purge feature
   #autopurge.purgeInterval=1
   ## Metrics Providers
   #
   # https://prometheus.io Metrics Exporter
   #metricsProvider.className=org.apache.zookeeper.metrics.prometheus.PrometheusMetricsProvider
   #metricsProvider.httpPort=7000
   #metricsProvider.exportJvmInfo=true
   ```

   跳至2.4启动对应搭建版本服务。

#### 2.3集群版（Clustered (Multi-Server) Setup）

为了获得可靠的 ZooKeeper 服务，您应该将 ZooKeeper 部署在一个称为*ensemble*的集群中。只要大多数合奏都已启动，该服务就可用。因为 Zookeeper 需要多数，所以**最好使用奇数台机器**。比如有四台机器的ZooKeeper只能处理一台机器的故障；如果两台机器出现故障，剩下的两台机器不构成多数。然而，有了五台机器，ZooKeeper 可以处理两台机器的故障。

**容错集群设置至少需要三台服务器，强烈建议您使用奇数台服务器。**

通常三台服务器对于生产安装来说已经足够了，但是为了在维护期间获得最大的可靠性，您可能希望安装五台服务器。对于三台服务器，如果您对其中一台进行维护，则在维护期间，其他两台服务器中的一台很容易发生故障。如果您有五个在运行，您可以将其中一个停机进行维护，并且知道如果其他四个中的一个突然出现故障，您仍然可以正常工作。

您的冗余考虑应包括您环境的所有方面。如果您有三台 ZooKeeper 服务器，但它们的网线都插入同一个网络交换机，那么该交换机的故障将使您的整个系统瘫痪。

详情请看《高级进阶-Zookeeper集群》。

##### 2.3.1本节测试

测试方式已伪集群方式进行。在本节的《文档》栏目可以找到官方文档的描述。

本测试搭建三个zookeeper实例。

步骤：

1. 准备一个自己的环境。拷贝一份zookeeper server按照2.1-2.2的步骤配置好一个zookeeper。

2. 复制三个步骤1的实例。

3. 修改每个实例的zoo.cfg配置文件。

   - 修改dataDir参数分别指定对应实例的data目录。
   - 修改clientPort指定不同监听端口。

4. 为每个服务器指定一个*myid*文件，该文件由数字组成，不可重复，位于该服务器实例的数据目录中，由配置文件参数**dataDir**所指定。（注：详细的myid文件介绍请看《高级进阶-Zookeeper集群》）。

   本文使用echo方式分别创建对应文件到data目录中。

   ```bash
   bash echo [内容] >> [路径+文件名]
   ```

   示例：

   ```bash
   bash echo 1 >> zookeeper1/data/myid
   ```

   更多有关CentOS中的命令详解请移步Linux仓库文档。

5. 在每个实例`zoo.cfg`中新增包含自己在内的其他实例的id，host、投票端口、选举端口的配置项。格式是这样子的：

   ```properties
   server.id=host:port:port
   ```

   id即为指定myid文件中的值；前面的port为**投票**端口，后面的port为**选举**端口

   本次测试完整的格式：

   ```properties
   server.1=127.0.0.1:2881:3881
   server.2=127.0.0.1:2882:3882
   server.3=127.0.0.1:2883:3883
   ```

6. （❌便于测试本点不是重点）编写启动和停止服务批处理脚本。

   启动脚本：

   ```sh
   ./<path>/bin/zkServer.sh start
   ./<path>/bin/zkServer.sh start
   ./<path>/bin/zkServer.sh start
   ```

   停止脚本：

   ```sh
   ./<path>/bin/zkServer.sh stop
   ./<path>/bin/zkServer.sh stop
   ./<path>/bin/zkServer.sh stop
   ```

   给上面的脚本权限即可运行。



#### 2.4.启动对应搭建版本服务

根据当前环境在`bin`目录中运行`zkServer.sh`脚本，即可启动zookeeper。

```bash
bash ./zkServer.sh [--config <conf-dir>] {start|start-foreground|stop|version|restart|status|print-cmd}
```

参数解释：

1. `start`：后台启动。
2. `start-foreground`：前台启动。
3. `stop`：停止zookeeper。
4. `version`：查看当前版本。
5. `restart`：重启zookeeper。
6. `status`：当前状态。
7. `print-cmd`：打印相关信息。
8. [--config <conf-dir>]：加载其他路径配置文件。

Example1（单机版）：后台启动Zookeeper。

```bash
bash ./zkServer.sh start
```

通过使用`status`参数大致可以得到如下输出：

```
output:
	Using config: /../../conf/zoo.cfg
	Client port found: 2181. Client address: localhost. Client SSL: false.
	Mode: standalone
```

Example2：前台启动制定目录下的zoo.cfg。

```bash
bash ./zkServer.sh --config ../conf start-foreground
```

Example3（集群版）：与上面例1一样分别启动集群中的Zookeeper实例即可。

通过使用`status`参数大致可以得到如下输出：

```
output1：
	Using config: /../../conf/zoo.cfg
	Client port found: 2183. Client address: localhost. Client SSL: false.
	Mode: follower
```

```
output2：
	Using config: /../../conf/zoo.cfg
	Client port found: 2182. Client address: localhost. Client SSL: false.
	Mode: leader
```

### 3.zkCli连接至zookeeper服务

根据当前环境使用bin目录中的`zkCli`脚本连接zookeeper服务。

```bash
ZooKeeper -server host:port -client-configuration properties-file cmd args
        ...
```

由于zkcli客户端参数较多。本处只介绍部分常用参数，详情请参见官方文档cli：[ZooKeeper CLI](https://zookeeper.apache.org/doc/r3.7.0/zookeeperCLI.html)。更多常用操作指令查看章节《zkCli操作Zookeeper命令》

一般的，直接运行`zkCli`会默认使用 127.0.0.1:2181 端口链接zookeeper服务：

```bash
bash ./zkCli.sh
```

对于远程连接zookeeper服务或服务群则需要指定连接参数 `-server host:port`进行连接：

```bash
bash ./zkCli.sh -server host:port
```

**集群：对于集群使用zkCli连接任意集群中的节点即可**

#### 其他参数使用案例：

Example1：`history`：显示有关您最近执行的 11 个命令的历史记录

```bash
bash ./zkCli.sh -server 127.0.0.1:2181
bash history
```

![](http://rep.shaoteemo.com/zkcli_bash_history.png)

由于在那之前没有操作过任何命令，所以此处为0。仅限本次连接，关闭后丢失。

Example2：`version`：显示 ZooKeeper 客户端/CLI 的版本

```bash
bash ./zkCli.sh version
```

![](http://rep.shaoteemo.com/zkcli_bash_version.png)

### 文档

[搭建单机版（官方文档）](https://zookeeper.apache.org/doc/r3.7.0/zookeeperStarted.html#sc_InstallingSingleMode)

[搭建集群版（官方文档）](http://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#sc_zkMulitServerSetup)、[Running Replicated ZooKeeper](https://zookeeper.apache.org/doc/r3.7.0/zookeeperStarted.html#sc_RunningReplicatedZooKeeper)

## zkCli操作Zookeeper命令

本节介绍部分常用的zkCli指令使用方式。详细请查看官方文档：[ZooKeeper CLI](http://zookeeper.apache.org/doc/r3.7.0/zookeeperCLI.html)

> 注意：本节内容均在使用zkCli连接过后的界面操作。详情请看《环境搭建细节》第3小节

通过zkCli连接到zookeeper后使用`help`出现如下指令提示：

```bash
ZooKeeper -server host:port -client-configuration properties-file cmd args
        addWatch [-m mode] path # optional mode is one of [PERSISTENT, PERSISTENT_RECURSIVE] - default is PERSISTENT_RECURSIVE
        addauth scheme auth
        close 
        config [-c] [-w] [-s]
        connect host:port
        create [-s] [-e] [-c] [-t ttl] path [data] [acl]
        delete [-v version] path
        deleteall path [-b batch size]
        delquota [-n|-b|-N|-B] path
        get [-s] [-w] path
        getAcl [-s] path
        getAllChildrenNumber path
        getEphemerals path
        history 
        listquota path
        ls [-s] [-w] [-R] path
        printwatches on|off
        quit 
        reconfig [-s] [-v version] [[-file path] | [-members serverID=host:port1:port2;port3[,...]*]] | [-add serverId=host:port1:port2;port3[,...]]* [-remove serverId[,...]*]
        redo cmdno
        removewatches path [-c|-d|-a] [-l]
        set [-s] [-v version] path data
        setAcl [-s] [-v version] [-R] path acl
        setquota -n|-b|-N|-B val path
        stat [-w] path
        sync path
        version 
        whoami
```

### 1.`ls`：列出一条路径的子节点。

```bash
bash ls [-s] [-w] [-R] path
```

-s：显示统计状态。

-w：设置查看子节点上的变化。

-R：递归显示所有子节点。

Example1：列出root节点下的子节点。

```bash
bash ls /
```

```
output:
	[zookeeper]
```

### 2.`create`：创建一个 znode。

```bash
bash create [-s] [-e] [-c] [-t ttl] path [data] [acl]
```

-s：有顺序的节点。

-e：临时节点。

-c：容器节点。

-t：ttl节点。

path：必填。路径。

data：数据值。

说明：这些参数可以联合使用。默认不加参数直接创建的是persistent_node，如果加上`-s`则表示创建persistent-sequential node。

Example1：创建一个持续节点。

```bash
bash create /persistent persistent
```

```
output：
	Created /persistent
```

Example2：创建一个持久化顺序节点。

```bash
bash create -s /persistent_sequential persistent_sequential
```

```
output:
	Created /persistent_sequential0000000001
```

Example3：创建一个临时顺序节点。

```bash
bash create -e -s /ephemeral_sequential_node ephemeral_sequential
```

```
output:
	Created /ephemeral_sequential_node0000000002
```

### 3.`get`：获取特定路径的数据

```bash
bash get [-s] [-w] path
```

-s：显示统计信息。

-w：设置查看数据的变化。

Example1：

```bash
bash  get /persistent -s
```

```
output:
	persistent
	cZxid = 0x300000006
	ctime = Sun Oct 03 14:43:05 CST 2021
	mZxid = 0x300000006 #最近一次更新的zxid
	mtime = Sun Oct 03 14:43:05 CST 2021
	pZxid = 0x300000006
	cversion = 0
	dataVersion = 0
	aclVersion = 0
	ephemeralOwner = 0x0
	dataLength = 10
	numChildren = 0
```

persistent ：节点上存储的数据

cZxid:创建时zxid(znode 每次改变时递增的事务id)

ctime:创建时间戳

mZxid:最近一次更新的zxid

mtime:最近一次更新的时间戳

pZxid:子节点的zxid

cversion:子节点更新次数

dataversion:节点数据更新次数

aclVersion:节点ACL(授权信息)的更新次数

ephemeralOwner: 如果该节点为ephemeral 节点( 临时， 生命周期与session 一样),ephemeralOwner 值表示与该节点绑定的session id. 如果该节点不是ephemeral 节点, ephemeralOwner 值为0.

dataLength:节点数据字节数

numChildren:子节点数量

### 4.`set`：设置/更新路径上的数据。

```bash
bash set [-s] [-v version] path data
```

-s：显示此节点的状态。

-v：使用 CAS 设置数据，可以使用 stat 从 dataVersion 中找到版本。

Example1：修改 /persistent 节点上的值

```bash
bash set /persistent persistent_updata
```

```
output:	
	WATCHER::
	WatchedEvent state:SyncConnected type:NodeDataChanged path:/persistent
```

### 5.`delete` ：删除具有特定路径的节点

```bash
bash delete [-v version] path
```

Example1：删除/persistent

```
bash delete /persistent
```

## JavaAPI

本节内容将会有代码存放于仓库中。基本的操作会在代码注释中体现，请查看源码。

官方Java代码示例：[Java Example](https://zookeeper.apache.org/doc/r3.7.0/javaExample.html)

### Java客户端连接工具依赖

[Maven中央仓库地址](https://search.maven.org/artifact/org.apache.zookeeper/zookeeper/3.7.0/jar)

```xml
<dependency>
  <groupId>org.apache.zookeeper</groupId>
  <artifactId>zookeeper</artifactId>
  <version>3.7.0</version>
</dependency>
```

## 实战演练

### 1.实战目标

使用RMI技术和Zookeeper实现一个RPC调用。

RPC：RPC（Remote Procedure Call）远程过程调用。

### 2.扩展知识

#### 2.1.什么是RMI

[本节取自Java官方文档：[RMI](https://docs.oracle.com/javase/tutorial/rmi/index.html)。更多核心的内容文档翻译会在技术仓库中存放]

Remote Method Invocation（RMI） 应用程序通常包括两个独立的程序，一个服务器和一个客户端。典型的服务器程序创建一些远程对象，使这些对象的引用可访问，并等待客户端调用这些对象上的方法。典型的客户端程序获取对服务器上一个或多个远程对象的远程引用，然后调用它们的方法。RMI 提供了服务器和客户端通信和来回传递信息的机制。这种应用程序有时被称为分布式对象应用程序。

分布式对象应用需要做到以下几点：

- **Locate remote objects.** 应用程序可以使用各种机制来获取对远程对象的引用。例如，应用程序可以使用 RMI 的简单命名工具 RMI 注册表注册其远程对象。或者，应用程序可以传递和返回远程对象引用作为其他远程调用的一部分。
- **Communicate with remote objects.** 远程对象之间的通信细节由 RMI 处理。对于程序员来说，远程通信看起来类似于常规的 Java 方法调用。
- **Load class definitions for objects that are passed around.** 因为 RMI 使对象能够来回传递，所以它提供了加载对象的类定义以及传输对象数据的机制。

下图描述了一个 RMI 分布式应用程序，它使用 RMI **注册表（ registry ）**来获取对**远程对象（remote object）**的引用。服务器调用 registry 以将名称与远程对象关联（或**绑定（bind）**）。客户端在服务器的注册表中通过名称**查找（looks up）**远程对象，然后**调用（invokes）**它的方法。该图还显示 RMI 系统使用现有的 Web 服务器在需要时为对象**加载类定义（load class definitions）**，从服务器到客户端，从客户端到服务器。

![](http://rep.shaoteemo.com/rmi-2.gif)

......更多详情请查看官方文档。

#### 2.2.RMI部分API介绍

##### 2.2.1java.rmi.Remote接口

![](http://rep.shaoteemo.com/rmi_api_interface.png)

##### 2.2.2. java.rmi.RemoteException异常

作为远程接口的成员，无论是哪一个允许远程调用方法。此方法必须定义为能够抛出 java.rmi.RemoteException。此异常由 RMI 系统从远程方法调用中抛出，以指示发生了通信故障或协议错误。 RemoteException 是一个已检查的异常，因此任何调用远程方法的代码都需要通过捕获它或在它的 throws 子句中声明它来处理此异常。

##### 2.2.3.java.rmi.server.UnicastRemoteObject类

此类实现了Remote 接口和Serializable 接口。自定义接口实现类除了实现自定义接口还需要继承此类。

##### 2.2.4.java.rmi.registry.LocateRegistry

java.rmi.registry.Registry 远程接口是用于绑定（或注册）和在注册表中查找远程对象的 API。 java.rmi.registry.LocateRegistry 类提供静态方法，用于合成对特定网络地址（主机和端口）处的注册表的远程引用。这些方法在不执行任何远程通信的情况下创建包含指定网络地址的远程引用对象。 LocateRegistry 还提供了用于在当前 Java 虚拟机中创建新注册表的静态方法，尽管本示例没有使用这些方法。一旦远程对象在本地主机上的 RMI 注册表中注册，任何主机上的客户端都可以按名称查找远程对象，获取其引用，然后调用该对象上的远程方法。注册表可以由主机上运行的所有服务器共享，或者单个服务器进程可以创建和使用自己的注册表。

##### 2.2.5.java.rmi.Naming

Naming类提供了在远程对象注册表中存储和获取对远程对象的引用的方法。

简言之这个类：对于Server提供绑定对象映射，对于Client提供获取对象映射。

### 3.实践步骤

#### 3.1编写RMI服务器

[官方文档](https://docs.oracle.com/javase/tutorial/rmi/server.html)

查看源码：

```java
com.shaoteemo.zookeeper.service.RmiMethodService
com.shaoteemo.zookeeper.practice.service.impl.RmiMethodServiceImpl
com.shaoteemo.zookeeper.practice.RmiClient
```

#### 3.2编写RMI客户端

查看代码：

```java
com.shaoteemo.zookeeper.practice.RmiClient
```

#### 3.3.运行项目

3.3.1.运行com.shaoteemo.zookeeper.practice.ZookeeperPracticeApplication启动RMI客户端。

![](http://rep.shaoteemo.com/zookeeper_rmi_server_run.png)

3.3.2.运行com.shaoteemo.zookeeper.practice.RmiClientTest#rmiClient测试方法模拟接口调用做加法运算。

![](http://rep.shaoteemo.com/zookeeper_rmi_rpc_invoke.png)

## 高级进阶

### 1.Zookeeper存储结构

![](http://rep.shaoteemo.com/zookeeper_node.png)

Zookeeper底层是一套数据结构。结构采用树形结构。而且znode还分为4种不同的类型：

1. PERSISTENT：持久化目录节点。客户端与zookeeper断开连接后，该节点依然存在。
2. PERSISTENT_SEQUENTIAL：持久化顺序编号目录节点。客户端与zookeeper断开连接后，该节点依然存在并且zookeeper会给该节点名称进行顺序编号。
3. EPHEMERAL：临时目录节点。客户端与zookeeper断开后，该节点删除。
4. EPHEMERAL_SEQUENTIAL：临时顺序编号目录节点。客户端与zookeeper断开后，该节点删除并且zookeeper会给该节点名称进行顺序编号。
5. Container Nodes（3.6.0版本添加）：ZooKeeper 有容器 znode 的概念。容器 znode 是特殊用途的 znode，可用于领导者、锁定等方法。当容器的最后一个子节点被删除时，该容器将成为将来某个时间点被服务器删除的候选对象。
6. TTL Nodes（3.6.0版本添加）：创建 **PERSISTENT** 或 **PERSISTENT_SEQUENTIAL** znode 时，您可以选择为 znode 设置以毫秒为单位的 TTL。如果在 TTL 内没有修改 znode 并且没有子节点，它将成为服务器在未来某个时间点删除的候选节点。

Zookeeper中每一个节点类型称为**znode**节点类型，其中\ （root）为节点名称，每一个znode默认可以存储1Mb的数据。这种节点与Unix文件系统路径相似，可以向节点存储、获取数据。

### 2.监听通知机制

zookeeper使用了设计模式中的**观察者模式**。客户端注册监听它关注的目录节点时，当目录节点发生变化（数据改变、删除、子目录节点增加删除）时，Zookeeper 会通知客户端。

### 3.Zookeeper集群

#### 3.1集权角色划分

![](http://rep.shaoteemo.com/zkcli_cluster_role.png)

![](http://rep.shaoteemo.com/zkcli_cluster_uml.png)

上图可知：对于一个zookeeper集群所有的Server都是互通的，5个server中有一个leader，一个observer，三个follower。当有任意Server数据发生改变会通知lander，然后lander会通知其他节点，因此Client连接任意一个server最终数据都是一致的。

#### 3.2集群搭建

为了获得可靠的 ZooKeeper 服务，您应该将 ZooKeeper 部署在一个称为*ensemble*的集群中。只要大多数合奏都已启动，该服务就可用。因为 Zookeeper 需要多数，所以最好使用奇数台机器。比如有四台机器的ZooKeeper只能处理一台机器的故障；如果两台机器出现故障，剩下的两台机器不构成多数。然而，有了五台机器，ZooKeeper 可以处理两台机器的故障。

> 正如[ZooKeeper Getting Started Guide ]中(https://zookeeper.apache.org/doc/r3.7.0/zookeeperStarted.html)提到的，容错集群设置至少需要三台服务器，强烈建议您使用奇数台服务器。
>
> 通常三台服务器对于生产安装来说已经足够了，但是为了在维护期间获得最大的可靠性，您可能希望安装五台服务器。对于三台服务器，如果您对其中一台进行维护，则在维护期间，其他两台服务器中的一台很容易发生故障。如果您有五个在运行，您可以将其中一个停机进行维护，并且知道如果其他四个中的一个突然出现故障，您仍然可以正常工作。
>
> 您的冗余考虑应包括您环境的所有方面。如果您有三台 ZooKeeper 服务器，但它们的网线都插入同一个网络交换机，那么该交换机的故障将使您的整个系统瘫痪。

以下是设置将成为 ensemble 一部分的服务器的步骤。这些步骤应该在整体中的每个主机上执行：

1. 安装 Java JDK。您可以为您的系统使用本机打包系统，或从以下位置下载 JDK：[http](http://java.sun.com/javase/downloads/index.jsp) : [//java.sun.com/javase/downloads/index.jsp](http://java.sun.com/javase/downloads/index.jsp)

2. 设置 Java 堆大小。这对于避免交换非常重要，交换会严重降低 ZooKeeper 的性能。要确定正确的值，请使用负载测试，并确保您远低于会导致交换的使用限制。保守一点 - 对 4GB 机器使用 3GB 的最大堆大小。

3. 安装 ZooKeeper 服务器包。可从以下[网址](http://zookeeper.apache.org/releases.html)下载：http: [//zookeeper.apache.org/releases.html](http://zookeeper.apache.org/releases.html)

4. 创建配置文件。这个文件可以叫任何名字。使用以下设置作为起点：

   ```
   tickTime=2000
   dataDir=/var/lib/zookeeper/
   clientPort=2181
   initLimit=5
   syncLimit=2
   server.1=zoo1:2888:3888
   server.2=zoo2:2888:3888
   server.3=zoo3:2888:3888
   ```

   您可以在[配置参数](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#sc_configuration)部分找到这些和其他配置设置的含义。关于这里的一些话：作为 ZooKeeper 集合的一部分的每台机器都应该知道集合中的所有其他机器。您可以使用一系列形式为**server.id=host:port:port**的行来完成此操作。（参数**主机**和**端口**很简单，对于每个服务器，您需要首先指定一个仲裁（即投票）端口，然后指定一个用于 ZooKeeper 领导者选举的专用端口）。从 ZooKeeper 3.6.0 开始，您还可以[指定多个地址](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#id_multi_address)对于每个 ZooKeeper 服务器实例（当可以在集群中并行使用多个物理网络接口时，这可以提高可用性）。您可以通过创建一个名为*myid*的文件将服务器 id 分配给每台机器，每个服务器*对应*一个文件，该文件位于该服务器的数据目录中，如配置文件参数**dataDir**所指定。

5. myid 文件由一行仅包含该机器的 id 文本组成。所以服务器 1 的*myid*将包含文本“1”而没有其他内容。id 在集合中必须是唯一的，并且值应该在 1 到 255 之间。**重要：**如果您启用了扩展功能，例如 TTL 节点，由于内部限制，id 必须在 1 到 254 之间。

6. 在与*myid*相同的目录中创建一个初始化标记文件*initialize*。该文件表明需要一个空的数据目录。当存在时，将创建一个空数据库并删除标记文件。如果不存在，一个空的数据目录将意味着该对等方没有投票权，并且在与活动领导者通信之前不会填充数据目录。预期用途是仅在启动新集合时创建此文件。

7. 如果您的配置文件已设置，您可以启动 ZooKeeper 服务器：

   ```
   $ java -cp zookeeper.jar:lib/*:conf org.apache.zookeeper.server.quorum.QuorumPeerMain zoo.conf
   ```

QuorumPeerMain 启动 ZooKeeper 服务器，还注册了[JMX](http://java.sun.com/javase/technologies/core/mntr-mgmt/javamanagement/)管理 bean，允许通过 JMX 管理控制台进行管理。该[ZooKeeper的JMX文件](https://zookeeper.apache.org/doc/r3.7.0/zookeeperJMX.html)包含有关使用JMX管理ZooKeeper的细节。有关启动服务器实例的示例，请参阅发行版中包含的脚本*bin/zkServer.sh*。8. 通过连接主机测试您的部署：在 Java 中，您可以运行以下命令来执行简单的操作：

```
    $ bin/zkCli.sh -server 127.0.0.1:2181
```

### 4.配置参数详解

❌**快速搭建的读者，请跳过此节**❌

配置文件的官方参数解释[官方配置参数](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#sc_configuration)。

ZooKeeper 的行为由 ZooKeeper 配置文件控制。该文件的设计使得组成 ZooKeeper 服务器的所有服务器都可以使用完全相同的文件，假设磁盘布局相同。如果服务器使用不同的配置文件，必须注意确保所有不同配置文件中的服务器列表匹配。

注意：在 3.5.0 及更高版本中，其中一些参数应放置在动态配置文件中。如果它们放在静态配置文件中，ZooKeeper 会自动将它们移到动态配置文件中。有关详细信息，请参阅[动态重新配置（官方文档）](https://zookeeper.apache.org/doc/r3.7.0/zookeeperReconfig.html)。

#### 4.1最低配置要求

以下是必须在配置文件中定义的最少配置关键字：

- *clientPort* : 监听客户端连接的端口；也就是说，客户端尝试连接的端口。默认：**2181**

- *secureClientPort*：使用 SSL 侦听安全客户端连接的端口。**clientPort**指定明文连接的端口，而**secureClientPort**指定 SSL 连接的端口。指定两者将启用混合模式，而省略其中任何一个将禁用该模式。请注意，当用户将 zookeeper.serverCnxnFactory、zookeeper.clientCnxnSocket 作为 Netty 插入时，将启用 SSL 功能。

- *observerMasterPort* : 监听观察者连接的端口；也就是说，观察者试图连接的端口。如果设置了该属性，那么除了处于领导者模式时，服务器将在跟随者模式下托管观察者连接，并相应地在观察者模式下尝试连接到任何投票对等点。

- *dataDir*：ZooKeeper 将存储内存中数据库快照的位置，除非另外指定，否则存储数据库更新的事务日志。

  > 小心放置事务日志的位置。专用的事务日志设备是保持良好性能的关键。将日志放在繁忙的设备上会对性能产生不利影响。

- *tickTime*：单个滴答的长度，这是 ZooKeeper 使用的基本时间单位，以毫秒为单位。它用于调节心跳和超时。例如，最小会话超时将为两个滴答声。

#### 4.2高级配置

本节中的配置设置是可选的。您可以使用它们来进一步微调 ZooKeeper 服务器的行为。有些也可以使用 Java 系统属性进行设置，通常采用*zookeeper.keyword*形式。确切的系统属性（如果可用）如下所示。

- dataLogDir：（无 Java 系统属性）此选项将指示机器将事务日志写入dataLogDir而不是dataDir。这允许使用专用日志设备，并有助于避免日志记录和快照之间的竞争。

  > 拥有专用日志设备对吞吐量和稳定延迟有很大影响。强烈建议使用专用日志设备并将**dataLogDir**设置为指向该设备上的目录，然后确保将**dataDir**指向*不在*该设备上的目录。

- *globalOutstandingLimit*：（Java 系统属性：**zookeeper.globalOutstandingLimit。**）客户端可以比 ZooKeeper 处理请求的速度更快地提交请求，尤其是在有很多客户端的情况下。为了防止 ZooKeeper 因请求排队而耗尽内存，ZooKeeper 将限制客户端，以便系统中的未完成请求不超过 globalOutstandingLimit。默认限制为 1,000。

- *preAllocSize* : (Java 系统属性：**zookeeper.preAllocSize** ) 为避免搜索 ZooKeeper 以 preAllocSize 千字节块为单位在事务日志文件中分配空间。默认块大小为 64M。更改块大小的原因之一是如果更频繁地拍摄快照，则减小块大小。（另外，请参阅**snapCount**和**snapSizeLimitInKb**）。

- *snapCount*：（Java 系统属性：**zookeeper.snapCount**）ZooKeeper 使用快照和事务日志（想想预写日志）记录其事务。在可以拍摄快照（以及事务日志滚动）之前，事务日志中记录的事务数) 由 snapCount 决定。为了防止 quorum 中的所有机器同时进行快照，每个 ZooKeeper 服务器都会在事务日志中的事务数量达到 [snapCount/2+1 , snapCount] 范围。默认 snapCount 为 100,000。

- *commitLogCount* * ：（Java 系统属性：**zookeeper.commitLogCount**）Zookeeper 维护一个内存中的最后提交请求列表，以便在追随者不太落后时与追随者快速同步。如果您的快照很大 (>100,000)，这会提高同步性能。默认 commitLogCount 值为 500。

- *snapSizeLimitInKb* : (Java 系统属性：**zookeeper.snapSizeLimitInKb**) ZooKeeper 使用快照和事务日志记录其事务（想想预写日志）。在可以拍摄快照（和事务日志滚动）之前，记录在事务日志中的一组事务中允许的总大小（以字节为单位）由 snapSize 确定。为了防止仲裁中的所有机器同时进行快照，当事务日志中事务集的字节大小达到运行时生成的随机值时，每个 ZooKeeper 服务器都会进行快照。 snapSize/2+1, snapSize] 范围。每个文件系统都有一个最小标准文件大小，为了使此功能有效运行，选择的数字必须大于该值。默认 snapSizeLimitInKb 为 4,194,304 (4GB)。非正值将禁用该功能。

- *txnLogSizeLimitInKb* : (Java 系统属性：**zookeeper.txnLogSizeLimitInKb** ) Zookeeper 事务日志文件也可以使用 txnLogSizeLimitInKb 更直接地控制。当使用事务日志完成同步时，较大的 txn 日志可能会导致跟随者同步变慢。这是因为领导者必须扫描磁盘上相应的日志文件才能找到要从中开始同步的事务。此功能默认关闭，并且 snapCount 和 snapSizeLimitInKb 是限制事务日志大小的唯一值。启用后，Zookeeper 将在达到任何限制时滚动日志。请注意，实际日志大小可能会超过此值，超出序列化事务的大小。另一方面，如果此值设置得太接近（或小于）**preAllocSize**，它会导致 Zookeeper 为每个事务滚动日志。虽然这不是正确性问题，但这可能会导致性能严重下降。为了避免这种情况并充分利用此功能，建议将该值设置为 N * **preAllocSize**，其中 N >= 2。

- *maxCnxns*：（Java 系统属性：**zookeeper.maxCnxns**）限制可以与zookeeper服务器建立的并发连接总数（每个服务器的每个客户端端口）。这用于防止某些类别的 DoS 攻击。默认值为 0，将其设置为 0 将完全取消对并发连接总数的限制。计算 serverCnxnFactory 和 secureServerCnxnFactory 的连接数是分开进行的，因此允许对等方最多托管 2*maxCnxns，前提是它们具有适当的类型。

- *maxClientCnxns*：（无 Java 系统属性）限制由 IP 地址标识的单个客户端可以与 ZooKeeper 集合的单个成员建立的并发连接数（在套接字级别）。这用于防止某些类别的 DoS 攻击，包括文件描述符耗尽。默认值为 60。将此设置为 0 将完全取消对并发连接的限制。

- *clientPortAddress* : **3.3.0 中的新功能：**侦听客户端连接的地址（ipv4、ipv6 或主机名）；也就是说，客户端尝试连接的地址。这是可选的，默认情况下，我们以这样一种方式绑定，即服务器上任何地址/接口/网卡的任何与**clientPort 的**连接都将被接受。

- *minSessionTimeout*：（无 Java 系统属性）**3.3.0 中的新增功能：**服务器允许客户端协商的最小会话超时时间（以毫秒为单位）。默认为**tickTime 的**2 倍。

- *maxSessionTimeout*：（无 Java 系统属性）**3.3.0 中的新增功能：**服务器允许客户端协商的最大会话超时（以毫秒为单位）。默认为**tickTime 的**20 倍。

- *fsync.warningthresholdms* : (Java 系统属性：**zookeeper.fsync.warningthresholdms** ) **3.3.4 中的新功能：**每当事务日志 (WAL) 中的 fsync 花费的时间超过此值时，都会向日志输出警告消息。这些值以毫秒为单位指定，默认为 1000。该值只能设置为系统属性。

- *maxResponseCacheSize* : (Java 系统属性：**zookeeper.maxResponseCacheSize** ) 当设置为正整数时，它决定了存储最近读取记录的序列化形式的缓存的大小。有助于节省流行 znode 的序列化成本。指标**response_packet_cache_hits**和**response_packet_cache_misses**可用于将此值调整为给定的工作负载。该功能默认开启，值为 400，设置为 0 或负整数以关闭该功能。

- *maxGetChildrenResponseCacheSize* : (Java 系统属性：**zookeeper.maxGetChildrenResponseCacheSize** ) **3.6.0 新增：**类似于**maxResponseCacheSize**，但适用于获取子请求。指标**response_packet_get_children_cache_hits**和**response_packet_get_children_cache_misses**可用于将此值调整为给定的工作负载。该功能默认开启，值为 400，设置为 0 或负整数以关闭该功能。

- *autopurge.snapRetainCount* : (No Java system property) **New in 3.4.0:**启用后，ZooKeeper 自动清除功能分别在**dataDir**和**dataLogDir 中**保留**autopurge.snapRetainCount**最近的快照和相应的事务日志，并删除其余部分。默认为 3。最小值为 3。

- *autopurge.purgeInterval*：（无 Java 系统属性）**3.4.0 中的新增功能：**必须触发清除任务的时间间隔（以小时为单位）。设置为正整数（1 及以上）以启用自动清除。默认为 0。

- *syncEnabled* : (Java 系统属性：**zookeeper.observer.syncEnabled** ) *3.4.6、3.5.0 中的***新功能：**观察者现在像参与者一样默认记录事务并将快照写入磁盘。这减少了观察者在重启时的恢复时间。设置为“false”以禁用此功能。默认为“真”

- *extendedTypesEnabled*：（仅限 Java 系统属性：**zookeeper.extendedTypesEnabled**）**3.5.4、3.6.0 中的****新增**`true`功能**：**定义为启用扩展功能，例如创建[TTL 节点](https://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html#TTL+Nodes)。默认情况下它们是禁用的。重要提示：由于内部限制，启用时服务器 ID 必须小于 255。

- *emulate353TTLNodes*：（仅限 Java 系统属性：**zookeeper.emulate353TTLNodes**）。3.5.4、3.6.0 中的新增功能**：**由于 [ZOOKEEPER-2901] (https://issues.apache.org/jira/browse/ZOOKEEPER-2901) 在 3.5.3 版本中创建的 TTL 节点在 3.5 中不受支持。 4/3.6.0。但是，通过 zookeeper.emulate353TTLNodes 系统属性提供了一种解决方法。如果您使用TTL节点的ZooKeeper 3.5.3和需要保持兼容性设置**zookeeper.emulate353TTLNodes**到`true`除了**zookeeper.extendedTypesEnabled**。注意：由于该错误，服务器 ID 必须为 127 或更少。此外，最大支持 TTL 值`1099511627775`小于 3.5.3 ( `1152921504606846975`)

- *watchManaggerName*：（仅限 Java 系统属性：**zookeeper.watchManagerName**）**3.6.0 中的**新增功能**：**添加到[ZOOKEEPER-1179 中](https://issues.apache.org/jira/browse/ZOOKEEPER-1179)添加了新的观察器管理器 WatchManagerOptimized 以优化大量手表用例中的内存开销。此配置用于定义要使用的观察者管理器。目前，我们只支持 WatchManager 和 WatchManagerOptimized。

- *watcherCleanThreadsNum*：（仅限 Java 系统属性：**zookeeper.watcherCleanThreadsNum**）**3.6.0 中的新功能：**在[ZOOKEEPER-1179 中](https://issues.apache.org/jira/browse/ZOOKEEPER-1179)添加新的观察者管理器 WatchManagerOptimized 将懒惰地清理死观察者，此配置用于决定 WatcherCleaner 中使用了多少线程. 更多的线程通常意味着更大的清理吞吐量。默认值为 2，即使对于繁重和连续的会话关闭/重新创建情况也足够好。

- *watcherCleanThreshold* : (Java system property only: **zookeeper.watcherCleanThreshold** ) **3.6.0**新增**：**[ZOOKEEPER-1179](https://issues.apache.org/jira/browse/ZOOKEEPER-1179)新增新的watcher manager WatchManagerOptimized会懒惰地清理死掉的watcher，清理过程比较繁重，批处理会降低成本并提高性能。此设置用于决定批量大小。默认为1000，如果没有内存或清理速度问题，我们不需要更改它。

- *watcherCleanIntervalInSeconds* : (Java system property only: **zookeeper.watcherCleanIntervalInSeconds** ) **3.6.0**新增**：**[ZOOKEEPER-1179](https://issues.apache.org/jira/browse/ZOOKEEPER-1179)新增新的watcher manager WatchManagerOptimized会懒惰地清理死掉的watcher，清理过程比较繁重，批处理会降低成本并提高性能。除了watcherCleanThreshold，这个设置用于在一定时间后清理dead watchers，即使dead watchers不大于watcherCleanThreshold，这样我们就不会把dead watchers留在那里太久。默认设置为 10 分钟，通常不需要更改。

- *maxInProcessingDeadWatchers*：（仅限 Java 系统属性：**zookeeper.maxInProcessingDeadWatchers**）**3.6.0**新增**：**在[ZOOKEEPER-1179 中](https://issues.apache.org/jira/browse/ZOOKEEPER-1179)添加这用于控制我们在 WatcherCleaner 中可以有多少积压，当达到这个数字时，它会减慢添加Dead watcher 到 WatcherCleaner，这反过来会减慢添加和关闭 watcher 的速度，这样我们就可以避免 OOM 问题。默认情况下没有限制，您可以将其设置为 watcherCleanThreshold * 1000 等值。

- *bitHashCacheSize*：（仅限 Java 系统属性：**zookeeper.bitHashCacheSize**）**新 3.6.0**：在[ZOOKEEPER-1179 中](https://issues.apache.org/jira/browse/ZOOKEEPER-1179)添加这是用于决定 BitHashSet 实现中的 HashSet 缓存大小的设置。如果没有 HashSet，我们需要用 O(N) 时间来获取元素，N 是 elementBits 中的位数。但是我们需要保持较小的大小以确保它不会在内存中花费太多，内存和时间复杂度之间存在权衡。默认值为 10，这看起来是一个比较合理的缓存大小。

- *fastleader.minNotificationInterval*：（Java 系统属性：**zookeeper.fastleader.minNotificationInterval**）领导选举的两次连续通知检查之间的时间长度下限。此时间间隔决定了对等方等待检查选举投票集的时间并影响选举解决的速度。间隔遵循从配置的最小值 (this) 和配置的最大值 (fastleader.maxNotificationInterval) 的退避策略，用于长选举。

- *fastleader.maxNotificationInterval* : (Java 系统属性：**zookeeper.fastleader.maxNotificationInterval** ) 领导选举的两次连续通知检查之间的时间长度上限。此时间间隔决定了对等方等待检查选举投票集的时间并影响选举解决的速度。间隔遵循从配置的最小值 (fastleader.minNotificationInterval) 和配置的最大值 (this) 的退避策略，用于长选举。

- *connectionMaxTokens* : (Java 系统属性：**zookeeper.connection_throttle_tokens** ) **3.6.0 新增：**这是调整服务器端连接节流器的参数之一，这是一种基于令牌的速率限制机制，具有可选的概率丢弃。此参数定义令牌桶中的最大令牌数。当设置为 0 时，节流被禁用。默认值为 0。

- *connectionTokenFillTime* : (Java 系统属性：**zookeeper.connection_throttle_fill_time** ) **3.6.0 新增功能：**这是调整服务器端连接节流器的参数之一，这是一种基于令牌的速率限制机制，具有可选的概率丢弃。该参数定义了使用*connectionTokenFillCount*令牌重新填充令牌桶的时间间隔（以毫秒为单位）。默认值为 1。

- *connectionTokenFillCount* : (Java 系统属性：**zookeeper.connection_throttle_fill_count** ) **3.6.0 新增：**这是调整服务器端连接节流器的参数之一，这是一种基于令牌的速率限制机制，具有可选的概率丢弃。此参数定义每*connectionTokenFillTime*毫秒添加到令牌桶中的令牌数。默认值为 1。

- *connectionFreezeTime* : (Java 系统属性：**zookeeper.connection_throttle_freeze_time** ) **3.6.0 新增：**这是调整服务器端连接节流器的参数之一，这是一种基于令牌的速率限制机制，具有可选的概率丢弃。该参数定义了调整丢弃概率的时间间隔（以毫秒为单位）。设置为 -1 时，禁用概率丢弃。默认值为 -1。

- *connectionDropIncrease*：（Java系统属性：**zookeeper.connection_throttle_drop_increase**）**的新3.6.0：**这是参数来调整服务器端连接调节器，这是一种基于令牌的速率限制机制与任选概率滴中的一个。该参数定义了要增加的丢弃概率。节流器检查每个*connectionFreezeTime*毫秒，如果令牌桶为空，则丢弃概率将通过*connectionDropIncrease*增加。默认值为 0.02。

- *connectionDropDecrease* : (Java 系统属性：**zookeeper.connection_throttle_drop_decrease** ) **3.6.0 新增功能：**这是调整服务器端连接节流器的参数之一，这是一种基于令牌的速率限制机制，具有可选的概率丢弃。该参数定义了要降低的丢弃概率。节流器检查每个*connectionFreezeTime*毫秒，如果令牌桶的令牌数超过阈值，则*connectionDropDecrease*将降低丢弃概率。阈值是*connectionMaxTokens* * *connectionDecreaseRatio*。默认值为 0.002。

- *connectionDecreaseRatio* : (Java 系统属性：**zookeeper.connection_throttle_decrease_ratio** ) **3.6.0 新增：**这是调整服务器端连接节流器的参数之一，这是一种基于令牌的速率限制机制，具有可选的概率丢弃。该参数定义了降低丢弃概率的阈值。默认值为 0。

- *zookeeper.connection_throttle_weight_enabled*：（仅限 Java 系统属性）**3.6.0 中的新增功能：**在节流时是否考虑连接权重。仅在启用连接限制时有用，即 connectionMaxTokens 大于 0。默认为 false。

- *zookeeper.connection_throttle_global_session_weight*：（仅限 Java 系统属性）**3.6.0 中的新功能：**全局会话的权重。它是全局会话请求通过连接节流器所需的令牌数。它必须是一个不小于本地会话权重的正整数。默认值为 3。

- *zookeeper.connection_throttle_local_session_weight*：（仅限 Java 系统属性）**3.6.0 中的新功能：**本地会话的权重。它是本地会话请求通过连接节流器所需的令牌数。它必须是一个不大于全局会话或更新会话的权重的正整数。默认值为 1。

- *zookeeper.connection_throttle_renew_session_weight*：（仅限 Java 系统属性）**3.6.0 新增：**更新会话的权重。它也是重新连接请求通过节流器所需的令牌数量。它必须是一个不小于本地会话权重的正整数。默认值为 2。

- *clientPortListenBacklog*：（无Java系统属性）**新的3.4.14，3.5.5，3.6.0：**对于ZooKeeper的服务器插座插座积压长度。这控制将在服务器端排队以供 ZooKeeper 服务器处理的请求数量。超过此长度的连接将收到网络超时（30 秒），这可能会导致 ZooKeeper 会话过期问题。默认情况下，此值是 unset ( `-1`)，它在 Linux 上使用`50`. 该值必须是正数。

- *serverCnxnFactory*：（Java 系统属性：**zookeeper.serverCnxnFactory**）指定 ServerCnxnFactory 实现。这应该设置为`NettyServerCnxnFactory`以使用基于 TLS 的服务器通信。默认为`NIOServerCnxnFactory`。

- *flushDelay*：（Java 系统属性：**zookeeper.flushDelay**）延迟提交日志刷新的时间（以毫秒为单位）。不影响*maxBatchSize*定义的限制。默认情况下禁用（值为 0）。具有高写入速率的集成可能会看到吞吐量提高了 10-20 毫秒的值。

- *maxWriteQueuePollTime*：（Java 系统属性：**zookeeper.maxWriteQueuePollTime**）如果启用了*flushDelay*，这将确定在没有新请求排队时刷新之前等待的时间（以毫秒为单位）。默认设置为*flushDelay* /3（默认情况下隐式禁用）。

- *maxBatchSize*：（Java 系统属性：**zookeeper.maxBatchSize**）触发提交日志刷新之前服务器中允许的事务数。不影响*flushDelay*定义的限制。默认值为 1000。

- *enforceQuota*：（Java系统属性：**zookeeper.enforceQuota**）**新的3.7.0：**强制配额检查。当启用并且客户端超过总字节数或子节点计数硬配额时，服务器将拒绝请求并`QuotaExceededException`强制回复客户端 a 。默认值为：false。探索[配额功能](http://zookeeper.apache.org/doc/current/zookeeperQuotas.html)以获取更多详细信息。

- *requestThrottleLimit*：（Java 系统属性：**zookeeper.request_throttle_max_requests**）**3.6.0 中的新增功能：**在 RequestThrottler 开始停止之前允许的未完成请求总数。当设置为 0 时，节流被禁用。默认值为 0。

- *requestThrottleStallTime* : (Java 系统属性：**zookeeper.request_throttle_stall_time** ) **3.6.0 中的新增功能：**线程可以等待通知它可以继续处理请求的最长时间（以毫秒为单位）。默认值为 100。

- *requestThrottleDropStale* : (Java 系统属性：**request_throttle_drop_stale** ) **3.6.0 中的新**功能**：**启用后，节流器将丢弃陈旧的请求，而不是将它们发送到请求管道。陈旧请求是由现在关闭的连接发送的请求，和/或请求延迟高于 sessionTimeout 的请求。默认值为真。

- *requestStaleLatencyCheck*：（Java 系统属性：**zookeeper.request_stale_latency_check**）**3.6.0 中的新**功能**：**启用后，如果请求延迟高于其关联的会话超时，则请求将被视为过时。默认禁用。

- *requestStaleConnectionCheck* : (Java 系统属性：**zookeeper.request_stale_connection_check** ) **3.6.0 中的新**功能**：**启用后，如果请求的连接已关闭，则请求将被视为过时。默认启用。

- *zookeeper.request_throttler.shutdownTimeout*：（仅限 Java 系统属性）**3.6.0 中的新增功能：** RequestThrottler 在关闭期间等待请求队列耗尽的时间（以毫秒为单位），然后再强制关闭。默认值为 10000。

- *advancedFlowControlEnabled* : (Java 系统属性：**zookeeper.netty.advancedFlowControl.enabled** ) 根据 ZooKeeper 管道的状态在 netty 中使用准确的流量控制，以避免直接缓冲区 OOM。它将禁用 Netty 中的 AUTO_READ。

- *enableEagerACLCheck*：（仅限 Java 系统属性：**zookeeper.enableEagerACLCheck**）当设置为“true”时，在将请求发送到法定人数之前，对每个本地服务器上的写入请求启用*预先*ACL 检查。默认值为“假”。

- *maxConcurrentSnapSyncs*：（Java 系统属性：**zookeeper.leader.maxConcurrentSnapSyncs**）一个领导者或追随者可以同时服务的最大快照同步数。默认值为 10。

- *maxConcurrentDiffSyncs*：（Java 系统属性：**zookeeper.leader.maxConcurrentDiffSyncs**）一个leader或follower可以同时服务的最大差异同步数。默认值为 100。

- *digest.enabled* : (Java system property only: **zookeeper.digest.enabled** ) **3.6.0新增:**增加digest特性，用于检测ZooKeeper从磁盘加载数据库时的数据不一致，追赶和跟随leader，增量哈希根据中提到的 adHash 论文检查 DataTree

  ```
  https://cseweb.ucsd.edu/~daniele/papers/IncHash.pdf
  ```

  思路很简单，DataTree的hash值会根据数据集的变化进行增量更新。当leader准备txn时，它会根据公式发生的变化预先计算树的哈希值：

  ```
  current_hash = current_hash + hash(new node data) - hash(old node data)
  ```

  如果它正在创建一个新节点，则哈希（旧节点数据）将为 0，如果它是删除节点操作，则哈希（新节点数据）将为 0。

  在将 txn 应用到数据树后，该哈希将与每个 txn 相关联以表示预期的哈希值，它将与原始提议一起发送给关注者。Learner 将 txn 应用于数据树后，将实际哈希值与 txn 中的值进行比较，如果不相同则报告不匹配。

  这些摘要值也将与磁盘上的每个 txn 和快照一起持久化，因此当服务器重新启动并从磁盘加载数据时，它会比较并查看是否存在哈希不匹配，这将有助于检测磁盘上的数据丢失问题。

  对于实际的hash函数，我们内部使用的是CRC，它不是无碰撞的hash函数，但是相比于无碰撞的hash，效率更高，而且碰撞的可能性真的很少，已经可以满足我们这里的需求了。

  此功能向后和向前兼容，因此它可以安全地滚动升级、降级、启用和稍后禁用，而不会出现任何兼容问题。以下是已经涵盖和测试的场景：

  1. 当leader用新代码运行而follower用旧代码运行时，摘要将附加到每个txn的末尾，follower只会读取header和txn数据，txn中的摘要值将被忽略。它不会影响跟随者读取和处理下一个交易。
  2. 当leader用旧代码运行而follower用新代码运行时，摘要不会随txn一起发送，当follower试图读取摘要时，它将抛出EOF，该EOF被捕获并优雅地处理，摘要值设置为null。
  3. 使用新代码加载旧快照时，尝试读取不存在的摘要值时会抛出 IOException，并且会捕获异常并将摘要设置为 null，这意味着我们在加载此快照时不会比较摘要，预计会在滚动升级期间发生
  4. 使用旧代码加载新快照时，将数据树反序列化后成功完成，快照文件末尾的摘要值将被忽略
  5. 滚动重启和flags变化的场景与上面讨论的第1和第2场景类似，如果leader启用但follower没有，digest值将被忽略，follower不会在运行时比较digest；如果领导者禁用但追随者启用，追随者将获得优雅处理的 EOF 异常。

  注意：由于 /zookeeper/quota stat 节点中潜在的不一致，当前的摘要计算排除了 /zookeeper 下的节点，我们可以在解决该问题后将其包含在内。

  默认情况下，此功能处于启用状态，设置“false”以禁用它。

- *snapshot.compression.method*：（Java 系统属性：**zookeeper.snapshot.compression.method**）**3.6.0 新增：**此属性控制 ZooKeeper 是否应在将快照存储到磁盘之前对其进行压缩（请参阅[ZOOKEEPER-3179](https://issues.apache.org/jira/browse/ZOOKEEPER-3179)）。可能的值为：

  - ""：禁用（无快照压缩）。这是默认行为。
  - “gz”：参见[gzip 压缩](https://en.wikipedia.org/wiki/Gzip)。
  - “snappy”：请参阅[Snappy 压缩](https://en.wikipedia.org/wiki/Snappy_(compression))。

- *snapshot.trust.empty*：（Java 系统属性：**zookeeper.snapshot.trust.empty**）**3.5.6 新增：**此属性控制 ZooKeeper 是否应将丢失的快照文件视为无法恢复的致命状态。设置为 true 以允许 ZooKeeper 服务器在没有快照文件的情况下恢复。这应该只在从旧版本的 ZooKeeper（3.4.x，3.5.3 之前）升级期间设置，其中 ZooKeeper 可能只有事务日志文件，但不存在快照文件。如果升级时设置了该值，我们建议升级后将该值设置回false并重启ZooKeeper进程，以便ZooKeeper在恢复过程中继续进行正常的数据一致性检查。默认值为假。

- *audit.enable*：（Java 系统属性：**zookeeper.audit.enable**）**3.6.0 中的新功能：**默认情况下禁用审核日志。设置为“true”以启用它。默认值为“假”。有关详细信息，请参阅[ZooKeeper 审核日志](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAuditLogs.html)。

- *audit.impl.class* : (Java 系统属性：**zookeeper.audit.impl.class** ) **3.6.0 新增：**实现审计记录器的类。默认情况下，使用基于 log4j 的审计记录器 org.apache.zookeeper.audit .Log4jAuditLogger。有关详细信息，请参阅[ZooKeeper 审核日志](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAuditLogs.html)。

- *largeRequestMaxBytes* : (Java 系统属性：**zookeeper.largeRequestMaxBytes** ) **3.6.0 新增：**所有进行中的大请求的最大字节数。如果即将到来的大请求导致超出限制，则连接将关闭。默认为 100 * 1024 * 1024。

- *largeRequestThreshold* : (Java 系统属性：**zookeeper.largeRequestThreshold** ) **3.6.0 中的新功能：**请求被视为大型请求的大小阈值。如果它是 -1，则所有请求都被认为是小请求，从而有效地关闭了大请求限制。默认值为 -1。

- *outstandingHandshake.limit*（仅限 Java 系统属性：**zookeeper.netty.server.outstandingHandshake.limit**）ZooKeeper 中最大的动态 TLS 握手连接，超过此限制的连接将在开始握手之前被拒绝。此设置不限制最大 TLS 并发性，但有助于避免在进行中的 TLS 握手过多时由于 TLS 握手超时而导致的羊群效应。将其设置为 250 之类的值就足以避免羊群效应。

- *ThrottledOpWaitTime*（Java 系统属性：**zookeeper.throttled_op_wait_time**）RequestThrottler 队列中的时间长于请求将被标记为受限制的时间。除了被送入它所属的服务器的管道以保留所有请求的顺序之外，不会处理被限制的请求。FinalProcessor 将针对这些未消化的请求发出错误响应（新错误代码：ZTHROTTLEDOP）。目的是让客户端不要立即重试。设置为 0 时，不会限制任何请求。默认值为 0。

- *learner.closeSocketAsync*（仅限 Java 系统属性：**learner.closeSocketAsync**）**3.6.2 中的新增**功能**：**启用后，学习器将异步关闭仲裁套接字。这对于关闭套接字可能需要很长时间、阻止关闭过程、可能延迟新领导者选举以及使仲裁不可用的 TLS 连接非常有用。尽管套接字关闭时间很长，但异步关闭套接字可以避免阻塞关闭过程，并且可以在套接字关闭的同时开始新的领导者选举。默认值为假。

- *leader.closeSocketAsync*（仅限 Java 系统属性：**leader.closeSocketAsync**）**3.6.2 新增：**启用后，领导者将异步关闭仲裁套接字。这对于关闭套接字可能需要很长时间的 TLS 连接很有用。如果由于 SyncLimitCheck 失败而在 ping() 中断开跟随者的连接，则较长的套接字关闭时间将阻止向其他跟随者发送 ping。没有收到ping，其他follower不会向leader发送会话信息，这会导致会话过期。将此标志设置为 true 可确保定期发送 ping。默认值为假。

- *forward_learner_requests_to_commit_processor_disabled* (Jave 系统属性：**zookeeper.forward_learner_requests_to_commit_processor_disabled** ) 当这个属性被设置时，来自学习者的请求不会被排队到 CommitProcessor 队列，这将有助于节省资源和领导者的 GC 时间。

  默认值为假。

#### 4.3集群选项

本节中的选项设计用于服务器集合——即部署服务器集群时。

- electionAlg：（无 Java 系统属性）要使用的选举实现。值“1”对应的是非认证的基于UDP的快速leader选举版本，“2”对应的是经过认证的基于UDP的快速leader选举版本，“3”对应的是基于TCP的快速leader选举版本选举。算法 3 在 3.2.0 中是默认的，之前的版本（3.0.0 和 3.1.0）也使用算法 1 和 2。

  > 领导选举 1 和 2 的实现在 3.4.0 中被**弃用**。由于 3.6.0 只有 FastLeaderElection 可用，在升级的情况下，您必须关闭所有服务器并使用electionAlg=3 重新启动它们（或通过从配置文件中删除该行）。>

- *maxTimeToWaitForEpoch* : (Java 系统属性：**zookeeper.leader.maxTimeToWaitForEpoch** ) **3.6.0 新增：**激活领导者时等待选民的最长时间。如果领导者收到了来自其中一个选民的 LOOKING 通知，并且在 maxTimeToWaitForEpoch 内没有收到来自多数人的 epoch 数据包，那么它将转到 LOOKING 并再次选举领导者。这可以调整以减少法定人数或服务器不可用时间，它可以设置为远小于 initLimit * tickTime。在跨数据中心环境中，它可以设置为 2s 之类的东西。

- *initLimit*：（无 Java 系统属性）允许追随者连接并同步到领导者的时间量，以滴答为单位（参见[tickTime](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#id_tickTime)）。如果 ZooKeeper 管理的数据量很大，则根据需要增加此值。

- *connectToLearnerMasterLimit*：（Java系统属性：饲养员**connectToLearnerMasterLimit**）的时间，在蜱（见[滚动时间](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#id_tickTime)），以使追随者领导人选举后与领导。默认为 initLimit 的值。当 initLimit 很高时使用，因此连接到学习者主节点不会导致更高的超时。

- *leaderServes*：（Java系统属性：饲养员**leaderServes**）负责接受客户端连接。默认值为“是”。领导机坐标更新。为了以轻微的读取吞吐量为代价获得更高的更新吞吐量，领导者可以配置为不接受客户端并专注于协调。此选项的默认值为 yes，这意味着领导者将接受客户端连接。

  > 当一个集合中有超过三个 ZooKeeper 服务器时，强烈建议打开领导者选择。

- server.x=[hostname]:nnnnn[:nnnnn] 等：（无 Java 系统属性）组成 ZooKeeper 集合的服务器。当服务器启动时，它通过在数据目录中查找文件 myid 来确定它是哪个服务器。该文件包含了服务器数量，在ASCII，它应该与X在server.x在此设置的左侧。客户端使用的组成 ZooKeeper 服务器的服务器列表必须与每个 ZooKeeper 服务器拥有的 ZooKeeper 服务器列表相匹配。有两个端口号nnnnn。第一个follower用于连接leader，第二个用于leader选举。如果你想在一台机器上测试多个服务器，那么每个服务器可以使用不同的端口。

  从 ZooKeeper 3.6.0 开始，可以为每个 ZooKeeper 服务器指定**多个地址**（参见[ZOOKEEPER-3188](https://issues.apache.org/jira/projects/ZOOKEEPER/issues/ZOOKEEPER-3188)）。要启用此功能，您必须将*multiAddress.enabled*配置属性设置为*true*。这有助于提高可用性并为 ZooKeeper 添加网络级别的弹性。当服务器使用多个物理网络接口时，ZooKeeper 能够绑定所有接口，并在网络错误的情况下运行时切换到工作接口。可以使用管道 ('|') 字符在配置中指定不同的地址。使用多个地址的有效配置如下所示：

  ```
  server.1=zoo1-net1:2888:3888|zoo1-net2:2889:3889
  server.2=zoo2-net1:2888:3888|zoo2-net2:2889:3889
  server.3=zoo3-net1:2888:3888|zoo3-net2:2889:3889
  ```

  > 通过启用此功能，Quorum 协议（ZooKeeper Server-Server 协议）将发生变化。用户不会注意到这一点，当任何人使用新配置启动 ZooKeeper 集群时，一切都会正常工作。但是，如果旧的 ZooKeeper 集群不支持*multiAddress*功能（和新的 Quorum 协议），则无法启用此功能并在滚动升级期间指定多个地址。如果您需要此功能但还需要从*3.6.0 之前*的 ZooKeeper 集群执行滚动升级，那么您首先需要在不启用 MultiAddress 功能的情况下进行滚动升级，然后使用新的单独的滚动重启**multiAddress.enabled**设置为**true 的**配置 并提供多个地址。

- *syncLimit*：（无 Java 系统属性）允许关注者与 ZooKeeper 同步的时间量，以滴答为单位（参见[tickTime](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#id_tickTime)）。如果追随者落后于领导者太远，他们将被淘汰。

- *group.x=nnnnn[:nnnnn]*：（无 Java 系统属性）启用分层仲裁构造。“x”是组标识符，“=”符号后面的数字对应于服务器标识符。赋值的左侧是一个以冒号分隔的服务器标识符列表。请注意，组必须是不相交的，并且所有组的并集必须是 ZooKeeper 集合。你会[在这里](https://zookeeper.apache.org/doc/r3.7.0/zookeeperHierarchicalQuorums.html)找到一个例子

- *weight.x=nnnnn*：（无 Java 系统属性）与“组”一起使用，它在形成法定人数时为服务器分配权重。这样的值对应于投票时服务器的权重。ZooKeeper 的一些部分需要投票，例如领导者选举和原子广播协议。默认情况下，服务器的权重为 1。如果配置定义了组，但没有定义权重，那么值 1 将分配给所有服务器。你会[在这里](https://zookeeper.apache.org/doc/r3.7.0/zookeeperHierarchicalQuorums.html)找到一个例子

- *cnxTimeout*：（Java系统属性：饲养员**cnxTimeout**）设置为打开领导人选举通知连接超时值。仅适用于使用electionAlg 3 的情况。

  > 默认值为 5 秒。

- quorumCnxnTimeoutMs：（Java系统属性：饲养员quorumCnxnTimeoutMs）设置为领导人选举通知的连接读取超时值。仅适用于使用electionAlg 3 的情况。

  > 默认值为 -1，然后将使用 syncLimit * tickTime 作为超时。

- *StandaloneEnabled*：（无 Java 系统属性）**3.5.0 新增功能：**设置为 false 时，可以在复制模式下启动单个服务器，单独的参与者可以与观察者一起运行，集群可以重新配置为一个节点，从一个节点。对于向后兼容性，默认值为 true。可以使用 QuorumPeerConfig 的 setStandaloneEnabled 方法或通过将“standaloneEnabled=false”或“standaloneEnabled=true”添加到服务器的配置文件来设置它。

- *reconfigEnabled*：（无 Java 系统属性）**3.5.3 中的新增功能：**这控制启用或禁用[动态重新配置](https://zookeeper.apache.org/doc/r3.7.0/zookeeperReconfig.html)功能。启用该功能后，用户可以通过 ZooKeeper 客户端 API 或通过 ZooKeeper 命令行工具执行重新配置操作，前提是用户有权执行此类操作。当该功能被禁用时，包括超级用户在内的任何用户都不能执行重新配置。任何重新配置的尝试都将返回错误。**“reconfigEnabled”**选项可以设置为**“reconfigEnabled=false”**或**“reconfigEnabled=true”**到服务器的配置文件，或使用 QuorumPeerConfig 的 setReconfigEnabled 方法。默认值为假。如果存在，该值应该在整个整体中的每个服务器上保持一致。在某些服务器上将该值设置为 true 而在其他服务器上设置为 false 会导致不一致的行为，具体取决于哪个服务器被选为领导者。如果领导者的设置为**"reconfigEnabled=true"**，那么集成将启用重新配置功能。如果领导者的设置为**"reconfigEnabled=false"**，则集成将禁用重新配置功能。因此，建议在整体中跨服务器的**“reconfigEnabled”**具有一致的值。

- *4lw.commands.whitelist*：（Java 系统属性：**zookeeper.4lw.commands.whitelist**）**3.5.3 中的新功能：**用户想要使用的逗号分隔的[四字母词](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#sc_4lw)命令列表。必须在此列表中放入有效的四字母词命令，否则 ZooKeeper 服务器将不会启用该命令。默认情况下，白名单只包含 zkServer.sh 使用的“srvr”命令。默认情况下禁用其余的四字母单词命令。以下是启用 stat、ruok、conf 和 isro 命令同时禁用四字母词命令的其余部分的配置示例：

  ```
  4lw.commands.whitelist=stat, ruok, conf, isro
  ```

如果您真的需要默认启用所有四个字母的单词命令，您可以使用星号选项，这样您就不必将每个命令都一一包含在列表中。例如，这将启用所有四个字母的单词命令：

```
4lw.commands.whitelist=*
```

- *tcpKeepAlive*：（Java 系统属性：**zookeeper.tcpKeepAlive**）**3.5.4 中的新增功能：**将此设置为 true 会在仲裁成员用于执行选举的套接字上设置 TCP keepAlive 标志。这将允许仲裁成员之间的连接在存在可能会破坏它们的网络基础设施时保持连接。某些 NAT 和防火墙可能会因长时间运行或空闲连接而终止或丢失状态。启用此选项依赖于操作系统级别的设置才能正常工作，请检查您的操作系统关于 TCP keepalive 的选项以获取更多信息。默认为**false**。
- *clientTcpKeepAlive*：（Java 系统属性：**zookeeper.clientTcpKeepAlive**）**3.6.1 中的新功能：**将此设置为 true 会在客户端套接字上设置 TCP keepAlive 标志。一些损坏的网络基础设施可能会丢失从关闭客户端发送的 FIN 数据包。这些从未关闭的客户端套接字会导致操作系统资源泄漏。启用此选项会通过空闲检查终止这些僵尸套接字。启用此选项依赖于操作系统级别的设置才能正常工作，请检查您的操作系统关于 TCP keepalive 的选项以获取更多信息。默认为**false**。请注意它和**tcpKeepAlive**之间的区别。它应用于客户端套接字，而**tcpKeepAlive**用于仲裁成员使用的套接字。目前此选项仅在使用默认值时可用`NIOServerCnxnFactory`。
- *electionPortBindRetry*：（仅限Java 系统属性：**zookeeper.electionPortBindRetry**）当Zookeeper 服务器无法绑定leader 选举端口时，属性设置最大重试次数。此类错误可以是临时的且可恢复的，例如[ZOOKEEPER-3320 中](https://issues.apache.org/jira/projects/ZOOKEEPER/issues/ZOOKEEPER-3320)描述的 DNS 问题，也可以是不可重试的，例如已在使用的端口。在出现暂时性错误的情况下，此属性可以提高 Zookeeper 服务器的可用性并帮助其自我恢复。默认值 3。在容器环境中，尤其是在 Kubernetes 中，该值应增加或设置为 0（无限重试）以解决与 DNS 名称解析相关的问题。
- *observer.reconnectDelayMs* : (Java 系统属性：**zookeeper.observer.reconnectDelayMs** ) 当观察者失去与领导者的连接时，它会等待指定的值，然后再尝试与领导者重新连接，这样整个观察者舰队就不会尝试运行领导者立即选举并重新连接到领导者。默认为 0 毫秒。
- *observer.election.DelayMs*：（Java 系统属性：**zookeeper.observer.election.DelayMs**）在断开连接时延迟观察者参与领导者选举，以防止在此过程中对投票节点产生意外的额外负载。默认为 200 毫秒。
- *localSessionsEnabled*和*localSessionsUpgradingEnabled*：**3.5 中的新增功能：**可选值为 true 或 false。它们的默认值为 false。通过设置*localSessionsEnabled=true*打开本地会话功能。打开*localSessionsUpgradingEnabled*可以根据需要自动将本地会话升级为全局会话（例如创建临时节点），这仅在启用*localSessionsEnabled*时才重要。

#### 4.4加密、身份验证、授权选项

本节中的选项允许控制服务执行的加密/身份验证/授权。

- *DigestAuthenticationProvider.enabled* : (Java 系统属性：**zookeeper.DigestAuthenticationProvider.enabled** ) **3.7 中的新功能：**确定是否`digest`启用身份验证提供程序。默认值为**true**以实现向后兼容性，但如果不使用则禁用此提供程序可能是个好主意，因为它可能导致审核日志中出现误导性条目（请参阅[ZOOKEEPER-3979](https://issues.apache.org/jira/browse/ZOOKEEPER-3979)）

- *DigestAuthenticationProvider.superDigest*：（Java 系统属性：**zookeeper.DigestAuthenticationProvider.superDigest**）默认情况下**禁用** 此功能**3.2 中的新增**功能**：**使 ZooKeeper 集成管理员能够以“超级”用户身份访问 znode 层次结构。特别是对于被认证为 super 的用户，不会进行 ACL 检查。可以使用 org.apache.zookeeper.server.auth.DigestAuthenticationProvider 生成 superDigest，使用一个参数“super:”。提供生成的“super:“作为启动集成的每个服务器时的系统属性值。当向 ZooKeeper 服务器（来自 ZooKeeper 客户端）进行身份验证时，传递“摘要”和“超级”身份验证数据的方案："。请注意，digest auth 将 authdata 以明文形式传递给服务器，谨慎的做法是仅在本地主机上（而不是通过网络）或通过加密连接使用此身份验证方法。

- *DigestAuthenticationProvider.digestAlg* : (Java 系统属性：**zookeeper.DigestAuthenticationProvider.digestAlg** ) **3.7.0 新增：**设置 ACL 摘要算法。默认值是：`SHA1`将来会因安全问题而被弃用。在所有服务器中将此属性设置为相同的值。

  - 如何支持其他更多的算法？

    - 通过指定修改下的`java.security`配置文件`$JAVA_HOME/jre/lib/security/java.security`：`security.provider.<n>=<provider class name>`.

      `For example: set zookeeper.DigestAuthenticationProvider.digestAlg=RipeMD160 security.provider.3=org.bouncycastle.jce.provider.BouncyCastleProvider`

    - 将 jar 文件复制到`$JAVA_HOME/jre/lib/ext/`.

      `For example: copy bcprov-jdk15on-1.60.jar to $JAVA_HOME/jre/lib/ext/`

  - 如何从一种摘要算法迁移到另一种算法？

    - 1. `superDigest`迁移到新算法时重新生成。
    - 1. `SetAcl` 对于已经具有旧算法摘要身份验证的 znode。

- *X509AuthenticationProvider.superUser*：（Java 系统属性：**zookeeper.X509AuthenticationProvider.superUser**）SSL 支持的方式，使 ZooKeeper 集成管理员能够以“超级”用户身份访问 znode 层次结构。当此参数设置为 X500 主体名称时，只有具有该主体的经过身份验证的客户端才能绕过 ACL 检查并拥有对所有 znode 的完全权限。

- *zookeeper.superUser*：（Java 系统属性：**zookeeper.superUser**）类似于**zookeeper.X509AuthenticationProvider.superUser，**但对于基于 SASL 的登录是通用的。它存储可以作为“超级”用户访问 znode 层次结构的用户的名称。您可以使用**zookeeper.superUser.[suffix]**表示法指定多个 SASL 超级用户，例如：`zookeeper.superUser.1=...`。

- *ssl.authProvider*：（Java 系统属性：**zookeeper.ssl.authProvider**）指定用于安全客户端身份验证的**org.apache.zookeeper.auth.X509AuthenticationProvider**的子类。这在不使用 JKS 的证书密钥基础结构中很有用。可能需要扩展**javax.net.ssl.X509KeyManager**和**javax.net.ssl.X509TrustManager**以从 SSL 堆栈中获得所需的行为。要将 ZooKeeper 服务器配置为使用自定义提供程序进行身份验证，请为自定义 AuthenticationProvider 选择方案名称并设置属性**zookeeper.authProvider.[scheme]**到自定义实现的完全限定类名。这会将提供者加载到 ProviderRegistry 中。然后设置此属性**zookeeper.ssl.authProvider=[scheme]**并且该提供程序将用于安全身份验证。

- *zookeeper.ensembleAuthName*：（仅限 Java 系统属性：**zookeeper.ensembleAuthName**）**3.6.0 中的新增功能：**指定一个以逗号分隔的集合的有效名称/别名列表。客户端可以提供它打算连接的集合名称作为方案“集合”的凭据。EnsembleAuthenticationProvider 将根据接收连接请求的集合的名称/别名列表检查凭证。如果凭证不在列表中，连接请求将被拒绝。这可以防止客户端意外连接到错误的集合。

- *sessionRequireClientSASLAuth* : (Java 系统属性：**zookeeper.sessionRequireClientSASLAuth** ) **3.6.0 新增：**当设置为**true 时**，ZooKeeper 服务器将只接受来自通过 SASL 与服务器进行身份验证的客户端的连接和请求。未配置 SASL 身份验证，或配置了 SASL 但身份验证失败（即，凭据无效）的客户端将无法与服务器建立会话。在这种情况下，将传递键入的错误代码 (-124)，此后 Java 和 C 客户端都将关闭与服务器的会话，不再尝试重试重新连接。

  此配置是**enforce.auth.enabled=true**和**enforce.auth.scheme=sasl 的简写**

  默认情况下，此功能处于禁用状态。想要选择加入的用户可以通过将**sessionRequireClientSASLAuth**设置为**true**来启用该功能。

  此功能否决了 zookeeper.allowSaslFailedClients 选项，因此即使服务器配置为允许未通过 SASL 身份验证的客户端登录，如果启用此功能，客户端将无法与服务器建立会话。

- *强制.auth.enabled*：（Java 系统属性：**zookeeper.enforce.auth.enabled**）**3.7.0 中的新增功能：**当设置为**true 时**，ZooKeeper 服务器将只接受来自已通过配置的身份验证方案与服务器进行身份验证的客户端的连接和请求。可以使用属性enforce.auth.schemes 配置身份验证方案。未使用在服务器上配置的任何身份验证方案或已配置但身份验证失败（即具有无效凭据）的客户端将无法与服务器建立会话。在这种情况下，将传递键入的错误代码 (-124)，此后 Java 和 C 客户端都将关闭与服务器的会话，不再尝试重试重新连接。

  默认情况下，此功能处于禁用状态。想要选择加入的用户可以通过将**enforce.auth.enabled**设置为**true**来启用该功能。

  当**enforce.auth.enabled =真**和**enforce.auth.schemes = SASL**然后zookeeper.allowSaslFailedClients配置被否决。因此，即使服务器配置为允许未通过 SASL 身份验证的客户端登录，如果使用 sasl 作为身份验证方案启用此功能，客户端将无法与服务器建立会话。

- *强制.auth.schemes*：（Java 系统属性：**zookeeper.enforce.auth.schemes**）**3.7.0 中的新功能：**逗号分隔的身份验证方案列表。在执行任何 zookeeper 操作之前，客户端必须至少通过一种身份验证方案进行身份验证。仅当**enforce.auth.enabled**为**true**时才使用此属性。

- *sslQuorum*：（Java 系统属性：**zookeeper.sslQuorum**）**3.5.5 中的新增功能：**启用加密的仲裁通信。默认为`false`。启用此功能时，还请考虑启用*leader.closeSocketAsync*和*learner.closeSocketAsync*以避免在关闭 SSL 连接时与潜在的较长套接字关闭时间相关的问题。

- *ssl.keyStore.location 和 ssl.keyStore.password*和*ssl.quorum.keyStore.location*和*ssl.quorum.keyStore.password* : (Java 系统属性：**zookeeper.ssl.keyStore.location**和**zookeeper.ssl.keyStore.password**和**zookeeper .ssl.quorum.keyStore.location**和**zookeeper.ssl.quorum.keyStore.password** ) **3.5.5 中的新增功能：**指定 Java 密钥库的文件路径，其中包含用于客户端和仲裁 TLS 连接的本地凭据以及密码解锁文件。

- *ssl.keyStore.type*和*ssl.quorum.keyStore.type*：（Java 系统属性：**zookeeper.ssl.keyStore.type**和**zookeeper.ssl.quorum.keyStore.type**）**3.5.5 新增：**指定客户端和仲裁密钥库。值：JKS、PEM、PKCS12 或 null（按文件名检测）。默认值：空。3.6.3、3.7.0 中的新增功能**：**添加了 BCFKS 格式。

- *ssl.trustStore.location*和*ssl.trustStore.password*和*ssl.quorum.trustStore.location*和*ssl.quorum.trustStore.password*：（Java 系统属性：**zookeeper.ssl.trustStore.location**和**zookeeper.ssl.trustStore.password**和**zookeeper .ssl.quorum.trustStore.location**和**zookeeper.ssl.quorum.trustStore.password** ) **3.5.5 中的新增功能：**指定 Java 信任库的文件路径，其中包含用于客户端和仲裁 TLS 连接的远程凭据以及密码解锁文件。

- *ssl.trustStore.type*和*ssl.quorum.trustStore.type*：（Java 系统属性：**zookeeper.ssl.trustStore.type**和**zookeeper.ssl.quorum.trustStore.type**）**3.5.5 新增：**指定客户端和法定人数信任商店。值：JKS、PEM、PKCS12 或 null（按文件名检测）。默认值：空。3.6.3、3.7.0 中的新增功能**：**添加了 BCFKS 格式。

- *ssl.protocol*和*ssl.quorum.protocol*：（Java 系统属性：**zookeeper.ssl.protocol**和**zookeeper.ssl.quorum.protocol**）**3.5.5 中的新增功能：**指定要在客户端和仲裁 TLS 协商中使用的协议。默认值：TLSv1.2

- *ssl.enabledProtocols*和*ssl.quorum.enabledProtocols*：（Java 系统属性：**zookeeper.ssl.enabledProtocols**和**zookeeper.ssl.quorum.enabledProtocols**）**3.5.5 中的新增**功能**：**指定客户端和仲裁 TLS 协商中启用的协议。默认值：`protocol`属性值

- *ssl.ciphersuites*和*ssl.quorum.ciphersuites*：（Java 系统属性：**zookeeper.ssl.ciphersuites**和**zookeeper.ssl.quorum.ciphersuites**）**3.5.5 中的新增**功能**：**指定在客户端和仲裁 TLS 协商中使用的已启用密码套件。默认值：启用的密码套件取决于所使用的 Java 运行时版本。

- *ssl.context.supplier.class*和*ssl.quorum.context.supplier.class*：（Java 系统属性：**zookeeper.ssl.context.supplier.class**和**zookeeper.ssl.quorum.context.supplier.class**）**3.5.5 新增：**指定用于在客户端和仲裁 SSL 通信中创建 SSL 上下文的类。这允许您使用自定义 SSL 上下文并实现以下场景：

  1. 使用硬件密钥库，使用 PKCS11 或类似的东西加载。
  2. 您无权访问软件密钥库，但可以从其容器中检索已构建的 SSLContext。默认值：空

- *ssl.hostnameVerification*和*ssl.quorum.hostnameVerification* : (Java 系统属性：**zookeeper.ssl.hostnameVerification**和**zookeeper.ssl.quorum.hostnameVerification** ) **3.5.5 新增：**指定是否在客户端和仲裁 TLS 协商过程中启用主机名验证。仅建议出于测试目的禁用它。默认值：真

- *ssl.crl*和*ssl.quorum.crl*：（Java 系统属性：**zookeeper.ssl.crl**和**zookeeper.ssl.quorum.crl**）**3.5.5 中的新增功能：**指定是否在客户端和仲裁 TLS 协议中启用证书吊销列表。默认值：假

- *ssl.ocsp*和*ssl.quorum.ocsp*：（Java 系统属性：**zookeeper.ssl.ocsp**和**zookeeper.ssl.quorum.ocsp**）**3.5.5 中的新增功能：**指定是否在客户端和仲裁 TLS 协议中启用在线证书状态协议。默认值：假

- *ssl.clientAuth*和*ssl.quorum.clientAuth*：（Java 系统属性：**zookeeper.ssl.clientAuth**和**zookeeper.ssl.quorum.clientAuth**）**在 3.5.5 中添加，但在 3.5.7 之前中断：**指定用于验证来自客户端的 ssl 连接的选项. 有效值为

  - “none”：服务器不会请求客户端身份验证
  - “想要”：服务器将“请求”客户端身份验证
  - “需要”：服务器将“要求”客户端身份验证

  默认值：“需要”

- *ssl.handshakeDetectionTimeoutMillis*和*ssl.quorum.handshakeDetectionTimeoutMillis* :（Java 系统属性：**zookeeper.ssl.handshakeDetectionTimeoutMillis**和**zookeeper.ssl.quorum.handshakeDetectionTimeoutMillis**）**3.5.5 中的新增功能：**待定

- *client.portUnification*：（Java 系统属性：**zookeeper.client.portUnification**）指定客户端端口应接受 SSL 连接（使用与安全客户端端口相同的配置）。默认值：假

- *authProvider*：（Java 系统属性：**zookeeper.authProvider**）您可以为 ZooKeeper 指定多个身份验证提供程序类。通常您使用此参数来指定 SASL 身份验证提供程序，例如：`authProvider.1=org.apache.zookeeper.server.auth.SASLAuthenticationProvider`

- *kerberos.removeHostFromPrincipal*（Java 系统属性：**zookeeper.kerberos.removeHostFromPrincipal**） 您可以指示 ZooKeeper 在身份验证期间从客户端主体名称中删除主机。（例如 zk/myhost@EXAMPLE.COM 客户端主体将在 ZooKeeper 中作为 zk@EXAMPLE.COM 进行身份验证）默认值：false

- *kerberos.removeRealmFromPrincipal*（Java 系统属性：**zookeeper.kerberos.removeRealmFromPrincipal**） 您可以指示 ZooKeeper 在身份验证期间从客户端主体名称中删除领域。（例如 zk/myhost@EXAMPLE.COM 客户端主体将在 ZooKeeper 中作为 zk/myhost 进行身份验证）默认值：false

- *kerberos.canonicalizeHostNames*（Java 系统属性：**zookeeper.kerberos.canonicalizeHostNames**）**3.7.0 中的新增功能：**指示 ZooKeeper 规范化从*server.x*行中提取的服务器主机名。这允许使用例如`CNAME`记录来引用配置文件中的服务器，同时仍然在仲裁成员之间启用 SASL Kerberos 身份验证。它本质上是客户端的*zookeeper.sasl.client.canonicalize.hostname*属性的法定人数。为了向后兼容，默认值为**false**。

- *multiAddress.enabled* : (Java 系统属性：**zookeeper.multiAddress.enabled** ) **3.6.0 新增功能：**从 ZooKeeper 3.6.0 开始，您还可以为每个 ZooKeeper 服务器实例[指定多个地址](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#id_multi_address)（当可以使用多个物理网络接口时，这可以提高可用性在集群中并行）。将此参数设置为**true**将启用此功能。请注意，如果旧的 ZooKeeper 集群的版本在 3.6.0 之前，则您无法在滚动升级期间启用此功能。默认值为**false**。

- *multiAddress.reachabilityCheckTimeoutMs* : (Java 系统属性：**zookeeper.multiAddress.reachabilityCheckTimeoutMs** ) **3.6.0 新增：**从 ZooKeeper 3.6.0 开始，您还可以[指定多个地址](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#id_multi_address)对于每个 ZooKeeper 服务器实例（当可以在集群中并行使用多个物理网络接口时，这可以提高可用性）。ZooKeeper 将执行 ICMP ECHO 请求或尝试在目标主机的端口 7（Echo）上建立 TCP 连接，以找到可访问的地址。仅当您在配置中提供多个地址时才会发生这种情况。在此属性中，您可以设置可达性检查的超时时间（以毫秒为单位）。不同地址的检查是并行进行的，因此您在此处设置的超时时间是检查所有地址的可达性所花费的最长时间。默认值为**1000**。

  此参数无效，除非您通过设置*multiAddress.enabled=true*启用 MultiAddress 功能。

#### 4.5不安全的选项

以下选项可能很有用，但在使用它们时要小心。解释每个风险的同时解释变量的作用。

- *forceSync*：（Java 系统属性：**zookeeper.forceSync**）要求在完成更新处理之前将更新同步到事务日志的媒体。如果此选项设置为 no，ZooKeeper 将不需要将更新同步到媒体。

- *jute.maxbuffer*：（Java 系统属性：**jute.maxbuffer**）。

  - 此选项只能设置为 Java 系统属性。上面没有zookeeper前缀。它指定可以存储在 znode 中的数据的最大大小。单位为：字节。默认值为 0xfffff(1048575) 字节，或略低于 1M。
  - 如果更改此选项，则必须在所有服务器和客户端上设置系统属性，否则会出现问题。
  - 当客户端的*jute.maxbuffer*大于服务器端的时候，客户端要写的数据超过了服务器端的*jute.maxbuffer*，服务器端会得到**java.io.IOException: Len错误**
  - 当客户端的*jute.maxbuffer*小于服务器端，客户端要读取的数据超过客户端的*jute.maxbuffer*时，客户端会得到**java.io.IOException: Unreasonable length** or **Packet len is out of范围！**
  - 这真的是一个健全的检查。ZooKeeper 旨在以千字节的大小存储数据。在生产环境中，不建议将此属性增加到超过默认值，原因如下：
  - 大型 znode 会导致不必要的延迟峰值，从而降低吞吐量
  - 大型znodes导致leader和follower同步时间不可预测且不收敛（有时会超时），导致quorum不稳定

- *jute.maxbuffer.extrasize*：（Java 系统属性：**zookeeper.jute.maxbuffer.extrasize**）**3.5.7 中的新功能：**在处理客户端请求时，ZooKeeper 服务器在将其作为事务持久化之前向请求中添加一些附加信息。早些时候，此附加信息大小固定为 1024 字节。对于很多场景，特别是 jute.maxbuffer 值大于 1 MB 且请求类型多的场景，这个固定大小是不够的。为了处理所有场景，附加信息大小从 1024 字节增加到与 jute.maxbuffer 大小相同，并且它也可以通过 jute.maxbuffer.extrasize 进行配置。通常不需要配置此属性，因为默认值是最佳值。

- *skipACL*：（Java 系统属性：**zookeeper.skipACL**）跳过 ACL 检查。这会提高吞吐量，但对每个人都开放了对数据树的完全访问。

- *quorumListenOnAllIPs*：当设置为 true 时，ZooKeeper 服务器将在所有可用 IP 地址上侦听来自其对等方的连接，而不仅仅是在配置文件的服务器列表中配置的地址。它会影响处理 ZAB 协议和 Fast Leader Election 协议的连接。默认值为**false**。

- *multiAddress.reachabilityCheckEnabled* : (Java 系统属性：**zookeeper.multiAddress.reachabilityCheckEnabled** ) **3.6.0 新增功能：**从 ZooKeeper 3.6.0 开始，您还可以为每个 ZooKeeper 服务器实例[指定多个地址](https://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#id_multi_address)（当可以使用多个物理网络接口时，这可以提高可用性在集群中并行）。ZooKeeper 将执行 ICMP ECHO 请求或尝试在目标主机的端口 7（Echo）上建立 TCP 连接，以找到可访问的地址。仅当您在配置中提供多个地址时才会发生这种情况。如果您尝试在单台机器上启动大型（例如 11+）集成成员集群进行测试时遇到某些 ICMP 速率限制（例如在 MacOS 上），则可达性检查可能会失败。

  默认值为**true**。通过将此参数设置为“false”，您可以禁用可达性检查。请注意，禁用可达性检查将导致集群在网络问题期间无法正确重新配置自身，因此建议仅在测试期间禁用。

  此参数无效，除非您通过设置*multiAddress.enabled=true*启用 MultiAddress 功能。