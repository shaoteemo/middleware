# Apache Hadoop（HDFS）教程中文版

注意本文档介绍Hadoop相关内容。实践其HDFS组件，其他组件将在其他文档中体现。

## 写在前面

撰写人：ShaoTeemo

本文档包含了HDFS的使用和部分原理。其中包含大量官方文档引用及部分三方文档链接引用。后期会翻译部分HDFS原理相关内容。

有较详细的项目搭建过程的文件信息，*所有内容均在指定的环境中测试运行通过*，请**注意环境信息的匹配**。

文中出现的译注为本人(译者)观点。

## 环境信息

|      技术栈      |            版本号             |
| :--------------: | :---------------------------: |
|   Spring-Boot    |            2.7.18             |
|      Hadoop      |             3.3.6             |
| hadoop-clientAPI |             3.3.6             |
|    Zookeeper     |             3.7.2             |
|    Oracle JDK    |          11.0.24/1.8          |
|      Linux       | CentOS Linux release 8.4.2105 |
|      Maven       |             3.9.8             |

图形绘制：Microsoft Office Visio 

## 问题引出

### 分治思想

1. 十万个元素（单词）需要存储，如何存储？

   数组、ArrayList、LinkedList、HashMap

2. 如果想查找某一个元素，最简单的遍历方式的复杂度是多少？

   如果是数组或链表则复杂度为：O(n)

3. 如果我们期望复杂度是O(4)呢？

   使用Hash表存储（或二维数组）则复杂的为：O(4)

   此方式具有分治思想

分而治之的思想非常重要，常见于以下技术：

1. Redis集群：Hash槽
2. Hadoop
3. Hbase
4. ElasticSearch

### 单机处理大数据的问题

有一个非常大的文本文件，里面有非常多的行，只有两行内容一样，它们出现在未知的位置，需要查找到它们。硬件：单台机器，而且可用的内存很少，也就500MB。

- 假如I/O速度是500MB/s 1024 000 MB/500（MB/s） = 1000*2s

- 1T文件读取一遍需要约30分钟
- 循环遍历需要N次I/O时间
- 分治思想可以使时间降为2次I/O

**如何对1TB文件进行排序？**

- 假如I/O速度是500MB/s
- 1T文件读取一遍需要约30分钟

方式1：外部有序，内部无序，然后再逐一读入内存排序

方式2：逐一读取500M排序，内部有序，外部无序 ，然后进行归并排序

### 集群分布式处理大数据优劣

需求：

- 有一个非常大的文本文件，里面有几百亿行，只有两行内容一样，它们出现在未知的位置，需要查找到它们。
- 分钟、秒级别完成
- 硬件：*台机器，而且可用的内存500MB。

**2000台真的比一台快吗？**

由于涉及到计算机之间文件传输，千兆带宽1000Mb/S也就是100MB/s

注意（带宽）：1b = 1bit 1B=1Byte=8bit

拉取网卡100MB/S ，之前忽略了上传时间：1TB/100(MB/S) = 1000 000MB/100(MB/S) =10000S

10000S/3600S = 3H

- 如果考虑分发上传文件的时间呢？
- 如果考虑每天都有1TB数据的产生呢？
- 如果增量了一年，最后一天计算数据呢？

## Hadoop介绍

Hadoop是一个由Apache基金会所开发的**分布式系统基础架构**。

Hadoop以一种可靠、高效、可伸缩的方式对大量数据存储和分析计算。

用户可以在不了解分布式底层细节的情况下，开发分布式程序。

Hadoop生态圈：

![](doc_img/hadoop-all.jpg)

## 环境组件及信息获取

