<h1>AMQP-RabbitMQ</h1>

## 写在前面

撰写人：ShaoTeemo

本文档包含了RabbitMQ的使用和部分原理。其中包含大量官方文档引用及部分三方文档链接引用。后期会翻译部分RabbitMQ原理相关内容。因此本文更多的内容**来自[官方文档](https://www.rabbitmq.com/documentation.html)**。

有较详细的项目搭建过程的文件信息，*所有内容均在指定的环境中测试运行通过*，请**注意环境信息的匹配**。

文中出现的译注为本人(译者)观点。

## 环境信息

|           技术栈            |            版本号             |
| :-------------------------: | :---------------------------: |
|         Spring-Boot         |             2.5.5             |
|       RabbitMQ Server       |             3.9.7             |
|           Erlang            |           OTP 24.1            |
|         Oracle JDK          |            11.0.12            |
| Linux（CentOS Base AliYun） | CentOS Linux release 8.4.2105 |
|            Maven            |             3.6.3             |

图形绘制：Microsoft Office Visio

## RabbitMQ自述

## 环境组件及信息获取

[SpringBoot](https://spring.io/projects/spring-boot)、[Erlang](https://www.erlang.org/)、[RabbitMQ](https://www.rabbitmq.com/)

## 环境搭建细节

RabbitMQ与Erlang版本匹配：[RabbitMQ Erlang Version Requirements](https://www.rabbitmq.com/which-erlang.html)

安装MAKE、GCC C++ 等依赖：

```bash
$ yum -y install make gcc gcc-c++ kernel-devel m4 ncurses-devel openssl-devel unixODBC unixODBC-devel
```

### 1.Elang环境安装

#### 1.1从源代码编译 Erlang安装

编译安装官方文档：[building and installation instructions](https://github.com/erlang/otp/blob/maint/HOWTO/INSTALL.md)

步骤：

1. 解压erlang源码包并进入解压后的文件夹。

2. 配置configure参数

   ```sh
   $ ./configure --prefix=<此处填写安装目录> --with-ssl --enable-threads --enable-smp-support --enable-kernel-poll --enable-hipe --without-javac
   ```

3. 编译

   ```bash
   $ make
   ```

4. 安装

   ```bash
   $ make install
   ```

5. 配置环境变量。在`/etc/profile`中新增如下变量。并生效

   ```shell
   export PATH=$PATH:<安装目录绝对路径>/bin
   ```

   ```bash
   $ source /etc/profile
   ```

   查看是否配置成功

   ```bash
   $ erl -version
   ```

   ```
   output:
   	Erlang (SMP,ASYNC_THREADS) (BEAM) emulator version 12.1
   ```

#### 1.2预构建的二进制包

官方文档：https://www.erlang.org/downloads

大多数操作系统包管理器提供预先构建的二进制包。您还可以从 Erlang Solutions 下载最新的稳定版本。 Erlang Solutions 为 OS X、Windows、Ubuntu、Debian、Fedora、CentOS、Raspbian 和其他操作系统提供预构建的二进制包。

- For [Homebrew](http://brew.sh/) on OS X: `brew install erlang`
- For [MacPorts](https://www.macports.org/) on OS X: `port install erlang`
- For [Ubuntu](http://www.ubuntu.com/) and [Debian](https://www.debian.org/): `apt-get install erlang`
- For [Fedora](https://getfedora.org/): `yum install erlang`
- For [FreeBSD](https://www.freebsd.org/): `pkg install erlang`

## 高级进阶

### 1.AMQP 0-9-1 模型解释：见《官方文档-AMQP 0-9-1 Model Explained中文版》

### 2.队列指南（Queues guide）：见《官方文档-Queue and Consumer Features中文版》

### 

## 扩展知识

## 名词解释

### TLS

安全传输层协议（TLS）用于在两个通信应用程序之间提供保密性和数据完整性。

该协议由两层组成： TLS 记录协议（TLS Record）和 TLS 握手协议（TLS Handshake）。

