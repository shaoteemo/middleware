# Apache Hive教程中文版

注意本文档介绍Hive相关内容。

## 写在前面

撰写人：ShaoTeemo

本文档包含了Hive的使用和部分原理。其中包含大量官方文档引用及部分三方文档链接引用。

有较详细的项目搭建过程的文件信息，*所有内容均在指定的环境中测试运行通过*，请**注意环境信息的匹配**。

文中出现的译注为本人(译者)观点。

## 环境信息

|      技术栈       |            版本号             |
| :---------------: | :---------------------------: |
|    Spring-Boot    |            2.7.18             |
|       Hive        |             4.0.1             |
|       MySQL       |            5.7.28             |
| MySQL-Connector-J |            5.1.49             |
|      Hadoop       |             3.3.6             |
|     Zookeeper     |             3.7.2             |
|    Oracle JDK     |              1.8              |
|       Linux       | CentOS Linux release 8.4.2105 |
|       Maven       |             3.9.8             |

图形绘制：Microsoft Office Visio 

## 相关组件及环境工具获取

[Apache Hive官网](https://hive.apache.org/)、[Hive下载地址](https://archive.apache.org/dist/hive/)

## Hive概述

Apache Hive数据仓库软件方便了使用SQL读取、写入和管理驻留在分布式存储中的大型数据集。结构可以投影到已经存储的数据上。提供命令行工具和JDBC驱动程序将用户连接到Hive。

由Facebook开源用于解决海量结构化日志的数据统计工具。Hive是基于Hadoop的一个数据仓库工具，可以将结构化的数据文件映射为一张表，并提供类SQL查询功能。

**Hive产生的原因：**非java编程者通过SQL语句对hdfs的数据做mapreduce操作。

**Hive本质**：将HQL转化成MapReduce程序

1. Hive处理的数据存储在HDFS
2. Hive分析数据底层的实现是MapReduce
3. 执行程序运行在Yarn上

![](doc_img/hive-bz.png)

## 环境搭建

### 安装方式

Hive中metastore（元数据存储）的三种模式：

1. 内嵌Derby模式（默认）

   ![](doc_img/install-1.png)

   此模式连接到一个In-memory 的数据库Derby，一般用于Unit Test（单元测试目前用的也少），一台服务器即可，基本不用。

2. 直连数据库模式（MySQL）

   通过网络连接到一个数据库中。

   ![](doc_img/install-2.png)

3. **远程服务器模式**（生产）

   用于非Java客户端访问元数据库，在服务器端启动MetaStoreServer，客户端利用Thrift协议通过MetaStoreServer访问元数据库。起到了解耦的作用，如果更换其他数据库只需要修改MetaStoreServer即可。

   ![](doc_img/install-3.png)

### MySQL安装

**MySQL安装见MySQL项目文档**

![](doc_img/hive_db_req.jpg)

### 直连数据库（MySQL）模式

1. 上传hive与mysql驱动包。解压hive到指定目录。

2. 添加环境变量至`profile`。并使其生效。

   ```bash
   export HIVE_HOME=/opt/hive-3.1.3
   export PATH=$PATH:$HIVE_HOME/bin
   ```

3. (如果有)日志jar包冲突解决。
   备份原有jar

   ```bash
   mv $HIVE_HOME/lib/log4j-slf4j-impl-2.18.0.jar $HIVE_HOME/lib/log4j-slf4j-impl-2.18.0.jar.bkp
   ```

4. 拷贝MySQL驱动至`lib`目录即可。

5. 配置连接MySQL

   进入conf目录：`vim hive-site.xml`复制以下内容。

   ```xml
   <?xml version="1.0"?>
   <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
   <configuration>
       <!-- jdbc连接的URL设置 -->
       <property>
           <name>javax.jdo.option.ConnectionURL</name>
           <value>jdbc:mysql://192.168.20.101:3306/hive?useSSL=false</value>
       </property>
       <!-- jdbc连接的Driver类设置-->
       <property>
           <name>javax.jdo.option.ConnectionDriverName</name>
           <value>com.mysql.jdbc.Driver</value>
       </property>
       <!-- 指定jdbc连接的username-->
       <property>
           <name>javax.jdo.option.ConnectionUserName</name>
           <value>root</value>
       </property>
       <!-- 指定jdbc连接mysql的password -->
       <property>
           <name>javax.jdo.option.ConnectionPassword</name>
           <value>xxxx</value>
       </property>
       <!-- Hive默认在HDFS的工作目录 -->
       <property>
           <name>hive.metastore.warehouse.dir</name>
           <value>/user/hive/warehouse</value>
       </property>
       <!-- Hive元数据存储的验证（生产环境视情况配置） -->
       <property>
           <name>hive.metastore.schema.verification</name>
           <value>false</value>
       </property>
       <!-- 元数据存储授权（生产环境视情况配置） -->
       <property>
           <name>hive.metastore.event.db.notification.api.auth</name>
           <value>false</value>
       </property>
   </configuration>
   ```

6. 在MySQL中创建hive schemas。

7. 初始化Hive元数据至MySQL

   ```bash
   # 显示帮助
   schematool --help
   # 初始化操作。完成后74张表。
   schematool -initSchema -dbType mysql -verbose
   ```

8. 启动Hive。

   ```bash
   hive
   ```

9. 功能测试

   ```hive
   -- 在hive中的数据操作展示
   -- 显示数据库
   show databases;
   -- 显示表
   show tables;
   -- 创建表
   create table tb_test(id int);
   create table test(id int,age int);
   show tables;
   -- 插入数据
   insert into test values(100,100);
   -- 查看数据
   select * from test;
   ```

   访问hadoop节点http://192.168.20.102:9870/explorer.html#/user/hive/warehouse可以查看创建的数据表目录及数据文件

   查看Yarn可以查询最近执行http://192.168.20.104:8088/cluster/apps

   也可以通过MySQL查看对应的信息（以下列举部分）：

   ```SQL
   # MySQL中的表
   # 数据库实例信息
   select * from hive.dbs;
   # 表数据信息
   select * from hive.tbls;
   # 表中列数据信息
   select * from hive.columns_v2;
   ```

   表查询结果展示：

   ```json
   // dbs
   [
     {
       "DB_ID": 1,
       "DESC": "Default Hive database",
       "DB_LOCATION_URI": "hdfs://mycluster/user/hive/warehouse",
       "NAME": "default",
       "OWNER_NAME": "public",
       "OWNER_TYPE": "ROLE",
       "CTLG_NAME": "hive"
     }
   ]
   ```

   ```json
   // tbls
   [
     {
       "TBL_ID": 1,
       "CREATE_TIME": 1730911859,
       "DB_ID": 1,
       "LAST_ACCESS_TIME": 0,
       "OWNER": "root",
       "OWNER_TYPE": "USER",
       "RETENTION": 0,
       "SD_ID": 1,
       "TBL_NAME": "test",
       "TBL_TYPE": "MANAGED_TABLE",
       "VIEW_EXPANDED_TEXT": null,
       "VIEW_ORIGINAL_TEXT": null,
       "IS_REWRITE_ENABLED": false
     },
     {
       "TBL_ID": 2,
       "CREATE_TIME": 1730911894,
       "DB_ID": 1,
       "LAST_ACCESS_TIME": 0,
       "OWNER": "root",
       "OWNER_TYPE": "USER",
       "RETENTION": 0,
       "SD_ID": 2,
       "TBL_NAME": "test2",
       "TBL_TYPE": "MANAGED_TABLE",
       "VIEW_EXPANDED_TEXT": null,
       "VIEW_ORIGINAL_TEXT": null,
       "IS_REWRITE_ENABLED": false
     }
   ]
   ```

   ```json
   // columns_v2
   [
     {
       "CD_ID": 1,
       "COMMENT": null,
       "COLUMN_NAME": "age",
       "TYPE_NAME": "int",
       "INTEGER_IDX": 1
     },
     {
       "CD_ID": 1,
       "COMMENT": null,
       "COLUMN_NAME": "id",
       "TYPE_NAME": "int",
       "INTEGER_IDX": 0
     },
     {
       "CD_ID": 2,
       "COMMENT": null,
       "COLUMN_NAME": "age",
       "TYPE_NAME": "int",
       "INTEGER_IDX": 1
     },
     {
       "CD_ID": 2,
       "COMMENT": null,
       "COLUMN_NAME": "id",
       "TYPE_NAME": "int",
       "INTEGER_IDX": 0
     },
     {
       "CD_ID": 2,
       "COMMENT": null,
       "COLUMN_NAME": "name",
       "TYPE_NAME": "string",
       "INTEGER_IDX": 2
     }
   ]
   ```

直连数据库模式：目前的这种配置方式，是将hive的服务器端和客户端放在一台服务器上。如果想通过jdbc程序访问该hive是没有办法的，这是因为hive没有开启hiveserver2服务（Metastore）。

### 远程服务器模式（推荐）

Doc:https://cwiki.apache.org/confluence/display/Hive/AdminManual+Metastore+3.0+Administration#AdminManualMetastore3.0Administration-MetastoreServer

节点规划：两个节点一个节点作为服务端，一个节点为客户端。自行虚拟机构建。

1. 拷贝直连模式的hive根文件夹至两个节点。或者重新上传tar包解压到指定节点的指定位置，注意驱动的引入。

2. 环境变量与直连数据库模式一样。两个节点：

   ```bash
   export HIVE_HOME=/opt/hive-3.1.3
   export PATH=$PATH:$HIVE_HOME/bin
   ```

3. 服务端的配置：无需改动。如果是直连切换至本模式需要初始化数据库。也可以重新建立数据库和hadoop仓库位置。

   在本节点启动Metadatastore服务

   ```bash
   hive --service metastore
   # 本指令前台阻塞，推荐使用后台启动。
   nohup hive --service metastore &
   ```

   通过网络工具`netstat`检索此服务端口为：`9083`

4. 客户端配置：修改位置文件`hive-site.xml`

   ```xml
   <?xml version="1.0"?>
   <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
   <configuration>
       <!-- Hive默认在HDFS的工作目录 -->
       <property>
           <name>hive.metastore.warehouse.dir</name>
           <value>/user/hive_remote/warehouse</value>
       </property>
       <!-- Hive元数据存储的验证（生产环境视情况配置） -->
       <property>
           <name>hive.metastore.schema.verification</name>
           <value>false</value>
       </property>
       <!-- 元数据存储授权（生产环境视情况配置） -->
       <property>
           <name>hive.metastore.event.db.notification.api.auth</name>
           <value>false</value>
       </property>
       <!--指定hive服务端-->
       <property>
           <name>metastore.thrift.uris</name>
           <value>thrift://centos7-node3:9083</value>
       </property>
   </configuration>
   ```

   在本客户端启动`hive`即可。

5. 测试

   任意创建一个表即可在对应的hadoop中出现指定的文件夹查看即可。

   ```hive
   create table test(id int,age int);
   insert into test values(12,233),(13,244);
   select * from test;
   ```

   通过hdfs下载数据文件后使用`cat -A  <file fath>`得知`^A`是Hive的默认分隔符 。

## HiveSQL（HQL）

官方文档：[LanguageManual](https://cwiki.apache.org/confluence/display/Hive/LanguageManual)

### Hive中的数据类型

```hive
data_type
  : primitive_type
  | array_type
  | map_type
  | struct_type
  | union_type  -- (Note: Available in Hive 0.7.0 and later)
 
primitive_type
  : TINYINT
  | SMALLINT
  | INT
  | BIGINT
  | BOOLEAN
  | FLOAT
  | DOUBLE
  | DOUBLE PRECISION -- (Note: Available in Hive 2.2.0 and later)
  | STRING
  | BINARY      -- (Note: Available in Hive 0.8.0 and later)
  | TIMESTAMP   -- (Note: Available in Hive 0.8.0 and later)
  | DECIMAL     -- (Note: Available in Hive 0.11.0 and later)
  | DECIMAL(precision, scale)  -- (Note: Available in Hive 0.13.0 and later)
  | DATE        -- (Note: Available in Hive 0.12.0 and later)
  | VARCHAR     -- (Note: Available in Hive 0.12.0 and later)
  | CHAR        -- (Note: Available in Hive 0.13.0 and later)

array_type
  : ARRAY < data_type >
 
map_type
  : MAP < primitive_type, data_type >
 
struct_type
  : STRUCT < col_name : data_type [COMMENT col_comment], ...>
 
union_type
   : UNIONTYPE < data_type, data_type, ... >  -- (Note: Available in Hive 0.7.0 and later)
```

基本数据类型

| 类型名称  | 描述                            | 举例          |
| --------- | ------------------------------- | ------------- |
| boolean   | true/false                      | true          |
| tinyint   | 1字节的有符号整数               | 1             |
| smallint  | 2字节的有符号整数               | 1             |
| int       | 4字节的有符号整数               | 1             |
| bigint    | 8字节的有符号整数               | 1             |
| float     | 4字节单精度浮点数               | 1.0           |
| double    | 8字节单精度浮点数               | 1.0           |
| string    | 字符串(不设长度)                | “gtjin”       |
| varchar   | 字符串（1-65355长度，超长截断） | “geekmooc.cn” |
| timestamp | 时间戳                          | 161243654     |
| date      | 日期                            | 20211110      |

复合数据类型

| 类型名称 | 描述                                                  | 举例              |
| -------- | ----------------------------------------------------- | ----------------- |
| array    | 一组有序的字段，字段类型必须相同 array(元素1，元素2)  | Array（1,2,3）    |
| map      | 一组无序的键值对 map(k1,v1,k2,v2)                     | Map(‘a’,1,'b',2)  |
| struct   | 一组命名的字段，字段类型可以不同 struct(元素1，元素2) | Struct('a',1,2,0) |

**array字段的元素访问方式：**

- 下标获取元素，下标从0开始
- 获取第一个元素：array[0]

**map字段的元素访问方式**

- 通过键获取值
- 获取a这个key对应的value:map['a']

**struct字段的元素获取方式**

- 定义一个字段c的类型为struct{a int;b string}
- 获取a和b的值:使用c.a 和c.b 获取其中的元素值
- 这里可以把这种类型看成是一个对象

```hive
 create table complex(
    col1 array<int>,
    col2 map<string,int>,
    col3 struct <a:string,b:int,c:double>
 )
```



### DDL语言

#### Database/schema操作

##### Create Database

创建数据库实例

```hive
CREATE [REMOTE] (DATABASE|SCHEMA) [IF NOT EXISTS] database_name
  [COMMENT database_comment]
  [LOCATION hdfs_path]
  [MANAGEDLOCATION hdfs_path]
  [WITH DBPROPERTIES (property_name=property_value, ...)];
```

```hive
-- 常用语法
CREATE DATABASE IF NOT EXISTS database_name COMMENT database_comment;
```

##### Dorp Database

删除数据库实例

```hive
DROP (DATABASE|SCHEMA) [IF EXISTS] database_name [RESTRICT|CASCADE];
```

```hive
DROP DATABASE IF EXISTS database_name;
-- 如果数据库中有数据表
DROP DATABASE IF EXISTS database_name CASCADE;
```

##### Alter Database

修改数据库

```hive
-- 修改数据库的属性
ALTER (DATABASE|SCHEMA) database_name SET DBPROPERTIES (property_name=property_value, ...);   -- (Note: SCHEMA added in Hive 0.14.0)
-- 修改数据库所有者或所属角色
ALTER (DATABASE|SCHEMA) database_name SET OWNER [USER|ROLE] user_or_role;   -- (Note: Hive 0.13.0 and later; SCHEMA added in Hive 0.14.0)
-- 修改数据库示例在hdfs上的位置
ALTER (DATABASE|SCHEMA) database_name SET LOCATION hdfs_path; -- (Note: Hive 2.2.1, 2.4.0 and later)

ALTER (DATABASE|SCHEMA) database_name SET MANAGEDLOCATION hdfs_path; -- (Note: Hive 4.0.0 and later)
```

##### USE Database

使用/切换数据库

```hive
USE <database_name>;
USE DEFAULT;
```

##### Show Database

查看数据库实例

```hive
SHOW (DATABASES|SCHEMAS) [LIKE 'identifier_with_wildcards'];
```

```hive
show databases;
show databases LIKE "*";
```

##### Describe Database

数据库示例详情

```hive
DESCRIBE DATABASE [EXTENDED] db_name;
DESCRIBE SCHEMA [EXTENDED] db_name;    -- (Note: Hive 1.1.0 and later)
```

```hive
DESCRIBE DATABASE db_name;
-- 或
DESC DATABASE db_name;
```



## 高级主题

### Hive架构

![](doc_img/hive-jg.jpg)

Hive的架构

1. 用户接口主要有三个：CLI，JDBC/ODBC和 WebUI。其中最常用的是CLI，Cli启动的时候，会同时启动一个Hive副本。Client是Hive的客户端，用户连接至Hive Server。在启动 Client模式的时候，需要指出Hive Server所在节点，并且在该节点启动Hive Server。 WUI是通过浏览器访问Hive。

2. Hive将元数据存储在数据库中，如mysql、derby（hive自带的内存数据库）。Hive中的元数据包括表的名字，表的列和分区及其属性，表的属性（是否为外部表等），表的数据所在目录等。

3. 解释器*（SQL Parser）*、编译器*（Compiler）*、优化器（Optimizer）完成HQL查询语句从词法分析、语法分析、编译、优化以及查询计划的生成。生成的查询计划存储在HDFS中，并在随后有执行器（Executor）调用MapReduce执行。

   解释器：将HQL字符串转换为抽象语法树AST，通过使用第三方工作完成，比较antlr;对AST进行语法分析，比如表是否存在、字段是否存在、HQL语义是否存在错误等。

   编译器：将AST编译生成逻辑执行计划。

   优化器：对逻辑执行计划进行优化。

   执行器：把优化后的逻辑执行计划转换成可以运行的物理计划。对于HIve来说就是MR/Spark。

4. Hive的数据存储在HDFS中，大部分的查询、计算由MapReduce完成（包含*的查询，比如select * from tbl不会生成MapRedcue任务）。

   比如：`select id,name from psn；`

   ![](doc_img/hive-jg2.png)

   **Hive架构**

   编译器将一个Hive SQL转换操作符

   操作符是Hive的最小的处理单元

   每个操作符代表HDFS的一个操作或者一道MapReduce作业

   **Operator（操作符）**

   Operator都是hive定义的一个处理过程

   Operator都定义有:

   ```java
   protected List <Operator<? extends Serializable >> childOperators; 
   protected List <Operator<? extends Serializable >> parentOperators; 
   protected boolean done; // 初始化值为false
   ```

   ![](doc_img/hive-jg3.png)

   查询操作 				表扫描操作 			限制输出 			文件输出操作。

   ANTLR词法语法分析工具解析hql

   ![](doc_img/hive-jg4.png)

   

## 附录

### Hive和关系型数据库区别

常见关系型数据库：mysql、oracle、SqlServer、DB2等等

| 对比项             | Hive                   | 关系型数据库           |
| ------------------ | ---------------------- | ---------------------- |
| 查询语句           | HQL                    | SQL                    |
| 数据存储           | HDFS                   | Local FS or RawDevice  |
| 执行器             | MapReducer             | Executor               |
| 数据insert         | 支持批量导入和单条插入 | 支持批量导入和单条插入 |
| 数据Update和delete | 支持追加，不支持删除   | 行级别更新和删除       |
| 数据规模           | 大                     | 小                     |
| 执行延迟           | 高                     | 低                     |
| 分区               | 支持                   | 支持                   |
| 索引               | v0.8后支持             | 支持                   |
| 数据加载模式       | 读时模式（快）         | 写时模式（慢）age int  |
| 扩展性             | 高                     | 低                     |
| 应用场景           | 海量数据查询           | 实时查询               |

### Hive的优缺点

#### 优点

1. 操作接口采用类SQL语法，提供快速开发的能力（简单、容易上手）。
2. 避免了去写MapReduce，减少开发人员的学习成本。
3. Hive的执行延迟比较高，因此Hive常用于数据分析，对实时性要求不高的场合。
4. Hive优势在于处理大数据，对于处理小数据没有优势，因为Hive的执行延迟比较高。
5. Hive支持用户自定义函数，用户可以根据自己的需求来实现自己的函数。

####  缺点

1. Hive的HQL表达能力有限
   1. 迭代式算法无法表达
   2.  数据挖掘方面不擅长，由于MapReduce数据处理流程的限制，效率更高的算法却无法实现。
2. Hive的效率比较低
   1. Hive自动生成的MapReduce作业，通常情况下不够智能化
   2. Hive调优比较困难，粒度较粗

### 一些思考

**思考：hive是如何连接到hdfs的？**

答：通过环境变量。