[Hadoop官网](https://hadoop.apache.org/)、[ApacheHadoop版本树](https://archive.apache.org/dist/hadoop/common/)、[Zookeeper文档](../zookeeper/README.MD)

## 核心组件

1. hadoop通用组件 - Hadoop Common
   包含了其他hadoop模块要用到的库文件和工具

2. 分布式文件系统 - Hadoop Distributed File System (HDFS)
   运行于通用硬件上的分布式文件系统，高吞吐，高可靠

3. 资源管理组件 - Hadoop YARN
   于2012年引入的组件，用于管理集群中的计算资源并在这些资源上调度用户应用。

4. 分布式计算框架 - Hadoop MapReduce

   用于处理超大数据集计算的MapReduce编程模型的实现。

5. [Hadoop Ozone](https://hadoop.apache.org/ozone/): An object store for Hadoop.

6. [Hadoop Submarine](https://hadoop.apache.org/submarine/): A machine learning engine for Hadoop

![](doc_img/hadoop-core.jpg)

## HDFS

### HDFS介绍

#### 产生背景

随着数据量越来越大，在一台电脑上存不下所有的数据，那么就分配到更多的电脑组成的集群上，但是不方便管理和维护，于是就需要一种可以在集群中来管理多台机器上文件的系统，即分布式文件关系系统。HDFS便是分布式文件管理系统中的一员。

HDFS (Hadoop Distributed File System)：分布式文件系统，用于存在文件，通过目录树来定位文件；构建在分布式集群上，集群中的服务器有各自的角色。

适合场景：适合一次写入，多次读取的场景。适合用来做大数据分析。

#### 优点

1. 可构建在廉价的机器上

2. 高容错

   1. 数据自动保存多个副本，通过增加副本的形式，提高容错性。
   2. 当某一个副本数据块丢失后，通过自动恢复保持副本数量。

3. 适合存储大量数据

   HDFS上的一个典型文件大小一般都在G字节至T字节。

   HDFS支持大文件存储。

   单一HDFS实例能支撑数以千万计的文件。

4. 简单的一致性模型

   HDFS应用遵循“一次写入多次读取”的文件访问模型。

   简化了数据一致性问题，并且使高吞吐量的数据访问成为可能。

   Map/Reduce应用或者网络爬虫应用都非常适合这个模型。

#### 缺点

1. 不适合低延迟的数据访问。
2. 无法高效的对大量小文件进行存储。
3. 不支持对同一个文件的并发写入。
4. 不支持文件的随机修改。

### HDFS架构

架构图

![](doc_img/hdfs_all.jpg)

> 100台服务器，存储空间单个8TB,总存储空间为800TB，那么5TB文件如何存储？
>
> 128MB一块 128MB8=1GB 12881024=1TB
>
> 5TB数据分成的128MB的块数8192 *5。

清单：

​	5TB文件分的块：

元数据：

​	文件名称：web.log，大小：5TB ，创建时间，权限，文件所有者，文件所属的用户组，文件类型等。

文件块列表信息：

​	0~128\*1024\*1024 -1：128MB：node1：path,node3:path,node8:path

​	128\*1024\*1024~2\*128\*1024\*1024 -1：128MB：node2：path,node4:path,node9:path

​	2\*128\*1024\*1024~3\*128\*1024\*1024 -1：128MB：node3：path,....

​	......

![](doc_img/hdfs_store.jpg)

### HDFS完全分布式搭建

hadoop运行模式：本地模式、伪分布式模式、**完全分布式模式**、高可用的完全分布式模式。

#### 搭建规划

| node1    | node2             | node3      | node4      |
| -------- | ----------------- | ---------- | ---------- |
| NameNode | SecondaryNameNode |            |            |
|          | DataNode-1        | DataNode-2 | DataNode-3 |

NameNode和SecondaryNameNode都比较消耗内存，所以不要将它们安装在同一台服务器上。

#### 前置环境

***为避免测试学习方便以下部分操作在生产环境慎用。***

1. 对应节点免密登录配置。见《Linux初步》
2. JDK环境。见《Linux初步》
3. 关闭并禁用防火墙

#### 安装步骤

**注意本节环境变量取决于你的机器，除特殊说明每个节点配置一致**

下载[安装包](https://archive.apache.org/dist/hadoop/common/)并上传至涉及节点虚拟机并解压。

![](doc_img/hadoop_dir.png)

- bin目录：存放对Hadoop相关服务（hadoop,hdfs,yarn）进行操作的脚本
- etc目录：Hadoop的配置文件目录，存放Hadoop的配置文件
- lib目录：存放Hadoop的本地库（对数据进行压缩解压缩功能）
- sbin目录：存放启动或停止Hadoop相关服务的脚本
- share目录：存放Hadoop的依赖jar包、文档、和官方案例

##### 环境变量

方便运行和停止

```bash
export HADOOP_HOME=/opt/hadoop-3.3.6
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
```

##### hadoop-env.sh配置

此文件在hadoop的etc目录下

搜索`JAVA_HOME` 填入环境变量`export JAVA_HOME=/usr/java/jdk-11`

##### workers

此文件在hadoop的etc目录下

此文件主要用于配置DataNode地址。

```
#示例
centos7-node2
192.168.1.102
```

注意：该文件中不能出现空行，添加的内容结尾也不能出现空格。

##### core-site.xml

[官方配置文档](https://hadoop.apache.org/docs/r3.3.6/hadoop-project-dist/hadoop-common/core-default.xml)

```xml
<configuration>
    <!-- 指定hdfs的namenode的地址 -->
    <property>
        <name>fs.defaultFS</name>
        <value>hdfs://centos7-node1:9820</value>
    </property>
    <!-- 指定hadoop数据的存储目录 -->
    <property>
        <name>hadoop.tmp.dir</name>
        <value>/var/shaoteemo/hadoop/full</value>
    </property>
</configuration>
```

##### hdfs-site.xml

[官方配置文档](https://hadoop.apache.org/docs/r3.3.6/hadoop-project-dist/hadoop-hdfs/hdfs-default.xml)

```xml
<configuration>
    <!-- 指定NameNode web访问地址 -->
    <property>
        <name>dfs.namenode.http-address</name>
        <value>centos7-node1:9870</value>
    </property>
    <!-- 指定secondary namenode web访问地址 -->
    <property>
        <name>dfs.namenode.secondary.http-address</name>
        <value>centos7-node2:9868</value>
    </property>
    <!-- 指定每个block块的副本数，默认为3 -->
    <property>
        <name>dfs.replication</name>
        <value>2</value>
    </property>
</configuration>
```

##### 验证环境

```bash
hadoop version
```

#### 启动

格式化启动。一般在NameNode节点

```bash
hdfs namenode -format
```

查看格式化后生成的信息：`cat  /var/shaoteemo/hadoop/full/dfs/name/current/VERSION`

```properties
namespaceID=1993928007
blockpoolID=BP-1973664926-192.168.20.101-1724025958281	#集群id
storageType=NAME_NODE	#角色为NameNode
cTime=1724025958281
clusterID=CID-b3281274-2e3a-4936-a6c7-adc4c738b903	#本次格式化后块池的id
layoutVersion=-66
```

启动集群。在NameNode节点启动

```bash
start-dfs.sh
```

此时启动会报错。修改NameNode上的

 `vim /opt/hadoop-3.3.6/sbin/start-dfs.sh` `vim /opt/hadoop-3.3.6/sbin/stop-dfs.sh`

注意视环境配置，此处为方便测试。添加如下代码后即可正常启动。

```properties
HDFS_NAMENODE_USER=root
HDFS_DATANODE_USER=root
HDFS_DATANODE_SECURE_USER=root
HDFS_SECONDARYNAMENODE_USER=root
```

通过jsp可以查看各集群节点情况。

访问Web页面测试：http://192.168.20.101:9870/

![](doc_img/hadoop_web.png)

#### 文件操作

命令行操作

- 创建查看目录

  ```bash
  # -p 创建多层
  hdfs dfs -mkdir -p /user/root
  # 查看内容
  hdfs dfs -ls /user
  ```

- 上传文件

  ```bash
  # hdfs dfs -put <文件> <存放位置>
  hdfs dfs -put /opt/apps/hadoop-3.3.6.tar.gz /user/root
  ```

- 删除文件

  ```bash
  # 删除文件 。-r 删除不为空的目录
  hdfs dfs -rm -r /user/test/profile
  ```
  
- 下载文件。下载至该节点的/root目录下

  ```bash
  hdfs dfs -get /user/test/profile
  ```

#### 停止

1. 停止集群

   ```bash
   stop-dfs.sh
   ```

2. 单独操作某一角色节点。需要在对应节点下。

   ```bash
   # 启动namenode节点
   hdfs --daemon start namenode
   # 启动secondaryNameNode和DataNode
   hdfs --daemon start secondarynamenode
   hdfs --daemon start datanode
   # 上面的指令将hdfs换成hadoop也可以使用。不推荐使用。
   # 关闭某一角色节点。将上面的start参数换成stop即可
   ```

### 角色

#### NameNode

**作用**：文件和目录的元数据：(运行时，元数据放内存)

- 文件的block副本个数
- 修改和访问的时间
- 访问权限
- block大小以及组成文件的block信息列表

**NameNode有两种方式本地进行持久化**：命名空间镜像文件（fsimage）和编辑日志（edits log）。

fsimage文件不记录每个block所在的DataNode信息，这些信息在每次系统启动的时候从DataNode重建。之后DataNode会周期性地通过心跳包向NameNode报告block信息。DataNode向NameNode注册的时候NameNode请求DataNode发送block列表信息。

```
1、文件名称和路径
2、文件的大小
3、文件的所属关系
4、文件的block块大小  128MB 
5、文件的副本个数  3  MR  10个副本
6、文件的修改时间
7、文件的访问时间
8、文件的权限
9、文件的block列表
```

**存储结构**

一个运行的NameNode如下的目录结构，该目录结构在第一次格式化的时候创建。`即前面配置的hadoop.tmp.dir`下

![](doc_img/datanode_data.png)

如果属性dfs.namenode.name.dir指定了多个路径，则每个路径中的内容是一样的，尤其是当其中一个是挂载的NFS的时候，这种机制为管理提供了一些弹性。备份数据

`in_use.lock`文件用于NameNode锁定存储目录。这样就防止其他同时运行的NameNode实例使用相同的存储目录。记录着进程id与host。

​	`edits-xxx`表示edits log日志文件

​	`fsimage-xxx`表示文件系统元数据镜像文件

​	NameNode在checkpoint之前首先要切换新的edits log文件，在切换时更新`seen_txid`的值。上次合并fsimage和editslog之后的第一个操作编号

**VERSION**文件

|      key      |                          Introduce                           |
| :-----------: | :----------------------------------------------------------: |
|  namespaceID  |  该文件系统的唯一标志符，当NameNode第一次格式化的时候生成。  |
|  blockpoolID  | block池的唯一标志符，一个NameNode管理一个命名空间，该命名空间中的所有文件存储的block都在block池中 |
|  storageType  |                 表示当前目录存储NameNode内容                 |
|     cTime     | 标记着当前NameNode创建的时间。对于刚格式化的存储，该值永远是0，但是当文件系统更新的时候，这个值就会更新为一个时间戳 |
|   clusterID   | HDFS集群使用的一个唯一标志符，在HDFS联邦的情况下，就看出它的作用了，因为联邦情况下，集群有多个命名空间，不同的命名空间由不同的NameNode管理 |
| layoutVersion | 是一个负数，定义了HDFS持久化数据结构的版本。这个版本数字跟hadoop发行的版本无关。当layout改变的时候，该数字减1（比如从--63到-64）。当对HDFS进行了升级，就会发生改变 |

oev                  apply the offline edits viewer to an edits file
oiv                  apply the offline fsimage viewer to an fsimage

##### fslmage

```bash
hdfs oiv --help
# 查看此类文件
hdfs oiv -p [fileType] -i <inpuFile> -o <outputfile>
# 示例
hdfs oiv -p XML -i fsimage_0000000000000000055 -o /opt/test55.xml
# vim格式化查看
# 1.Ctrl+V  2.: !xmllint -format - 
```

解析：文件包含文件夹权限相关信息、文件层级信息等。**不包括block块datanode节点信息，因为集群启动后datanode会隔段时间上报节点块信息**。

##### edits log

```bash
hdfs oev --help
# 查看此类文件
hdfs oev -p [fileType] -i <inpuFile> -o <outputfile>
# 示例
hdfs oev -p XML -i edits_xxx -o /opt/test2.xml
# vim格式化查看
# 1.Ctrl+V  2.: !xmllint -format - 
```

解析：文件包含操作相关记录详细信息。

当文件系统客户端进行了写操作（例如创建或移动了文件），这个事务首先在edits log中记录下来。NameNode在内存中有文件系统的元数据，当edits log记录结束后，就更新内存中的元数据。内存中的元数据用于响应客户端的读请求。

edits log在磁盘上表现为一定数量的文件。每个文件称为片段（Segment），前缀“edits”，后缀是其中包含的事务ID（transaction IDs）。每个写操作事务都仅仅打开一个文件（比如：edits_inprogress_00000000000010），写完后冲刷缓冲区并同步到磁盘，然后返回客户端success状态码。如果NameNode的元数据需要写到多个目录中，则对于每个写事务需要所有的写操作都完成，并冲刷缓冲区同步到磁盘才返回success状态码。这样就可以保证在发生宕机的时候没有事务数据丢失。

用户的操作是一个事务，每个操作NameNode都要先将操作记录到edits log中，如果给NameNode指定了多个目录，则在多个目录中都存在edits log文件，用户的操作要在多个目录中都写完成，才让NameNode同步数据到内存中。当NameNode在内存中也同步了数据，就返回客户端success。

每个fsimage文件都是系统元数据的一个完整的持久化检查点（checkpoint）（后缀表示镜像中的最后一个事务）。写操作不更新这个数据，因为镜像文件通常为GB数量级，写到磁盘很慢。如果NameNode宕机，可以将最新fsimage加载到内存，同时执行edits log对应于该fsimage之后的操作，就可以重建元数据的状态。而这正是每次启动NameNode的时候NameNode要做的工作。

#### SecondaryNameNode

**存在的意义**

edits log会随着对文件系统的操作而无限制地增长，这对正在运行的NameNode而言没有任何影响，如果NameNode重启，则需要很长的时间执行edits log的记录以更新fsimage（元数据镜像文件）。在此期间，整个系统不可用。定期进行FsImage和Edits的合并，如果这个操作由NameNode节点完成，又会效率过低。因此，引入一个新的节点SecondaryNamenode，专门用于FsImage和Edits log的合并。

SecondaryNameNode要和NameNode拥有相同的内存。对大的集群，SecondaryNameNode运行于一台专用的物理主机。

**存储结构**

1. SecondaryNameNode中checkpoint目录布局（`dfs.namenode.checkpoint.dir`）和NameNode中的基本一样。
2. 如果NameNode完全坏掉，可以快速地从SecondaryNameNode恢复。有可能丢数据
3. 如果SecondaryNameNode直接接手NameNode的工作，需要在启动NameNode进程的时候添加-importCheckpoint选项。该选项会让NameNode从由`dfs.namenode.checkpoint.dir`属性定义的路径中加载最新的checkpoint数据，但是为了防止元数据的覆盖，要求`dfs.namenode.name.dir`定义的目录中没有内容。

**工作流程**

![](doc_img/2namenode_step.jpg)

edits_inprogress_00000000001_0000000019 seen_txid=20

1. secondarynamenode请求namenode生成新的edits log文件(edits_inprogress_...)并向其中写日志。NameNode会在所有的存储目录中更新seen_txid文件
2. SecondaryNameNode通过HTTP GET的方式从NameNode下载fsimage和edits文件到本地。
3. SecondaryNameNode将fsimage加载到自己的内存，并根据edits log更新内存中的fsimage信息，然后将更新完毕之后的fsimage写到磁盘上。
4. SecondaryNameNode通过HTTP PUT将新的fsimage文件发送到NameNode，NameNode将该文件保存为.ckpt的临时文件备用。
5. NameNode重命名该临时文件并准备使用。此时NameNode拥有一个新的fsimage文件和一个新的很小的edits log文件（可能不是空的，因为在SecondaryNameNode合并期间可能对元数据进行了读写操作）。管理员也可以将NameNode置于safemode，通过hdfs dfsadmin -saveNamespace命令来进行edits log和fsimage的合并。

检查点创建时机:对于创建检查点（checkpoint）的过程，有三个参数进行配置：

```xml
<!-- 默认情况下，SecondaryNameNode每个小时进行一次checkpoint合并 -->
<property>
    <name>dfs.namenode.checkpoint.period</name>
    <value>3600s</value>
</property>
<!-- 一分钟检查一次操作次数，当操作次数达到1百万时，SecondaryNameNode执行一次 -->
<property>
    <name>dfs.namenode.checkpoint.txns</name>
    <value>1000000</value>
    <description>操作动作次数</description>
</property>
<property>
    <name>dfs.namenode.checkpoint.check.period</name>
    <value>60s</value>
    <description> 1分钟检查一次操作次数</description>
</property >
```

合并条件总结：1、每小时一次，2、不足一小时，则只要edits log中记录的事务数达到了1000000，则合并。

`hdfs-default.xml`，如果想修改这几个参数的话，直接在`hdfs-site.xml`中进行配置即可。

#### DataNode

##### **工作机制**

![](doc_img/datanode-work.jpg)

1. 一个数据块在DataNode上以文件形式存储在磁盘上，包括两个文件，一个是数据本身，一个是元数据包括数据块的长度，块数据的校验和，以及时间戳。
2. DataNode启动后向NameNode注册，通过后，周期性（1小时）的向NameNode上报所有的块信息。
3. 心跳是每3秒一次，心跳返回结果带有NameNode给该DataNode的命令如复制块数据到另一台机器，或删除某个数据块。如果超过10分钟没有收到某个DataNode的心跳，则认为该节点不可用。
4. 集群运行中可以安全加入和退出一些机器。

##### 存储机制

DataNode不需要显式地格式化。关键文件和目录结构如下：

![](doc_img/datanode_tree.jpg)

1. HDFS块数据存储于blk_前缀（block缩写）的文件中，包含了被存储文件原始字节数据的一部分。
2. 每个block文件都有一个.meta后缀的元数据文件关联。该文件包含了一个版本和类型信息的头部，后接该block中每个部分的校验和。
3. 每个block属于一个block池，每个block池有自己的存储目录，该目录名称就是该池子的ID（跟NameNode的VERSION文件中记录的block池ID一样）。

当一个目录中的block达到64个（通过`dfs.datanode.numblocks`配置）的时候，DataNode会创建一个新的子目录来存放新的block和它们的元数据。这样即使当系统中有大量的block的时候，目录树也不会太深。同时也保证了在每个目录中文件的数量是可管理的，避免了多数操作系统都会碰到的单个目录中的文件个数限制（几十几百上千个）。

如果`dfs.datanode.data.dir`指定了位于在不同的硬盘驱动器上的多个不同的目录，则会通过轮询的方式向目录中写block数据。需要注意的是block的副本不会在同一个DataNode上复制，而是在不同的DataNode节点之间复制。

##### 存储数据模型(重点)

1. 文件线性切割成块（Block）（按字节切割）

2. Block分散存储在集群节点中

3. 单一文件Block大小一致，文件与文件可以不一致

   ```bash
   #上传时设置块大小和副本数
   hdfs dfs -D  dfs.blocksize=1048576 -D dfs.replication=2 -put test.txt  /
   ```

4. Block可以设置副本数，副本分散在不同节点中

   1. 副本数不要超过DataNode节点数量
   2. 承担计算
   3. 容错

5. 文件上传可以设置Block大小和副本数

6. 已上传的文件Block副本数可以调整，大小不变

7. 只支持一次写入多次读取；对同一个文件，一个时刻只有一个写入者

8. 可以append追加数据

**优势（了解）**

1. 一个文件的大小可以大于网络中任意一个节点的磁盘容量
2. 使用抽象块而非整个文件作为存储单元，大大简化存储子系统的设计
3. 块非常适合用于数据备份进而提供数据容错能力和提高可用性

#### 权限

1. 每个文件和目录都和一个拥有者和组相关联。
2. 文件或者目录对与拥有者、同组用户和其他用户拥有独立的权限。
3. 对于一个文件，r表示读取的权限，w表示写或者追加的权限。对于目录而言，r表示列出目录内容的权限，w表示创建或者删除文件和目录的权限，x表示访问该目录子项目的权限。
4. 默认情况下hadoop运行时安全措施处于停用模式（hadoop2.6.5）,启用模式3.1.3。一个客户端可以在远程系统上通过创建和任意一个合法用户同名的账号来进行访问。
5. 安全措施可以防止用户或自动工具及程序意外修改或删除文件系统的重要部分。（`dfs.permissions.enabled`属性）
6. 超级用户是namenode进程的标识。对于超级用户，系统不会执行任何权限检查。

### Windows环境搭建

[官方提供的编译二进制库（已停更）](https://github.com/steveloughran/winutils)

[三方下载地址](https://github.com/cdarlint/winutils)

注意需要配置环境变量`HADOOP_HOME`，以及对应的`Path=%PATH%;%HADOOP_HOME%\bin`

### HDFS Federation联邦

[官方Federation文档](https://hadoop.apache.org/docs/r3.3.6/hadoop-project-dist/hadoop-hdfs/Federation.html)

HDFS架构的局限性

1. 内存限制

   由于NameNode在内存中存储所有的元数据（metadata），因此单个NameNode所能存储的对象数目受到NameNode所在JVM的heap size的限制。随着数据的飞速增长，存储的需求也随之增长。内存将成为限制系统横向扩展的瓶颈。

2. 隔离问题

   由于HDFS仅有一个NameNode，无法隔离各个程序，因此HDFS上的一个程序就很有可能影响整个HDFS上运行其它的程序。

3. 性能的瓶颈

   由于是单个NameNode的HDFS架构，因此整个HDFS文件系统的吞吐量受限于单个NameNode的吞吐量。

**HDFS** Federation架构设计

![](doc_img/federation.gif)

不同应用可以使用不同NameNode进行数据管理，比如日志分析业务、图片业务、爬虫业务等，不同的业务模块使用不同的NameNode进行管理NameSpace。（隔离性）

![](doc_img/Federation2.jpg)

**Federation优点**

1. 通过多个namenode/namespace把元数据的存储和管理分散到多个节点中，使得namenode/namespace可以通过增加机器来进行水平扩展。
2. 能把单个namenode的负载分散到多个节点中，在HDFS数据规模较大的时候不会也降低HDFS的性能。
3. 可以通过多个namespace来隔离不同类型的应用，把不同类型应用的HDFS元数据的存储和管理分派到不同的namenode中。

### NameNode HA

所谓HA（High Availability），即高可用（7*24小时服务不中断）。通过主备NameNode解决，如果主NameNode发生故障，则切换到备NameNode上，从而解决NameNode单点故障的问题。

1. 实现高可用最关键的目的是消除单点故障。HA严格来说应该分成各个组件的HA机制：HDFS的HA和YARN的HA。

2. Hadoop1.x在HDFS集群中NameNode存在单点故障;Hadoop2.0+可以通过NameNode HA解决单点故障的问题。

   Hadoop2.x中支持两个NameNode做HA，一个主，一个备。

   Hadoop3.x中支持两个或两个以上的NameNode做HA，一主一备，或一主多备。

3. NameNode主要在以下两个方面影响HDFS集群

   1. NameNode机器发生意外，如宕机，集群将无法使用，直到管理员修复重启后。
   2. NameNode机器需要升级，包括软件、硬件升级，此时集群也将无法使用

4. HDFS HA功能通过配置Active/Standby两个NameNodes实现在集群中对NameNode的热备来解决上述问题。如果出现故障，如机器崩溃或机器需要升级维护，这时可通过此种方式将NameNode很快的切换到另外一台机器。

#### 手动NameNode HA

![](doc_img/dfs_mu_client.jpg)

fsimage+edits log需要由StandbyNameNode做合并工作,fsimage推送的时机可以通过参数来调整：

```properties
dfs.namenode.checkpoint.period  1小时
dfs.namenode.checkpoint.txns   100 0000事务
dfs.namenode.checkpoint.check.period  3s
```

1. 一个NameNode进程处于Active状态，另1个NameNode进程处于Standby状态。Active的NameNode负责处理客户端的请求。
2. Active的NN修改了元数据之后，会在JNs的半数以上的节点上记录这个日志。Standby状态的NameNode会监视任何对JNs上edit log的更改。一旦edits log出现更改，Standby的NN就会根据edits log更改自己记录的元数据。
3. 当发生故障转移时，Standby主机会确保已经读取了JNs上所有的更改来同步它本身记录的元数据，然后手动完成由Standby状态切换为Active状态。
4. 为了确保在发生故障转移操作时拥有相同的数据块位置信息，DNs向所有NN发送数据块位置信息和心跳数据。
5. JNS只允许一台NameNode向JNs写edits log数据，这样就能保证不会发生“脑裂”。
6. 手动切换：通过命令实现主备之间的切换，可以用HDFS升级等场合

#### 自动NameNode HA

![](doc_img/dfs_au_client.jpg)

**主备NameNode自动切换解决单点故障**

1. 主NameNode对外提供服务，备NameNode同步主NameNode元数据，以待切换
2. 所有DataNode同时向两个NameNode汇报数据块信息（位置）
3. JNN(JournalNode)集群（属性）同步edits log
4. standby备，完成了fsimage+edits.log文件的合并产生新的fsimage，推送回ANN
5. 自动切换：基于Zookeeper自动切换方案
6. ZooKeeper Failover Controller：监控NameNode健康状态，并向Zookeeper注册NameNode。NameNode挂掉后，ZKFC为NameNode竞争锁，获得ZKFC 锁的NameNode变为active

#### 自动HA 集群搭建

规划节点

|    node1     |    node2     |    node3     |   node4    |
| :----------: | :----------: | :----------: | :--------: |
|  NameNode1   |  NameNode2   |              |            |
|              |  DataNode1   |  DataNode2   | DataNode3  |
|              |  Zookeeper1  |  Zookeeper2  | Zookeeper3 |
|    ZKFC1     |    ZKFC2     |              |            |
| JournalNode1 | JournalNode2 | JournalNode3 |            |

ZKFC始终跟随NN。

搭建步骤停止已有hadoop服务：

1. 删除已有的数据。

   ```bash
   rm -rf /var/shaoteemo/hadoop/full
   rm -rf /opt/hadoop3.1.3/logs
   ```

2. 配置各节点jdk、修改workers指定datanode位置(hadoop-env.sh)。在前面的章节《完全分布式搭建》有方式。

3. core-site.xml

   ```xml
   <configuration>
       <property>
           <name>fs.defaultFS</name>
           <value>hdfs://mycluster</value>
       </property>
       <!-- 数据的存放目录 -->
       <property>
           <name>hadoop.tmp.dir</name>
           <value>/var/shaoteemo/hadoop/ha</value>
       </property>
       <!-- 指定每个zookeeper服务器的位置和客户端端口号 -->
       <property>
           <name>ha.zookeeper.quorum</name>
           <value>centos7-node2:2181,centos7-node3:2181,centos7-node4:2181</value>
       </property>
       <!-- 解决HDFS web页面上删除、创建文件权限不足的问题 -->
       <property>
           <name>hadoop.http.staticuser.user</name>
           <value>root</value>
       </property>
   </configuration>
   ```

4. hdfs-site.xml

   ```xml
   <configuration>
       <!-- JournalNode数据存储目录 -->
       <property>
           <name>dfs.journalnode.edits.dir</name>
           <value>${hadoop.tmp.dir}/dfs/journalnode/</value>
       </property>
       <!--集群名称 -->
       <property>
           <name>dfs.nameservices</name>
           <value>mycluster</value>
       </property>
       <!-- 集群中NameNode节点都有哪些.mycluster与集群名称对应-->
       <property>
           <name>dfs.ha.namenodes.mycluster</name>
           <value>nn1,nn2</value>
       </property>
       <!-- NameNode的节点的RPC通信地址 -->
       <property>
           <name>dfs.namenode.rpc-address.mycluster.nn1</name>
           <value>centos7-node1:9820</value>
       </property>
       <property>
           <name>dfs.namenode.rpc-address.mycluster.nn2</name>
           <value>centos7-node2:9820</value>
       </property>
       <!-- NameNode的节点的http通信地址 -->
       <property>
           <name>dfs.namenode.http-address.mycluster.nn1</name>
           <value>centos7-node1:9870</value>
       </property>
       <property>
           <name>dfs.namenode.http-address.mycluster.nn2</name>
           <value>centos7-node2:9870</value>
       </property>
       <!-- 指定NameNode元数据在JournalNode上的存放位置 -->
       <property>
           <name>dfs.namenode.shared.edits.dir</name>
           <value>qjournal://centos7-node1:8485;centos7-node2:8485;centos7-node3:8485/mycluster</value>
       </property>
       <!-- 访问代理类：client用于确定哪个NameNode为Active -->
       <property>
           <name>dfs.client.failover.proxy.provider.mycluster</name>
           <value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>
       </property>
       <!-- 配置隔离机制，即同一时刻只能有一台服务器对外响应 -->
       <property>
           <name>dfs.ha.fencing.methods</name>
           <value>sshfence</value>
       </property>
       <!-- 使用隔离机制时需要ssh秘钥登录-->
       <property>
           <name>dfs.ha.fencing.ssh.private-key-files</name>
           <value>/root/.ssh/id_dsa</value>
       </property>
       <!-- 启用nn故障自动转移 -->
       <property>
           <name>dfs.ha.automatic-failover.enabled</name>
           <value>true</value>
       </property>
   </configuration>
   ```

5. 首次启动

   1. 对应节点Zookeeper。node2-4.

      ```bash
      zkServer.sh start
      ```

   2. 启动对应节点JournalNode。1~3.

      ```bash
      hdfs --daemon start journalnode
      ```

   3. 格式化NameNode1

      ```bash
      hdfs namenode -format
      ```

   4. 启动namenode进程

      ```bash
      hdfs --daemon start namenode
      ```

   5. NameNode2（node2）同步NameNode1数据。并启动namenode进程。

      ```bash
      hdfs namenode -bootstrapStandby
      ```

   6. 在node1上执行。初始化ZK。

      ```bash
      hdfs zkfc -formatZK
      ```

   7. 检查zookeeper注册配置信息。在有zookeeper的节点上连接即可。

      ```bash
      zkCli.sh
      # 出现对应zk数据即表示步骤6成功：hadoop-xx.可通过Get获取值信息。
      ```

   8. 在node1启动DataNode节点进程

      新增`sbin/start-dfs.sh`、`sbin/stop-dfs.sh`启动脚本内容

      ```properties
      HDFS_JOURNALNODE_USER=root
      HDFS_ZKFC_USER=root
      ```

      启动DataNode

      ```bash
      start-dfs.sh
      ```

      检查步骤七查看对应zk节点信息。

      访问http://192.168.20.101:9870/，http://192.168.20.102:9870/两个NameNode节点查看状态。

   安装rpm包以使NameNode转换正常：`yum install -y psmisc`

6. HA启动与关闭脚本

   功能：zk、hdfs启停

   启动

   ```shell
   #!/bin/bash
   nodes=(centos7-node2 centos7-node3 centos7-node4)
   for node in ${nodes[@]}
   do
       ssh $node "source /etc/profile;zkServer.sh start"
   done
   # sleep
   sleep 1
   # start HDFS
   start-dfs.sh
   # watch info
   echo "-------current info----------"
   jps
   for node in ${nodes[@]}
   do
       echo "-------$node----------"
       ssh $node "source /etc/profile;jps"
   done
   ```

   关闭

   ```bash
   #!/bin/bash
   nodes=(centos7-node2 centos7-node3 centos7-node4)
   stop-dfs.sh
   sleep 1
   for node in ${nodes[@]}
   do
       ssh $node "source /etc/profile;zkServer.sh stop"
   done
   # ---可以加入类似的ps查看
   ```

## Yarn

[官方文档](https://hadoop.apache.org/docs/r3.3.6/hadoop-yarn/hadoop-yarn-site/YARN.html)

### yarn介绍

YARN(Yet Another Resource Negotiator): Hadoop 2.0新引入的资源管理系统，直接从MRv1演化而来的；核心思想：将MRv1中JobTracker的资源管理和任务调度两个功能分开，分别由ResourceManager和ApplicationMaster进程实现。YARN的引入，使得多个计算可运行在一个集群中, 每个job对应一个ApplicationMaster。 目前多个计算框架可以运行在YARN上，比如MapReduce、Spark等解耦资源与计算。

### 架构解析

Yarn是一个资源调度平台，负责为运行的程序（提交job作业）提供服务器的资源（CPU、Mem..，网络等）。

![](doc_img/yarn/y_introduce.png)

#### 各角色任务功能

1. ResourceManager（简称RM）：
   - 处理客户端的请求
   - 监控NodeManager
   - 启动和监控ApplicationMaster
   - 负责整个集群的资源管理和调度
2. NodeManager:
   - 与RM汇报资源
   - 管理当前服务器上的Container容器
   - 处理来自RM的指令
   - 处理来之ApplicationMaster指令
3. ApplicationMaster（简称AM）：负责应用程序相关的事务，比如任务调度、任务监控和容错等
   - 以作业为单位，负载到不同的节点，避免单点故障。
   - 向RM申请创建Task任务（MapTask、ReduceTask）所需要资源（Container）
   - 负责任务切分、任务调度、任务监控和容错等
4. Container：【节点NM，CPU,MEM,I/O大小，启动命令】
   - 默认NodeManager启动线程监控Container大小
   - 超出申请资源额度，被kill掉
5. Client：
   - • RM-Client：请求资源创建AM
   - • AM-Client：与AM交互

**MRAppMaster容错**

- 失败后，由YARN重新启动
- 任务失败后，MRAppMaster重新申请资源

### 工作机制

也是一个job的从提交到执行完毕的全流程，除了job本身执行以外，还有Client、RM、NodeManager、MRApplicationMaster.

![](doc_img/yarn/yn_1.png)

**作业提交：**

1. 提交作业job后，`job.waitForCompletion(true)`调用`monitorAndPrintJob()`方法每秒轮询作业进度，如果发现自上次报告后有改变，便把进度报告给控制台。Job的`submit()`方法创建一个内部的JobSubmitter实例，并调用其`submitJobInternal`方法（步骤1）。作业完成后，如果成功，就显示计数器；如果失败，这将导致作业失败的错误记录到控制台。

**JobSubmitter所实现的作业提交过程如下所述：**

1. 向资ResourceManager源管理器请求一个新作业的ID，用于MapReduce作业ID。
2. 作业客户端检查作业的输出说明，计算输入分片splits并将作业资源（包括作业Jar包、配置文件和分片信息）复制到HDFS
3. 通过调用资源管理器上的`submitApplication()`方法提交作业

**作业初始化**

1. 资源管理器ResourceManager收到调用他的`submitApplication()`消息后，便将请求传递给调度器（scheduler）。调度器分配一个容器（Container），然后资源管理器在节点管理器（NodeManager）的管理下载容器中启动应用程序的master进程（步骤5a和5b）
2. MapReduce作业的application master是一个Java应用程序，它的主类是MRAppMaster。它对作业进行初始化：通过创建多个簿记对象以保持对作业进度的跟踪，因为它将接受来自任务的进度和完成报告（步骤6）。
3. 接下来，它接受来自共享文件系统的在客户端计算的输入分片（步骤7）。对每一个分片创建一个map任务对象以及由`mapreduce. job.reduces`属性确定的具体数量的reduce任务对象。

**任务分配**

1. AppMaster为该作业中的所有map任务和reduce任务向资源管理器请求容器。

**任务执行**

1. 一旦资源管理器的调度器为任务分配了容器，AppMaster就通过与节点管理器NodeManager通讯来启动容器（步骤9a和9b）。
2. 该任务由主类为YarnChild的Java应用程序执行。在它允许任务之前，首先将任务需要的资源本地化，包括作业的配置、JAR文件和所有来自分布式缓存的文件.
3. 最后运行map任务或reduce任务。

**进度和状态更新**

在YARN下运行时，任务每3秒钟通过umbilical接口向APPMaster汇报进度和状态。客户端每一秒钟（通过`mapreduce.client.Progressmonitor.pollinterval`设置）查询一次AppMaster以接收进度更新，通常都会向用户显示。

**作业完成**

除了向AppMaster查询进度外，客户端每5秒还通过调用Job的`waitForCompletion()`来检测作业是否完成。查询的间隔可以通过`mapreduce.client.completion.pollinterval`属性进行设置。作业完成后，AppMaster和任务容器清理器工作状态。

### 资源调度器

目前，Hadoop作业调度器没有好坏之分，只有适合与否之分，所以Hadoop提供三种调度器让使用者进行选择，这三种：FIFO、Capacity Scheduler和Fair Scheduler。Hadoop3.1.3默认的资源调度器是Capacity Scheduler。具体设置详见：`yarn-default.xml`文件

```xml
<!-- 修改自己搭建集群的调度器，该配置在yarn-site.xml文件 -->
<property>
    <description>The class to use as the resource scheduler.</description>
    <name>yarn.resourcemanager.scheduler.class</name>   <value>org.apache.hadoop.yarn.server.resourcemanager.scheduler.capacity.CapacityScheduler</value>
</property>
```

#### FIFO

![](doc_img/yarn/yarn_FIFO.jpg)

Hadoop最初设计目的是支持大数据批处理作业，如日志挖掘、Web索引等作业，为此，Hadoop仅提供了一个非常简单的调度机制：FIFO，即先来先服务，在该调度机制下，所有作业被统一提交到一个队列中，Hadoop按照提交顺序依次运行这些作业。

但随着Hadoop的普及，单个Hadoop集群的任务处理量越来越多，不同用户提交的应用程序往往具有不同的服务质量要求，典型的应用有以下几种：

- 批处理作业：这种作业往往耗时较长，对时间完成一般没有严格要求，如数据挖掘、机器学习等方面的应用程序。
- 交互式作业：这种作业期望能及时返回结果，如HQL查询（Hive）等。
- 实时统计作业：这种作业要求有一定量的资源保证，如淘宝交易量大屏等。

此外，这些应用程序对硬件资源需求量也是不同的，因此，简单的FIFO调度策略不仅不能满足多样化需求，也不能充分利用硬件资源。

#### Capacity Scheduler

![](doc_img/yarn/yarn_cs.jpg)

Capacity Scheduler 是Yahoo开发的多用户调度器。

- 它以队列为单位划分资源，支持多个队列，每个队列可设定一定比例的资源最低保证和使用上限。
- 每个队列采用是FIFO调度策略。
- 为了防止同一个用户的作业独占队列中的资源，调度器会对同一个用户提交的作业所占的资源量进行限定。
- 当有任务被提交时，计算每个队列中正在运行的任务数/该对应应分得的计算资源 的比值，任务分给比值最小（最闲）的队列。
- 每个队列最前端的作业可以并行运行。

总之，Capacity Scheduler 主要有以下几个特点：

- 容量保证。管理员可为每个队列设置资源最低保证和资源使用上限，而所有提交到该队列的应用程序共享这些资源。
- 灵活性，如果一个队列中的资源有剩余，可以暂时共享给那些需要资源的队列，而一旦该队列有新的应用程序提交，则其他队列借调的资源会归还给该队列。这种资源灵活分配的方式可明显提高资源利用率。
- 多重租赁。支持多用户共享集群和多应用程序同时运行。为防止单个应用程序、用户或者队列独占集群中的资源，管理员可为之增加多重约束（比如单个应用程序同时运行的任务数等）。
- 安全保证。每个队列有严格的ACL列表规定它的访问用户，每个用户可指定哪些用户允许查看自己应用程序的运行状态或者控制应用程序（比如杀死应用程序）。此外，管理员可指定队列管理员和集群系统管理员。
- 动态更新配置文件。管理员可根据需要动态修改各种配置参数，以实现在线集群管理。

#### Fair Scheduler

![](doc_img/yarn/yarn_fr1.jpg)

![](doc_img/yarn/yarn_fr2.jpg)

Fair Scheduler 是Facebook开发的多用户调度器。

公平调度器的目的是让所有的作业随着时间的推移，都能平均地获取等同的共享资源。当有作业提交上来，系统会将空闲的资源分配给新的作业，每个任务大致上会获取平等数量的资源。和传统的调度策略不同的是它会让小的任务在合理的时间完成，同时不会让需要长时间运行的耗费大量资源的任务挨饿！

同Capacity Scheduler类似，它以队列为单位划分资源，每个队列可设定一定比例的资源最低保证和使用上限，同时，每个用户也可设定一定的资源使用上限以防止资源滥用；当一个队列的资源有剩余时，可暂时将剩余资源共享给其他队列。

当然，Fair Scheduler也存在很多与Capacity Scheduler不同之处，这主要体现在以下几个方面：

① 资源公平共享。在每个队列中，Fair Scheduler 可选择按照FIFO、Fair或DRF策略为应用程序分配资源。其中:

**FIFO策略**

公平调度器每个队列资源分配策略如果选择FIFO的话，就是禁用掉每个队列中的Task共享队列资源，此时公平调度器相当于上面的容量调度器。

**Fair策略**

Fair 策略(默认)是一种基于最大最小公平算法实现的资源多路复用方式，默认情况下，每个队列内部采用该方式分配资源。这意味着，如果一个队列中有两个应用程序同时运行，则每个应用程序可得到1/2的资源；如果三个应用程序同时运行，则每个应用程序可得到1/3的资源。

【扩展：】最大最小公平算法举例：

```
1.不加权(关注点是job的个数)：
有一条队列总资源12个, 有4个job，对资源的需求分别是: 
job1->1,  job2->2 , job3->6,  job4->5
   第一次算:  12 / 4 = 3 
        job1: 分3 --> 多2个 
        job2: 分3 --> 多1个
        job3: 分3 --> 差3个
        job4: 分3 --> 差2个
    第二次算: 3 / 2  = 1.5 
     job1: 分1
        job2: 分2
        job3: 分3 --> 差3个 --> 分1.5 --> 最终: 4.5 
        job4: 分3 --> 差2个 --> 分1.5 --> 最终: 4.5 
    第n次算: 一直算到没有空闲资源
2.加权(关注点是job的权重)：
有一条队列总资源16，有4个job 
对资源的需求分别是: job1->4  job2->2  job3->10  job4->4 
每个job的权重为:  job1->5  job2->8  job3->1  job4->2   
    第一次算: 16 / (5+8+1+2) =  1
       job1:  分5 --> 多1
       job2:  分8 --> 多6
       job3:  分1 --> 少9
       job4:  分2 --> 少2      
    第二次算: 7 / (1+2) = 7/3
       job1: 分4
       job2: 分2
       job3: 分1 --> 分7/3 --> 少
       job4: 分2 --> 分14/3(4.66) -->多2.66
   第三次算: 
       job1: 分4
       job2: 分2
       job3: 分1 --> 分7/3 --> 分2.66
       job4: 分4
   第n次算: 一直算到没有空闲资源
```

**DRF策略**

DRF(Dominant Resource Fairness)，我们之前说的资源，都是单一标准，例如只考虑内存(也是yarn默认的情况)。但是很多时候我们资源有很多种，例如内存，CPU，网络带宽等，这样我们很难衡量两个应用应该分配的资源比例。

那么在YARN中，我们用DRF来决定如何调度：假设集群一共有100 CPU和10T 内存，而应用A需要(2 CPU, 300GB)，应用B需要(6 CPU, 100GB)。则两个应用分别需要A(2%CPU, 3%内存)和B(6%CPU, 1%内存)的资源，这就意味着A是内存主导的, B是CPU主导的，针对这种情况，我们可以选择DRF策略对不同应用进行不同资源（CPU和内存）的一个不同比例的限制。

②支持资源抢占。当某个队列中有剩余资源时，调度器会将这些资源共享给其他队列，而当该队列中有新的应用程序提交时，调度器要为它回收资源。为了尽可能降低不必要的计算浪费，调度器采用了先等待再强制回收的策略，即如果等待一段时间后尚有未归还的资源，则会进行资源抢占：从那些超额使用资源的队列中杀死一部分任务，进而释放资源。

`yarn.scheduler.fair.preemption=true` 通过该配置开启资源抢占。

③提高小应用程序响应时间。由于采用了最大最小公平算法，小作业可以快速获取资源并运行完成。

### Yarn HA

[官方文档](https://hadoop.apache.org/docs/r3.3.6/hadoop-yarn/hadoop-yarn-site/ResourceManagerHA.html)

RM高可用机制：

![](https://hadoop.apache.org/docs/r3.3.6/hadoop-yarn/hadoop-yarn-site/images/rm-ha-overview.png)

RM高可用集群规划：

表中的NN、DN、ZK、ZKFC、JNN节点在本文档的HDFS->NameNode HA中介绍。

| node1  | node2  | node3 | node4 |
| :----: | :----: | :---: | :---: |
|  NN-1  |  NN-2  |       |       |
|        |  DN-1  | DN-2  | DN-3  |
|        |  ZK-1  | ZK-2  | ZK-3  |
| ZKFC-1 | ZKFC-2 |       |       |
| JNN-1  | JNN-2  | JNN-3 |       |
|        |        | RM-1  | RM-2  |
|        |  NM-1  | NM-2  | NM-3  |

#### 搭建细则

1. 相关配置文件变更：`mapred-default.xml`、`yarn-default.xml`

   mapred-default.xml

   可选参数：local/classic/yarn

   指定mr作业运行的框架：要么本地运行（local），要么使用MRv1(classic，几乎不用)，要么使用yarn

   ```xml
   <property>
       <name>mapreduce.framework.name</name>
       <value>yarn</value>
   </property>
   ```

   yarn-site.xml

   ```xml
   <configuration>
       <!-- 让yarn的容器支持mapreduce的洗牌，开启shuffle服务 -->
       <property>
           <name>yarn.nodemanager.aux-services</name>
           <value>mapreduce_shuffle</value>
       </property>
       <!-- 启用resourcemanager ha -->
       <property>
           <name>yarn.resourcemanager.ha.enabled</name>
           <value>true</value>
       </property>
       <!-- 指定zookeeper集群的各个节点地址和端口号 --> 
       <property>
           <name>yarn.resourcemanager.zk-address</name>
           <value>centos7-node2:2181,centos7-node3:2181,centos7-node4:2181</value>
       </property>
       <!-- 启用自动恢复 --> 
       <property>
           <name>yarn.resourcemanager.recovery.enabled</name>
           <value>true</value>
       </property>
       <!-- 指定RM的状态信息存储在zookeeper集群 --> 
       <property>
           <name>yarn.resourcemanager.store.class</name>   <value>org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore</value>
       </property>
       <!-- 声明两台resourcemanager的地址 -->
       <property>
           <name>yarn.resourcemanager.cluster-id</name>
           <value>cluster-yarn1</value>
       </property>
       <!--指定resourcemanager的逻辑列表-->
       <property>
           <name>yarn.resourcemanager.ha.rm-ids</name>
           <value>rm1,rm2</value>
       </property>
       <!--  rm1的配置  -->
       <!-- 指定rm1的主机名 -->
       <property>
           <name>yarn.resourcemanager.hostname.rm1</name>
           <value>centos7-node3</value>
       </property>
       <!-- 指定rm1的web端地址 -->
       <property>
           <name>yarn.resourcemanager.webapp.address.rm1</name>
           <value>centos7-node3:8088</value>
       </property>
       <!-- 指定rm1的内部通信地址 -->
       <property>
           <name>yarn.resourcemanager.address.rm1</name>
           <value>centos7-node3:8032</value>
       </property>
       <!-- 指定AM向rm1申请资源的地址 -->
       <property>
           <name>yarn.resourcemanager.scheduler.address.rm1</name> 
           <value>centos7-node3:8030</value>
       </property>
       <!-- 指定供NM连接的地址 --> 
       <property>
           <name>yarn.resourcemanager.resource-tracker.address.rm1</name>
           <value>centos7-node3:8031</value>
       </property>
       <!-- rm2的配置 -->
       <!-- 指定rm2的主机名 -->
       <property>
           <name>yarn.resourcemanager.hostname.rm2</name>
           <value>centos7-node4</value>
       </property>
       <property>
           <name>yarn.resourcemanager.webapp.address.rm2</name>
           <value>centos7-node4:8088</value>
       </property>
       <property>
           <name>yarn.resourcemanager.address.rm2</name>
           <value>centos7-node4:8032</value>
       </property>
       <property>
           <name>yarn.resourcemanager.scheduler.address.rm2</name>
           <value>centos7-node4:8030</value>
       </property>
       <property>
           <name>yarn.resourcemanager.resource-tracker.address.rm2</name>
           <value>centos7-node4:8031</value>
       </property>
       <!-- 环境变量的继承 -->
       <property>
           <name>yarn.nodemanager.env-whitelist</name> <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_MAPRED_HOME,HADOOP_YARN_HOME</value>
       </property>
       <!-- yarn容器允许分配的最大最小内存 -->
       <property>
           <name>yarn.scheduler.minimum-allocation-mb</name>
           <value>512</value>
       </property>
       <property>
           <name>yarn.scheduler.maximum-allocation-mb</name>
           <value>1024</value>
       </property>
       <!-- yarn容器允许管理的物理内存大小 -->
       <property>
           <name>yarn.nodemanager.resource.memory-mb</name>
           <value>1024</value>
       </property>
       <!-- 关闭yarn对物理内存的限制检查 -->
       <property>
           <name>yarn.nodemanager.pmem-check-enabled</name>
           <value>false</value>
       </property>
       <!-- 关闭yarn对虚拟内存的限制检查 -->
       <property>
           <name>yarn.nodemanager.vmem-check-enabled</name>
           <value>false</value>
       </property>
   </configuration>
   ```

2. 启动

   需要先启动hdf集群。 

   添加如下参数至`sbin/start-yarn.sh`、`sbin/stop-yarn.sh`

   ```properties
   YARN_RESOURCEMANAGER_USER=root
   YARN_NODEMANAGER_USER=root
   ```

   在node3执行`start-yarn.sh`、`stop-yarn.sh`启动/停止RM。

3. 测试

   注意：hadoop2.x版本中还需要在Node4启动另外一个RM  `yarn-daemon.sh  start  resourcemanager`。可以通过jps查看：发现node3和node4上分别多一个RM进程，node2、node3、node4上分别多出一个NodeManager进程。
   查看服务 `yarn rmadmin -getServiceState rm1`

   通过访问配置的RMweb节点访问页面。会自动切换Active节点：http://192.168.20.104:8088

   也可访问zk查看新增的Znode信息。

   高可用测试：

   ```bash
   #hadoop2.x版本中的关闭或启动resourcemanager进程的命令
   yarn-deamon.sh stop  resourcemanager
   yarn-deamon.sh start  resourcemanager
   #hadoop3.x版本中关闭或启动resourcemanager进程的命令
   yarn --daemon stop resourcemanager
   yarn --daemon start resourcemanager
   ```

#### 启停脚本

此脚本延续HDFS HA脚本，是一个综合启停脚本。

启动

```shell
#!/bin/bash
nodes=(centos7-node2 centos7-node3 centos7-node4)
for node in ${nodes[@]}
do
    ssh $node "source /etc/profile;zkServer.sh start"
done
# sleep
sleep 1
# start HDFS
start-dfs.sh
#start yarn
ssh centos7-node3 "source /etc/profile;start-yarn.sh"
# watch info
echo "-------current info----------"
jps
for node in ${nodes[@]}
do
    echo "-------$node----------"
    ssh $node "source /etc/profile;jps"
done
```

停止

```shell
#!/bin/bash
nodes=(centos7-node2 centos7-node3 centos7-node4)
#stop yarn
ssh centos7-node3 "source /etc/profile;stop-yarn.sh"
#stop hdfs
stop-dfs.sh
sleep 1
# stop zk
for node in ${nodes[@]}
do
    ssh $node "source /etc/profile;zkServer.sh stop"
done
# ---可以加入类似的ps查看
```

### 测试案例

在mapreduce中有一些测试程序案例，此处以单词统计（Wordcount）为例。

案例程序在：`~/hadoop-3.3.6/share/hadoop/mapreduce/hadoop-mapreduce-examples-3.3.6.jar`

准备任意单词txt文本

创建hdfs目录并上传txt文本

```bash
hdfs dfs -mkdir -p /wordcount/
hdfs dfs -put ~/word.txt /wordcount
```

执行如下命令以启动统计程序

`hadoop(yarn) jar <程序包> <执行操作> <输入文本路径> <输出结果路径>`

```bash
hadoop jar hadoop-mapreduce-examples-3.1.3.jar wordcount /wordcount/data /wordcount/result
```

注意结果目录不要放在输入路径内。

注意MP程序默认要求有1536MB内存如果环境不匹配，执行上述操作会失败，具体可以查看日志，内存充足请忽略。

修改`mapred-site.xml`配置变更接受内存大小以解决此类问题。

```xml
<property>
    <name>yarn.app.mapreduce.am.resource.mb</name>
    <value>256</value>
</property>
<!-- 默认对mapred的内存请求都是1G，也降低至合适的值。-->
<property>
    <name>mapreduce.map.memory.mb</name>
    <value>256</value>
</property>
<property>
    <name>mapreduce.reduce.memory.mb</name>
    <value>256</value>
</property>
```

执行完成后在输出目录`/wordcount/result`会有_SUCCESS、part-r-00000文件前者为成功标识、后者为结果数据文件。打开结果文件查看统计结果：

```bash
hdfs dfs -cat /wordcount/result/part-r-00000
```

## MapReduce

### MapReduce介绍

Google发表了两篇论文《Google File System》 《Google MapReduce》

1. 《Google File System》简称GFS，是Google公司用于解决海量数据存储的文件系统。
2. 《Google MapReduce》简称MapReduce,是Google的计算框架，基于GFS。

***MapReduce是一个分布式运算程序的框架***重要组成部分，是用户开发“基于Hadoop HDFS的数据分析应用”的核心框架。

MapReduce核心功能是将用户编写的业务逻辑代码和自带默认组件整合成一个完整的分布式运算程序，并行运行在Hadoop集群上。

### 优缺点

#### 优点

1. MapReduce易于编程

   它简单的实现一些接口（比如Mapper、Reducer等），就可以完成一个分布式程序的开发，分布式程序可以运行在大量廉价的PC机器上。也就是说你写一个分布式程序，跟写一个简单的串行程序是一模一样的。因此使得MapReduce编程变得非常流行。及时对分布式不太了解，也可以开发分布式分析程序。

2. 良好的扩展性

   当你的计算资源不够用的时候，你可以通过简单的增加机器来扩展它的计算能力。

3. 高容错性

   MapReduce设计的初衷就是使程序能够部署在廉价的PC机器上，这就要求它具有很高的容错性。比如其中一台机器挂了，它可以把上面的计算任务转移到另外一个节点上运行，不至于这个任务运行失败，而且这个过程不需要人工参与，而完全是由Hadoop内部完成的。

4. 适合TB+级别海量数据的离线处理

   可以实现数以千计的服务器集群并发工作，提供数据处理能力。

#### 缺点

1. 不擅长实时计算

   MapReduce无法像MySQL、Spark、Flink一样，在毫秒或者秒级内返回结果。

2. 不擅长流式计算

   流式计算的输入数据是动态的，而MapReduce的输入数据集是静态的，不能动态变化。这是因为MapReduce自身的设计特点决定了数据源必须是静态的。

3. 不擅长DAG（有向无环图）计算

   多个应用程序存在依赖关系，后一个应用程序的输入为前一个的输出。在这种情况下，MapReduce并不是不能做，而是使用后，每个MapReduce作业的输出结果都会写入到磁盘，会造成大量的磁盘IO，导致性能非常的低下。

### 工作流程

系统执行排序、将map输出作为输入传给reducer的过程称为Shuffle。（看完是不是一脸懵逼）通俗来讲，就是从map产生输出开始到reduce消化输入的整个过程称为Shuffle。如下图用黑线框出的部分：

![](doc_img/mapreduce/mp_in.jpg)

圆形缓冲区介绍：

![](doc_img/mapreduce/mp_cache.jpg)

每一个map任务都会有一个圆形缓冲区。默认大小100MB（`io.sort.mb`属性）阈值0.8也就是80MB(`mapreduce.map.sort.spill.percent`属性指定） ，一旦达到阈值，一个后台线程开始把内容写到(spill)磁盘的指定目录`mapred.local.dir`下的新建的一个溢出写文件。写入磁盘前先partition、sort、[combiner]。一个map task任务可能产生N个磁盘文件。map task运算完之后，产生了N个文件，然后将这些文件merge合成一个文件。 如果N=2，合成的新文件写入磁盘前只经过patition（分区）和sort（排序）过程，不会执行combiner合并（无论是否指定combiner类），如下图所示：

![](doc_img/mapreduce/mp_cache2.png)

如果N>=3，合成的新文件写入磁盘前经过patition（分区）、sort（排序）过和combiner合并（前提是指定了combiner类），如下图所示：

![](doc_img/mapreduce/mp_cache3.png)

思考：为什么只有当N>=3时，合成文件才会执行combiner呢？

这是因为如果N<3时，执行combiner虽然减少了文件的大小，但是同时产生了一定的系统开销。由于减少的文件大小不大，权衡利弊后，确定N<2时不在执行combiner操作。当该map task全部执行完之后，对应的reduce task将会拷贝对应分区的数据（该过程称为fetch），如下图所示：

![](doc_img/mapreduce/mp_cache4.png)

其它的map task任务完成后，对应的reduce task也同样执行fetch操作，如下图所示：

![](doc_img/mapreduce/mp_cache5.png)

每个map任务的完成时间可能不同，因此只要有一个任务完成，reduce任务就开始复制其输出。该阶段被称为reduce的复制阶段。reduce任务有少量复制线程，因此能够并行取得map输出。默认值是5个线程，但这个默认值可以通过设置`mapred.reduce.parallel.copies`属性改变。

![](doc_img/mapreduce/mp_cache6.png)

复制完所有map输出后，reduce任务进入合并阶段，该阶段将合并map输出，并维持其顺序排序（相当于执行了sort），如果指定了combiner，在写入磁盘前还会执行combiner操作。

那么具体是如何合并的呢？ 合并因子默认是10，可以通过`io.sort.factor`属性设置。合并过程是循环进行了，可能叫经过多趟合并。目标是合并最小数量的文件以便满足最后一趟的合并系数。假设有40个文件，我们不会在四趟中每趟合并10个文件从而得到4个文件。相反，第一趟只合并4个文件，随后的三趟分别合并10个文件。再最后一趟中4个已合并的文件和余下的6个（未合并的）文件合计10个文件。具体流程如下图所示：

![](doc_img/mapreduce/mp_cache7.png)

注意：这并没有改变合并次数，它只是一个优化措施，目的是尽量减少写到磁盘的数据量，因为最后一趟总是直接合并到reduce。 看到这里您是否理解了Shuffle的具体原理呢，如果没有，也没有关系，接下来我们通过一个wordcount案例再将整个流程梳理一遍。

![](doc_img/mapreduce/mp_cache8.png)

在分区（分区规则：按首字母分四个区，分别为a-i,j-q,r-z,其它）的过程中，会将相同的单词合并到一起，将出现次数用逗号隔开，如上图所示。注意此时还没有排序。

接着执行排序操作，默认排序规则是按照key的字典升序排序，当然你也可以指定排序规则，排序后如下图所示：

![](doc_img/mapreduce/mp_cache9.png)

接下来执行combiner操作，将每个单词后续的1求和。

![](doc_img/mapreduce/mp_cache10.png)

combiner的结果如上图所示

map任务执行完，产生N个spill文件，接着对N个文件进行合并，分以下两种情况：

1.N<3，无论是否指定combiner类，合并文件时都不会执行combiner

![](doc_img/mapreduce/mp_cache11.png)

2.N>=3,如果指定了combiner类将执行combiner操作，如下图：

![](doc_img/mapreduce/mp_cache12.jpg)

接下来进入fetch（或copy）阶段

![](doc_img/mapreduce/mp_cache13.png)

然后在reduce端进行合并

然后执行最后一趟合并，并将结果直接传给reduce

![](doc_img/mapreduce/mp_cache14.png)

reduce task执行后，输出结果：

![](doc_img/mapreduce/mp_cache15.png)



## Hadoop安全模式

### 工作流程(理解)

1. 启动NameNode，NameNode加载fsimage到内存，对内存数据执行edits log日志中的事务操作。
2. 文件系统元数据内存镜像加载完毕，进行fsimage和edits log日志的合并，并创建新的fsimage文件和一个空的edits log日志文件。
3. NameNode等待DataNode上传block列表信息，直到副本数满足最小副本条件。最小副本条件指整个文件系统中有**99.9%**的block达到了**最小副本数**（默认值是1，可设置）
4. 当满足了最小副本条件，再过**30秒**，NameNode就会退出安全模式。

在NameNode安全模式（safemode）

1. 对文件系统元数据进行只读操作
2. 当文件的所有block信息具备的情况下，对文件进行只读操作
3. 不允许进行文件修改（写，删除或重命名文件）

### 注意事项

1. NameNode不会持久化block位置信息；DataNode保有各自存储的block列表信息。正常操作时，NameNode在内存中有一个blocks位置的映射信息（所有文件的所有文件块的位置映射信息）。
2. NameNode在安全模式，NameNode需要给DataNode时间来上传block列表信息到NameNode。如果NameNode不等待DataNode上传这些信息的话，则会在DataNode之间进行block的复制，而这在大多数情况下都是非必须的（因为只需要等待DataNode上传就行了），还会造成资源浪费。
3. 在安全模式NameNode不会要求DataNode复制或删除block。
4. 新格式化的HDFS不进入安全模式，因为DataNode压根就没有block。

### 配置信息

在`hdfs-site.xml`位置中进行配置。

| 属性名称                            | 类型    | 默认值 | 描述                                                         |
| ----------------------------------- | ------- | ------ | ------------------------------------------------------------ |
| dfs.namenode.replication.min        | int     | 1      | 写文件成功的最小副本数                                       |
| dfs.namenode.safemode.threshold-pct | float   | 0.999  | 系统中block达到了最小副本数的比例，之后NameNode会退出安全模式。小于等于0表示不进入安全模式，大于1表示永不退出安全模式 |
| dfs.namenode.safemode.extension     | int(ms) | 30000  | 当副本数达到最小副本条件之后安全模式延续的时间。对于小的集群（几十个节点），可以设置为0 |

一些常见的命令

```bash
# 查看安全模式状态
hdfs dfsadmin -safemode get
# 等待安全模式退出，之后进行文件的读写操作
hdfs dfsadmin -safemode wait
# 管理员有权在任何时间让namenode进入或退出安全模式。
# 进入安全模式
hdfs dfsadmin -safemode enter
# 退出安全模式
hdfs dfsadmin -safemode leave
```





## 高级主题

### HDFS读写文件流程

#### 写入

![](doc_img/hdfs_io.jpg)

**三大角色（读写）：HDFS client、NameNode、DataNode。**

1. 调用客户端的对象DistributedFileSystem的create方法：create(new Path("/xxx/xxx.xxx"))；

2. DistributedFileSystem会发起对namenode的一个RPC连接，请求创建一个文件，不包含关于block块的请求。namenode会执行各种各样的检查，判断客户端是否拥有有创建文件的权限，如果检查通过，namenode会创建一个文件（在edits中，同时更新内存状态），否则创建失败，客户端抛异常IOException，流程结束。hadoop2.x版本中如果指定的文件在对应的路径下已经存在，则创建失败；不存在则创建成功。hadoop3.x版本中如果指定的文件在对应的路径下已经存在，则覆盖。namenode在文件（空文件）创建后，返回给HDFSClient可以开始上传文件块。

   ```java
   FSDataOutputStream fsDataOutputStream = fileSystem.create(new Path("/xxx/xxx.xxx"));
   ```

   DistributedFileSystem返回一个FSDataOutputStream对象给客户端用于写数据。FSDataOutputStream封装了一个DFSOutputStream对象负责客户端跟datanode以及namenode的通信。

3. DFSOutputStream对象向NameNode(NN)请求上传文件块（比如：0-128M),让NN返回DataNode(DN)节点列表。namenode根据网络距离的远近和资源的使用情况来选择DN节点，并返回最适合的“三个”节点。

   DFSOutputStream对象收到DN列表后，按照一定规则请求建立一个通道DN1->DN2->DN3

   DN开启应答DN3->DN2->DN1->DFSOutputStream对象,至此双工通道建立完成。

4. DFSOutputStream对象将数据切分为小的数据包（64kb，core-default.xml：file.client-write-packet-size默认值65536），并写入到一个内部队列（“数据队列”）。DataStreamer会读取其中内容，DataStreamer将数据包发送给管线中的DN1，DN1将接收到的数据发送给DN2，DN2发送给DN3

5. DFSOoutputStream维护着一个数据包的队列，这的数据包是需要写入到datanode中的，该队列称为确认队列。当一个数据包在管线中所有datanode中写入完成，就从ack队列中移除该数据包。

   ![](doc_img/write-packet1.jpg)

6. 如果在数据写入期间datanode发生故障，则执行以下操作:

   1. 关闭管线，把确认队列中的所有包都添加回数据队列的最前端，以保证故障节点下游的datanode不会漏掉任何一个数据包。
   2. 为存储在另一正常datanode的当前数据块指定一个新的标志，并将该标志传送给namenode，以便故障datanode在恢复后可以删除存储的部分数据块。
   3. 从管线中删除故障数据节点并且把余下的数据块写入管线中另外两个正常的datanode。namenode在检测到副本数量不足时，会在另一个节点上创建新的副本。
   4. 后续的数据块继续正常接受处理。
   5. 在一个块被写入期间可能会有多个datanode同时发生故障，但非常少见。只要设置了`dfs.replication.min`的副本数（默认为1），写操作就会成功，并且这个块可以在集群中异步复制，直到达到其目标副本数（`dfs.replication`默认值为3）。

7. 如果有多个block，则会反复从步骤3开始执行，直至所有块写完。

8. 当客户端完成了所有的数据块的传输，调用数据流的close方法。该方法将数据队列中的剩余数据包写到DN的管线并等待管线的确认

9. 客户端收到管线中所有正常DN的确认消息后，通知NN文件写完了。

10. namenode已经知道文件由哪些块组成，所以它在返回成功前只需要等待数据块进行最小量的复制。

##### 节点距离计算

在HDFS写数据的过程中，NameNode会选择距离HDFS Client最近的DataNode节点接收数据。那么这个最近距离如何计算？

节点距离：两个节点到达最近的共同父节点的距离和。

![](doc_img/network-tp.jpg)

hadoop把网络看作是一棵树，两个节点间的距离是它们到最近共同祖先的距离和。通常可以设置等级：

1. 同一个节点上的进程
2. 同一机架上的不同节点
3. 同一数据中心中不同机架上的节点
4. 不同数据中心中的节点

##### 数据块副本放置策略

![](doc_img/block_st.jpg)

第一个副本：放置在上传文件的DataNode；如果是集群外提交，则随机挑选一台磁盘不太满，CPU不太忙的节点。

第二个副本：放置在于第一个副本不同的 机架的节点上。

第三个副本：与第二个副本相同机架的不同节点。

更多副本：随机节点

#### 读取

![](doc_img/dfs_r.jpg)

1. 客户端通过FileSystem对象的open方法打开希望下载的文件，DistributedFileSystem对象通过RPC调用namenode，以确保文件起始位置。对于每个block，namenode返回存有该副本的datanode地址。这些datanode根据它们与客户端的距离来排序。如果客户端本身就是一个datanode，并保存有相应block一个副本，会从本地读取这个block数据。
2. DistributedFileSystem返回一个FSDataInputStream对象给客户端读取数据。该类封装了DFSInputStream对象，该对象管理着datanode和namenode的I/O（网络连接），用于给客户端使用。客户端对这个输入调用read方法，存储着文件起始几个block的datanode地址的DFSInputStream连接距离最近的datanode。通过对数据流反复调用read方法，可以将数据从datanaode传输到客户端。到达block的末端时，DFSInputSream关闭与该datanode的连接，然后寻找下一个block的最佳datanode。客户端只需要读取连续的流，并且对于客户端都是透明的。
3. 客户端从流中读取数据时，block是按照打开DFSInputStream与datanode新建连接的顺序读取的。它也会根据需要询问namenode来检索下一批数据块的datanode的位置。一旦客户端完成读取，就close掉FSDataInputStream的输入流。
4. 在读取数据的时候如果DFSInputStream在与datanode通信时遇到错误，会尝试从这个块的一个最近邻datanode读取数据。它也记住那个故障datanode，保证以后不会反复读取该节点上后续的block。DFSInputStream也会通过校验和确认从datanode发来的数据是否完整。如果发现有损坏的块，就在DFSInputStream试图从其他datanode读取其副本之前通知namenode。
5. Client下载完block后会验证datanode中的MD5，保证块数据的完整性。

namenode告知客户端每个block中最佳的datanode，并让客户端直接连到datanode检索数据。由于数据流分散在集群中的所有datanode，这样可以使HDFS可扩展到大量的并发客户端。同时，namenode只需要响应block位置的请求，无需响应数据请求，否则namenode会成为瓶颈。

### Hadoop序列化与反序列化

序列化定义：就是把内存中的对象，转换为字节序列，以便于存储到磁盘或网络传输，此过程被称为序列化。

反序列化定义：将字节序列或磁盘中的持久化字节数据，转换为内存中的对象的过程。

> hadoop为什么需要序列化和反序列化？
>
> 数据经过mapper 任务的处理后，会产生溢出文件，这些文件会被保存到磁盘上。mapper任务完成后，reducer会通过http get的方式从mapper端拷贝对应分区的数据，中间需要经过网络传输。需要做持久化（存盘）或网络传输，这中间就需要做数据的序列化和反序列操作。

为什么不使用Java自带的序列化（Serializable）

Java的序列化是一个偏重量序列化，一个对象被序列化后，会带有很多的额外的信息，比如各种校验信息、继承体系、Header，从而导致体积较大。又由于hadoop处理的数据量一般都比较大，所以该方式不利于数据的传输。

Hadoop序列化的特点：

1. 紧凑：节省存储空间
2. 快捷：读写数据的额外开销比较小
3. 可扩展型更强：可以随着通讯协议升级而升级。
4. 跨语言：支持多语言的交互。

hadoop为基本的数据类型提供的对应的序列化和反序列化功能的类。如下图所示：

![](doc_img/dfs_ser.jpg)

| **Java类型** | **Hadoop Writable类型** |
| ------------ | ----------------------- |
| Byte         | ByteWritable            |
| Int          | IntWritable             |
| Long         | LongWritable            |
| Float        | FloatWritable           |
| Double       | DoubleWritable          |
| Boolean      | BooleanWritable         |
| String       | Text                    |
| Map          | MapWritable             |
| Array        | ArrayWritable           |
| Null         | NullWritable            |

有些时候这些基本的类不能满足我们的开发需求，需要自定义类，那么这些自定义的类如何实现序列化和反序列化呢？

具体实现的步骤如下：

1. 实现Writable接口

2. 预置一个空的构造函数，这是因为在发序列化时会被调用的

   ```java
   public Xxx(){
     super();
   }
   ```

3. 重写序列化的方法

   ```java
   @Override
   public void write(DataOutput out) throws IOException{
     out.writeInt(age);
     out.writeLong(xx);
      ....
   }
   ```

4. 重写反序列化的方法

   ```java
   @Override
   public void readFields(DataInput in) throws IOException{
     age = in.readInt();
     xx = in.readLong();
      ....
   }
   ```

   顺序一定要保持一致，先序列化的谁，一定要先反序列化谁。

5. 重写类的toString()方法

6. 如果该类需要作为Mapper的key中使用，还需要实现Comparable接口，这是因为Shuffle过程中需要对Mapper的key做排序。

## 附录

### Hadoop发展历史

1. 2002年10月，**Doug Cutting**和Mike Cafarella创建了开源网页爬虫项目Nutch。
2. 2003年10月，Google发表Google File System论文。
3. 2004年7月，Doug Cutting和Mike Cafarella在Nutch中实现了类似GFS的功能，即后来HDFS的前身。
4. 2004年10月，Google发表了MapReduce论文。
5. 2005年2月，Mike Cafarella在Nutch中实现了MapReduce的最初版本。
6. 2005年12月，开源搜索项目Nutch移植到新框架，使用MapReduce和HDFS在20个节点稳定运行。
7. 2006年1月，Doug Cutting加入雅虎，Yahoo!提供一个专门的团队和资源将Hadoop发展成一个可在网络上运行的系统。
8. 2006年2月，Apache Hadoop项目正式启动以支持MapReduce和HDFS的独立发展。
9. 2006年3月，Yahoo!建设了第一个Hadoop集群用于开发。
10. 2006年4月，第一个Apache Hadoop发布。
11. 2006年11月，Google发表了Bigtable论文，激起了Hbase的创建。
12. 2007年10月，第一个Hadoop用户组会议召开，社区贡献开始急剧上升。
13. 2007年，百度开始使用Hadoop做离线处理。
14. 2007年，中国移动开始在“大云”研究中使用Hadoop技术。
15. 2008年，淘宝开始投入研究基于Hadoop的系统——云梯，并将其用于处理电子商务相关数据。
16. 2008年1月，Hadoop成为Apache顶级项目。
17. 2008年2月，Yahoo!运行了世界上最大的Hadoop应用，宣布其搜索引擎产品部署在一个拥有1万个内核的Hadoop集群上。
18. 2008年4月，在900个节点上运行1TB排序测试集仅需209秒，成为世界最快。
19. 2008年8月，第一个Hadoop商业化公司Cloudera成立。
20. 2008年10月，研究集群每天装载10TB的数据。
21. 2009 年3月，Cloudera推出世界上首个Hadoop发行版——CDH（Cloudera's Distribution including Apache Hadoop）平台，完全由开放源码软件组成。
22. 2009年6月，Cloudera的工程师Tom White编写的《Hadoop权威指南》初版出版，后被誉为Hadoop圣经。
23. 2009年7月 ，Hadoop Core项目更名为Hadoop Common;
24. 2009年7月 ，MapReduce 和 Hadoop Distributed File System (HDFS) 成为Hadoop项目的独立子项目。
25. 2009年8月，Hadoop创始人Doug Cutting加入Cloudera担任首席架构师。
26. 2009年10月，首届Hadoop World大会在纽约召开。
27. 2010年5月，IBM提供了基于Hadoop 的大数据分析软件——InfoSphere BigInsights，包括基础版和企业版。
28. 2011年3月，Apache Hadoop获得Media Guardian Innovation Awards媒体卫报创新奖
29. 2012年3月，企业必须的重要功能HDFS NameNode HA被加入Hadoop主版本。
30. 2012年8月，另外一个重要的企业适用功能YARN成为Hadoop子项目。
31. 2014年2月，Spark逐渐代替MapReduce成为Hadoop的缺省执行引擎，并成为Apache基金会顶级项目。
32. 2017年12月，Release 3.0.0 generally available
33. 更多版本信息见：https://hadoop.apache.org/release.html

### hadoop 3.x 新特性

1. 将默认的最低jdk从7升级到8
2. 纠删码可以将3倍副本占据的空间压缩到1.5倍，并保持3倍副本的容错。由于在读取数据的时候需要进行额外的计算，用于存储使用不频繁的数据
3. 通过扩展YARN的资源类型，支持CPU和内存之外的其他资源，如GPU、FPGA、软件许可证、本地存储等。
4. 重写了hadoop中的shell脚本，修复了很多长期存在的bug并添加了新特性。有一些改进兼容老版本，有一些不兼容。
5. 对map阶段的输出收集器增加了本地实现，对于洗牌密集型工作，可以提高30%以上的性能。
6. hadoop2.x中NameNode的HA包含一个active的NameNode和一个Standby的NameNode。解决了系统中NameNode的单点故障问题。在hadoop3中允许多个standby状态的NameNode以达到更高级别容错的目的。
7. 以前，多个Hadoop服务的默认端口位于Linux临时端口范围（32768-61000）。 这意味着在启动时，由于与另一个应用程序的冲突，服务有时无法绑定到端口。这些冲突的端口已移出临时范围，影响NameNode，Secondary NameNode，DataNode和KMS。
8. Hadoop现在支持与Microsoft Azure Data Lake和Aliyun对象存储系统的集成，作为替代Hadoop兼容的文件系统。
9. 单个DataNode管理多个磁盘。 在正常写入操作期间，磁盘将被均匀填充。 但是，添加或替换磁盘可能会导致DataNode内的严重数据偏斜。 旧的HDFS平衡器不能处理，旧的HDFS平衡器处理DN之间而非内部的数据偏斜。
10. 对Hadoop守护进程以及MapReduce任务的堆管理做了一系列更改。现在可以根据主机的内存大小进行自动调整，并且不推荐使用HADOOP_HEAPSIZE变量。简化了map和reduce任务堆空间的配置，在任务中不再需要以java选项的方式进行指定
11. 为Amazon S3存储的S3A客户端添加了一个可选功能：能够将DynamoDB表用于文件和目录元数据的快速一致存储。
12. HDFS基于路由器的联邦添加了一个RPC路由层，该层提供多个HDFS命名空间的联合视图。 这与现有的ViewFs和HDFS联合功能类似，不同之处在于安装表由路由层而不是客户端在服务器端进行管理， 简化了对现有HDFS客户端对联邦群集的访问。

### Hadoop三大发行版本

Hadoop三大发行版本：Apache、Cloudera、Hortonworks。

Apache版本最原始（最基础）的版本，对于入门学习最好。

Cloudera内部集成了很多大数据框架。对应产品CDH。

Hortonworks文档较好。对应产品HDP。

1）Apache Hadoop

官网地址：http://hadoop.apache.org/releases.html

下载地址：https://archive.apache.org/dist/hadoop/common/

2）Cloudera Hadoop

官网地址：https://www.cloudera.com/downloads/cdh/5-10-0.html

下载地址：http://archive-primary.cloudera.com/cdh5/cdh/5/

（1）2008年成立的Cloudera是最早将Hadoop商用的公司，为合作伙伴提供Hadoop的商用解决方案，主要是包括支持、咨询服务、培训。

（2）2009年Hadoop的创始人Doug Cutting也加盟Cloudera公司。Cloudera产品主要为CDH，Cloudera Manager，Cloudera Support

（3）CDH是Cloudera的Hadoop发行版，完全开源，比Apache Hadoop在兼容性，安全性，稳定性上有所增强。Cloudera的标价为每年每个节点10000美元。

（4）Cloudera Manager是集群的软件分发及管理监控平台，可以在几个小时内部署好一个Hadoop集群，并对集群的节点及服务进行实时监控。

3）Hortonworks Hadoop

官网地址：https://hortonworks.com/products/data-center/hdp/

下载地址：https://hortonworks.com/downloads/#data-platform

（1）2011年成立的Hortonworks是雅虎与硅谷风投公司Benchmark Capital合资组建。

（2）公司成立之初就吸纳了大约25名至30名专门研究Hadoop的雅虎工程师，上述工程师均在2005年开始协助雅虎开发Hadoop，贡献了Hadoop80%的代码。

（3）Hortonworks的主打产品是Hortonworks Data Platform（HDP），也同样是100%开源的产品，HDP除常见的项目外还包括了Ambari，一款开源的安装和管理系统。

（4）Hortonworks目前已经被Cloudera公司收购。

### 关联项目

1. Apache Ambari是一种基于Web的工具，支持Apache Hadoop集群的供应、管理和监控。Apache Ambari 支持HDFS、MapReduce、Hive、Pig、Hbase、Zookeepr、Sqoop和Hcatalog等的集中管理。也是5个顶级hadoop管理工具之一。
2. Avro™:数据序列化系统
3. hive是基于Hadoop的一个数据仓库工具，可以将结构化的数据文件映射为一张数据库表，并提供简单的sql查询功能，可以将sql语句转换为MapReduce任务进行运行。
4. Mahout 提供一些可扩展的机器学习领域经典算法的实现，旨在帮助开发人员更加方便快捷地创建智能应用程序。Mahout包含许多实现，包括聚类、分类、推荐过滤、频繁子项挖掘。此外，通过使用 Apache Hadoop 库，Mahout 可以有效地扩展到云中。
5. Apache Pig 是一个高级过程语言，适合于使用 Hadoop 和 MapReduce 平台来查询大型半结构化数据集。通过允许对分布式数据集进行类似 SQL 的查询，Pig 可以简化 Hadoop 的使用。
6. Apache Spark 是专为大规模数据处理而设计的快速通用的计算引擎。Spark是UC Berkeley AMP lab开源的类Hadoop MapReduce的通用并行框架，拥有MapReduce所具有的优点；但是Job中间输出结果可以保存在内存中，从而不再需要读写HDFS，因此Spark能更好地适用于数据挖掘与机器学习等需要迭代的MapReduce的算法。
7. Tez 是 Apache 最新的支持 DAG 作业的开源计算框架。它允许开发者为最终用户构建性能更快、扩展性更好的应用程序。Hadoop传统上是一个大量数据批处理平台。但是，有很多用例需要近乎实时的查询处理性能。还有一些工作则不太适合MapReduce，例如机器学习。Tez的目的就是帮助Hadoop处理这些用例场景。
8. [ZooKeeper](../zookeeper/README.MD)是一个分布式的，开放源码的分布式应用程序协调服务，是Google的Chubby一个开源的实现，是Hadoop和Hbase的重要组件。它是一个为分布式应用提供一致性服务的软件，提供的功能包括：配置维护、域名服务、分布式同步、组服务等。
9. HBase是一个分布式的、高可靠性、高性能、面向列、可伸缩的分布式存储系统，该技术来源于Fay Chang所撰写的Google论文“Bigtable：一个结构化数据的分布式存储系统”。就像Bigtable利用了Google文件系统（File System）所提供的分布式数据存储一样，HBase在Hadoop之上提供了类似于Bigtable的能力。

### 配置文件、端口说明

Hadoop配置文件分两类：默认配置文件和自定义配置文件，只有用户想修改某一默认配置值时，才需要修改自定义配置文件，更改相应属性值。

默认配置文件：

| 默认文件           | 文件存放在Hadoop的jar包中的位置           |
| ------------------ | ----------------------------------------- |
| [core-default.xml] | hadoop-common-3.1.3.jar/ core-default.xml |
| [hdfs-default.xml] | hadoop-hdfs-3.1.3.jar/ hdfs-default.xml   |

自定义配置文件：

**core-site.xml、hdfs-site.xml**两个配置文件存放在$HADOOP_HOME/etc/hadoop这个路径上，用户可以根据项目需求重新进行修改配置。

| 文件                                | 作用                                               |
| ----------------------------------- | -------------------------------------------------- |
| hadoop-env.sh                       | 找jdk环境变量                                      |
| workers（hadoop2.x中的名称slaves ） | 指定datanode节点                                   |
| core-site.xml                       | 涉及到的参数如果没有配置，找对应的默认文件中的配置 |
| hdfs-site.xml                       | 涉及到的参数如果没有配置，找对应的默认文件中的配置 |

常用端口号

| Daemon                  | App                          | Hadoop2     | Hadoop3 |
| ----------------------- | ---------------------------- | ----------- | ------- |
| NameNode Port           | Hadoop HDFS NameNode         | 8020 / 9000 | 9820    |
|                         | Hadoop HDFS NameNode HTTP UI | 50070       | 9870    |
| Secondary NameNode Port | Secondary NameNode           | 50091       | 9869    |
|                         | Secondary NameNode HTTP UI   | 50090       | 9868    |
| DataNode Port           | Hadoop HDFS DataNode IPC     | 50020       | 9867    |
|                         | Hadoop HDFS DataNode         | 50010       | 9866    |
|                         | Hadoop HDFS DataNode HTTP UI | 50075       | 9864    |

### NameNode故障处理

NameNode故障后，可以采用如下两种方法恢复数据。

1. 将SecondaryNameNode中数据拷贝到NameNode存储数据的目录；

   1. kill -9 NameNode进程id(可以通过jps查看nodename进程的id)

   2. 删除NameNode存储的数据

      ```bash
      rm -rf /var/shaoteemo/hadoop/full/dfs/name/*
      ```

   3. 拷贝SecondaryNameNode中数据到原NameNode存储数据目录

      ```bash
      scp -r root@centos7-node2:/var/shaoteemo/hadoop/full/dfs/namesecondary/* /var/shaoteemo/hadoop/full/dfs/name/
      ```

   4. 重新启动NameNode

      ```bash
      hdfs --daemon start namenode
      ```

2. 使用-importCheckpoint选项启动NameNode守护进程，从而将SecondaryNameNode中数据拷贝到NameNode目录中。

   1. 修改`hdfs-site.xml`中的

      ```xml
      <property>
          <name>dfs.namenode.checkpoint.period</name>
          <value>120</value>
      </property>
      <property>
          <name>dfs.namenode.name.dir</name>
          <value>/var/shaoteemo/hadoop/full/dfs/name</value>
      </property>
      ```

   2. kill -9 NameNode进程id

   3. 删除NameNode存储的数据`/var/shaoteemo/hadoop/full/dfs/name`

      ```bash
      rm -rf /var/shaoteemo/hadoop/full/dfs/name/*
      ```

   4. 如果SecondaryNameNode不和NameNode在一个主机节点上，需要将SecondaryNameNode存储数据的目录拷贝到NameNode存储数据的平级目录，并删除`in_use.lock`文件

   5. 导入检查点数据（结束后ctrl+c结束即可）

      ```bash
      hdfs namenode -importCheckpoint
      ```

   6. 启动NameNode

      ```bash
      hdfs --daemon start namenode
      ```

### NameNode多目录配置

NameNode的本地目录可以配置成多个，且**每个目录存放内容相同**，增加了可靠性

具体配置如下：

1. 停止集群，在对应节点的`hdfs-site.xml`文件中添加如下内容

   ```xml
   <property>
       <name>dfs.namenode.name.dir</name>
   <value>file://${hadoop.tmp.dir}/dfs/name1,file://${hadoop.tmp.dir}/dfs/name2</value>
   </property>
   ```

2. 删除对应节点的/var/shaoteemo/hadoop/full和/opt/hadoop-3.1.3/logs中所有数据。

3. 格式化集群并启动。

   ```bash
   hdfs namenode –format
   start-dfs.sh
   ```

4. 验证结果

   ```bash
   ls -l /var/shaoteemo/hadoop/full/dfs
   ```


### NameNode与JournalNode在同一节点导致某一NN节点停止解决方案

![](doc_img/nn_err.png)

本方法根据日志日志内容，发现NN在与JNN通信时出现超时，在NN多次尝试后关闭。整个集未完全启动导致重试次数用尽，因此增加对应重试次数。**不适用于配置导致或例外情况**。

在core-site.xml添加如下属性

```xml
<!--下面的参数解决NameNode启动后停止的解决-->
<property>
    <name>ipc.client.connect.max.retries</name>
    <value>100</value>
    <description>Indicates the number of retries a client will make to establish a server connection.
    </description>
</property>
<property>
    <name>ipc.client.connect.retry.interval</name>
    <value>10000</value>
    <description>Indicates the number of milliseconds a client will wait for before retrying to establish a server connection.
    </description>
</property>
```

