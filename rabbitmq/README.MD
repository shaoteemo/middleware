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

### 1.AMQP 0-9-1 模型解释

本节翻译自官方文档：[AMQP 0-9-1 Overview](https://www.rabbitmq.com/tutorials/amqp-concepts.html) 。原文复制版：《官方文档-AMQP 0-9-1 Model Explained.md》仅用于翻译对比。

#### 1.1概述

本指南概述了 AMQP 0-9-1 协议，RabbitMQ 支持的协议之一。

#### 1.2AMQP 0-9-1 和 AMQP 模型的高级概述

##### 1.2.1什么是AMQP 0-9-1？

AMQP 0-9-1（高级消息队列协议）是一种消息传递协议，它使符合要求的客户端应用程序能够与符合要求的消息传递中间件代brokers行通信。

##### 1.2.2Brokers及他们的角色

消息传递 broker 从[发布者](https://www.rabbitmq.com/publishers.html)（发布它们的应用程序，也称为生产者）接收消息并将它们路由到[消费者](https://www.rabbitmq.com/consumers.html)（处理它们的应用程序）。

由于它是一个网络协议，发布者、消费者和broker都可以驻留在不同的机器上。

##### 1.2.3AMQP 0-9-1 模型简介

AMQP 0-9-1 模型具有以下世界观：消息发布（published）到交换器（*exchanges*），通常将其比作邮局或邮箱。交换然后使用称为绑定（*bindings*）的规则将消息副本分发到队列（*queues*）。然后broker要么将消息传递给订阅队列的消费者（ consumers ），要么消费者按需从队列中获取/拉取（fetch/pull）消息。

![](http://rep.shaoteemo.com/hello-world-example-routing.png)

发布消息时，发布者可以指定各种消息属性（消息元数据）。其中一些元数据可能由broker使用，但是，其余的元数据对broker是完全不透明的，并且仅由接收消息的应用程序使用。

当网络不可靠时，应用程序可能无法处理消息，因此 AMQP 0-9-1 模型有一个消息确认的概念：当消息传递给消费者时，消费者会自动或在应用程序开发人员选择时立即通知broker这样做。当使用消息确认时，broker只会在收到该消息（或消息组）的通知时从队列中完全删除该消息。

在某些情况下，例如，当消息无法确认路由（routed）时，消息可能会返回给发布者、丢弃，或者如果broker实现扩展，则将其放入所谓的“死信队列（dead letter queue）”。发布者通过使用某些参数发布消息来选择如何处理此类情况。

队列（Queues）、交换（exchanges ）和绑定（bindings）统称为 AMQP 实体。

##### 1.2.4AMQP 0-9-1 是一个可编程协议

AMQP 0-9-1 是一种可编程协议，因为 AMQP 0-9-1 实体和路由方案主要由应用程序本身定义，而不是broker管理员。因此，为声明队列和交换、定义它们之间的绑定、订阅队列（[queues](https://www.rabbitmq.com/queues.html)）等的协议操作做出了规定。

这为应用程序开发人员提供了很大的自由，但也要求他们了解潜在的定义冲突。在实践中，定义冲突很少见，通常表示配置错误。

应用程序声明它们需要的 AMQP 0-9-1 实体，定义必要的路由方案，并可以在不再使用 AMQP 0-9-1 实体时选择删除它们。

#### 1.3交换器（Exchanges）和交换器类型

交换器是发送消息的 AMQP 0-9-1 实体。交换器接收一条消息并将其路由（route）到零个或多个队列中。使用的路由算法取决于交换类型（*exchange type* ）和称为绑定（*bindings*）的规则。 AMQP 0-9-1 broker提供四种交换器类型：

| Exchange type（交换器类型）  | Default pre-declared names（默认预先声明的名称） |
| :--------------------------- | :----------------------------------------------- |
| Direct exchange（直接交换）  | (Empty string) and amq.direct                    |
| Fanout exchange（扇出交换）  | amq.fanout                                       |
| Topic exchange（主题交换）   | amq.topic                                        |
| Headers exchange（首部交换） | amq.match (and amq.headers in RabbitMQ           |

除了交换器类型之外，交换器还声明了许多属性，其中最重要的是：

- Name
- Durability (exchanges survive broker restart（持久化）)
- Auto-delete (当最后一个队列解除绑定时，交换被删除)
- Arguments (可选，由插件和特定于broker的功能使用)

交换器可以是持久的或短暂的。持久交换在broker重启后幸免于难，而临时交换则不然（他们必须在broker重新启动上线时重新声明）。并非所有场景和用例都要求交换是持久的。

##### 1.3.1默认交换器

默认交换器是没有broker预先声明名称（空字符串）的**直接交换器**。它有一个特殊的属性，使它对简单的应用程序非常有用：创建的每个队列都会自动绑定到一个与队列名称相同的路由键。

例如，当您声明一个名为“search-indexing-online”的队列时，AMQP 0-9-1 broker将使用“search-indexing-online”作为路由键（在此上下文有时称为绑定键）。因此，使用路由键“search-indexing-online”发布到默认交换器的消息将被路由到队列“search-indexing-online”。换句话说，默认交换器使得看起来可以将消息直接传递到队列，即使从技术上讲这不是正在发生的事情。

##### 1.3.2Direct exchange（直接交换）

直接交换根据消息路由键（routing key）将消息传递到队列。直接交换是消息单播路由的理想选择（尽管它们也可用于多播路由）。下面是它的工作原理：

- 一个队列使用路由键 K 绑定到交换器。
- 当具有路由键 R 的新消息到达直接交换时，如果 K = R，则交换将其路由到该队列。

直接交换通常用于以循环方式在多个工作人员（同一应用程序的实例）之间分配任务。这样做，重要的是要了解，在 AMQP 0-9-1 中，**消息在使用者之间而不是队列之间进行负载平衡**。

直接交换可以用图形表示如下：

![](http://rep.shaoteemo.com/exchange-direct.png)

##### 1.3.3Fanout exchange（扇出交换）

扇出交换将消息路由绑定到它的所有队列，并且忽略路由键（routing key）。如果 N 个队列绑定到一个扇出交换器，则当新消息发布到该交换器时，该消息的副本将传递到所有 N 个队列。扇出交换是消息广播路由的理想选择。

因为扇出交换向绑定到它的每个队列传递消息的副本，所以它的用例非常相似：

- 大型多人在线 (MMO) 游戏可将其用于排行榜更新或其他全局事件。
- 体育新闻网站可以使用扇出交换向移动客户端近乎实时地分发比分更新。
- 分布式系统可以广播各种状态和配置更新。
- 群聊可以使用扇出交换在参与者之间分发消息（虽然 AMQP 没有内置的在线概念，所以 XMPP 可能是更好的选择）。

扇出交换可以用图形表示如下：

![](http://rep.shaoteemo.com/exchange-fanout.png)

##### 1.3.4Topic exchange（主题交换）

主题交换根据消息路由键和用于将队列绑定到交换的模式之间的匹配将消息路由到一个或多个队列。主题交换类型通常用于实现各种发布/订阅（publish/subscribe）模式变体。主题交换通常用于消息的多播路由。

主题交换有非常广泛的用例。每当一个问题涉及多个消费者/应用程序（consumers/applications）有选择地选择他们想要接收的消息类型时，就应该考虑使用主题交换。

示例用途：

- 分发与特定地理位置相关的数据，例如销售点。
- 由多个工作人员完成的后台任务处理，每个工作人员都能够处理特定的任务集。
- 股票价格更新（以及其他类型财务数据的更新）。
- 涉及分类或标记的新闻更新（例如，仅针对特定运动或团队）。
- 云中不同类型服务的编排。
- 分布式 架构/特定于操作系统 的软件构建或打包，其中每个构建器只能处理一种架构或操作系统。

##### 1.3.5Headers exchange（首部交换）

首部交换设计用于在多个属性上进行路由，这些属性比路由键更容易表示为消息标头。首部交换忽略路由键属性。相反，用于路由的属性取自 headers 属性。如果 header 的值等于绑定时指定的值，则认为消息匹配。

可以使用多个首部进行匹配，将队列绑定到首部交换。在这种情况下，broker需要来自应用程序开发人员的另一条信息，即它是否应该考虑与任何首部匹配的消息，还是所有首部？这就是`x-match`绑定参数的用途。当`x-match`参数设置为`any`时，只有一个匹配的标头值就足够了。或者，将`x-match`设置为`all`要求所有值都必须匹配。

首部交换可以被视为“steroids的直接交换”。因为它们基于首部值进行路由，所以它们可以用作路由键不必是字符串的直接交换；例如，它可以是整数或散列（字典）。

请注意，以字符串`x-`开头的标头不会用于评估匹配项。

#### 1.4队列（Queues）

AMQP 0-9-1 模型中的队列（[Queues](https://www.rabbitmq.com/queues.html)）与其他消息和任务队列系统中的队列非常相似：它们存储应用程序使用的消息。队列与交换器共享一些属性，但也有一些额外的属性：

- Name
- Durable (the queue will survive a broker restart（持久化）)
- Exclusive (仅由一个连接使用，当该连接关闭时队列将被删除)
- Auto-delete (当最后一个消费者取消订阅时，删除至少有一个消费者的队列)
- Arguments (可选的;由插件和特定于broker的功能使用，例如消息 TTL、队列长度限制等)

在使用队列之前，必须先声明它。如果队列不存在，则声明将创建该队列。如果队列已经存在并且其属性与声明中的属性相同，则声明将不起作用。当现有队列属性与声明中的属性不同时，将引发代码为 406 (`PRECONDITION_FAILED`) 的通道级异常。

##### 1.4.1队列名称

 应用程序可以选择队列名称或要求broke）为它们生成一个名称。队列名称最多可以包含 255 个字节的 UTF-8 字符。AMQP 0-9-1 broker可以代表应用程序生成唯一的队列名称。要使用此功能，请将空字符串作为队列名称参数传递。生成的名称将与队列声明一起响应返回给客户端。

以`amq.`开头的队列名称。broker保留供内部使用。尝试使用违反此规则的名称声明队列将导致通道级异常，回复代码为 403 (`ACCESS_REFUSED`)。

##### 1.4.2队列持久性

在 AMQP 0-9-1 中，队列可以声明为持久的或临时的。持久队列的元数据存储在磁盘上，而临时队列的元数据尽可能存储在内存中。

发布时的消息也有同样的区别。[messages at publishing time](https://www.rabbitmq.com/publishers.html#message-properties)

在持久性很重要的环境和用例中，应用程序必须使用持久队列并确保发布或将发布的消息标记为持久化。

队列指南中更详细地介绍了该主题。[Queues guide](https://www.rabbitmq.com/queues.html#durability)

#### 1.5绑定（[Bindings](https://www.rabbitmq.com/tutorials/amqp-concepts.html#bindings)）

绑定是交换器使用（除其他外）将消息路由到队列的规则。为了指示交换器 E 将消息路由到队列 Q，Q 必须绑定到 E。绑定可能具有一些交换类型使用的可选路由键属性。路由键（routing key）的目的是选择某些发布到交换机的消息，以便路由到绑定的队列。换句话说，路由键就像一个过滤器。

打个比方：

- 队列就像你在纽约市的目的地
- 交换器就像肯尼迪机场
- 绑定是从肯尼迪机场到目的地的路线。可以有零种或多种方式到达

拥有绑定间接层可以实现使用直接发布到队列不可能或很难实现的路由方案，并且还消除了应用程序开发人员必须做的一定数量的重复工作。

如果一条消息不能路由到任何队列（例如，因为没有绑定它被发布到的交换器），[它要么被丢弃，要么返回给发布者](https://www.rabbitmq.com/publishers.html#unroutable)，这取决于发布者设置的消息属性。

#### 1.6消费者（Consumers）

除非应用程序可以[使用](https://www.rabbitmq.com/consumers.html)它们，否则将消息存储在队列中是没有用的。在 AMQP 0-9-1 模型中，应用程序有两种方法可以做到这一点：

- 订阅以消息传递给他们（“push API”）：这是推荐的选项
- 轮询（“pull API”）：这种方式非常低效，在大多数情况下应该避免

使用“push API”，应用程序必须表明要使用来自特定队列的消息。当他们这样做时，我们说他们注册了一个消费者，或者简单地说，订阅了一个队列。每个队列可能有多个消费者或注册一个独占消费者（在消费时从队列中排除所有其他消费者）。

每个消费者（订阅）都有一个称为消费者标签的标识符。它可用于取消订阅消息。消费者标签只是字符串。

##### 1.6.1消息确认

[消费者应用程序](https://www.rabbitmq.com/consumers.html)（即接收和处理消息的应用程序）可能偶尔无法处理单个消息或有时会崩溃。网络问题也有可能导致问题。这就提出了一个问题：broker何时应该从队列中删除消息？ AMQP 0-9-1 规范让消费者对此进行控制。有两种[确认模式](https://www.rabbitmq.com/confirms.html)：

- 在broker向应用程序发送消息之后（使用`basic.deliver`或`basic.get-ok`方法）。
- 在应用程序发回确认之后（使用`basic.ack`方法）。

前者称为自动确认模型，后者称为显式确认模型。使用显式模型，应用程序选择何时发送确认。它可以是在接收到消息之后，或者在处理之前将它持久化到数据存储之后，或者在完全处理消息之后（例如，成功获取一个网页，将其处理并存储到某个持久性数据存储中）。

如果消费者在没有发送确认的情况下宕机，则broker将其重新发送给另一个消费者，或者，如果当时没有可用的消费者，则broker将等到至少有一个消费者注册到同一队列，然后再尝试重新发送。

##### 1.6.2拒绝消息

当消费者应用程序收到一条消息时，对该消息的处理可能会成功，也可能失败。应用程序可以通过拒绝消息向broker指示消息处理失败（或当时无法完成）。拒绝消息时，应用程序可以要求broker丢弃或重新排队。当队列中只有一个消费者时，请确保您不会通过一遍又一遍地拒绝和重新排队来自同一消费者的消息来创建无限的消息传递循环。

##### 1.6.3否定确认消息

使用`basic.reject`方法拒绝消息。`basic.reject`有一个限制：无法像使用消息确认那样拒绝多条消息。但是，如果您使用的是 RabbitMQ，则有一个解决方案。RabbitMQ 提供了一个 AMQP 0-9-1 扩展，称为否定确认或 nacks。有关更多信息，请参阅[确认](https://www.rabbitmq.com/confirms.html)和[`basic.nack`](https://www.rabbitmq.com/nack.html) 扩展指南。

##### 1.6.4预取消息

对于多个消费者共享一个队列的情况，能够指定每个消费者在发送下一个消息确认之前可以一次发送多个消息很有用。这可以用作简单的负载均衡技术，或者如果消息倾向于批量发布，则可以提高吞吐量。例如，如果生产者应用程序由于其正在执行的工作的性质而每分钟发送一次消息。

请注意，RabbitMQ 仅支持通道级预取计数，不支持基于连接或大小的预取。

#### 1.7消息属性和有效负载

AMQP 0-9-1 模型中的消息具有属性。有些属性非常常见，以至于 AMQP 0-9-1 规范定义了它们，应用程序开发人员不必考虑确切的属性名称。举一些例子：

- Content type
- Content encoding
- Routing key
- Delivery mode (persistent or not)
- Message priority
- Message publishing timestamp
- Expiration period
- Publisher application id

AMQP brokers使用某些属性，但大多数属性都可以由接收它们的应用程序读取。一些属性是可选的，称为*headers*。它们类似于 HTTP 中的 X-Header。消息属性是在发布消息时设置的。

消息还有一个有效载荷（它们携带的数据），AMQP brokers将其视为一个加密的字节数组。broker不会检查或修改有效负载。消息可能只包含属性而没有负载。通常使用 JSON、Thrift、Protocol Buffers 和 MessagePack 等序列化格式来序列化结构化数据，以便将其作为消息负载发布。协议对等方通常使用`content-type`和`content-encoding`字段来传达此信息，但这仅是按照惯例。

消息可以作为持久性发布，这使得Broker将它们持久化到磁盘。如果服务器重新启动，系统会确保接收到的持久消息不会丢失。简单地将消息发布到持久交换器或者它被路由到的队列是持久的这一操作并不会使消息持久：这完全取决于消息本身的持久模式。将消息作为持久性发布会影响性能（就像数据存储一样，持久性以一定的性能成本为代价）。

可以在[Publishers指南](https://www.rabbitmq.com/publishers.html)中了解更多信息。

#### 1.8消息确认

由于网络不可靠且程序出错，因此通常需要某种处理确认消息机制。有时只需要确认已收到消息。有时确认意味着消息已由消费者验证和处理，例如，验证为具有强制性数据并持久保存到数据存储或索引。

这种情况很常见，因此 AMQP 0-9-1 具有消息确认（有时称为 acks）的内置功能，消费者使用它来确认消息传递和（或）处理。如果程序崩溃（当连接关闭时 AMQP Broker会监听到）， AMQP Broker未收到消息确认，则消息将重新入队（并可能立即传递给另一个已存在的消费者）。

在协议中内置消息确认有助于开发人员构建更强大的软件。

#### 1.9AMQP 0-9-1 方法

AMQP 0-9-1 被构造为许多方法。方法是一些列的操作（如 HTTP 方法），与面向对象编程语言中的方法没有任何共同之处。AMQP 0-9-1 中的协议方法被分组到类中。类只是 AMQP 方法的逻辑分组。 [AMQP 0-9-1 参考](https://www.rabbitmq.com/amqp-0-9-1-reference.html)包含所有 AMQP 方法的完整详细信息。

让我们来看看交换类，一组与交换操作相关的方法。它包括以下操作：

- `exchange.declare`
- `exchange.declare-ok`
- `exchange.delete`
- `exchange.delete-ok`

（请注意，RabbitMQ 站点参考还包括我们不会在本指南中讨论的交换类的 RabbitMQ 特定扩展）。

上述操作形成逻辑对：`exchange.declare` 和 `exchange.declare-ok`、`exchange.delete` 和 `exchange.delete-ok`。这些操作是“requests”（由客户端发送）和“responses”（由Brokers发送以响应上述“requests”）。

例如，客户端要求Broker使用 `exchange.declare` 方法声明一个新的交换器：

![](http://rep.shaoteemo.com/exchange-declare.png)

如上图所示，`exchange.declare` 带有几个参数。它们使客户端能够指定交换name、type、durable(持久性标志)等。

如果操作成功，broker 会响应`exchange.declare-ok`方法：

![](http://rep.shaoteemo.com/exchange-declare-ok.png)

`exchange.declare-ok` 不携带任何参数，除了通道号（通道将在本指南的后面介绍）。

AMQP 0-9-1 队列方法类上的一个方法对的事件序列也非常相似：`queue.declare` 和 `queue.declare-ok`：

![](http://rep.shaoteemo.com/queue-declare.png)

![](http://rep.shaoteemo.com/queue-declare-ok.png)

并非所有 AMQP 0-9-1 方法都有对应的方法。有些（`basic.publish` 是最广泛使用的）没有响应的“response”方法，而其他一些（例如`basic.get`）有不止一种可能的“response”。

#### 1.10连接（Connections）

AMQP 0-9-1 连接通常是处于长期连接状态。 AMQP 0-9-1 是一种应用级协议，它使用 TCP 进行可靠传输。连接使用身份验证并且可以使用 TLS 进行保护。当应用程序不再需要连接到服务器时，它应该优雅地关闭其 AMQP 0-9-1 连接，而不是突然关闭底层 TCP 连接。

#### 1.11通道（Channels）

有些应用程序需要多个连接到Broker。但是，同时保持许多 TCP 连接打开是不可取的，因为这样做会消耗系统资源并且使配置防火墙更加困难。AMQP 0-9-1 连接与可以被认为是“共享单个 TCP 连接的轻量级连接”的[通道channels](https://www.rabbitmq.com/channels.html)复用。

客户端执行的每个协议操作都发生在通道上。特定通道上的通信与另一个通道上的通信完全分开，因此每个协议方法还携带一个通道 ID（也称为通道号），这是一个整数，Broker 和客户端都使用它来确定该方法适用于哪个通道。

通道仅存在于连接的上下文中，而不会单独存在。当连接关闭时，其上的所有通道也关闭。

对于使用多个线程或进程进行处理的应用程序，为每个线程或进程打开一个新通道而不在它们之间共享通道是很常见的。

#### 1.12虚拟主机（Virtual Hosts）

为了让单个代理可以托管多个隔离的“环境”（用户组、交换器、队列等），AMQP 0-9-1 包含了[虚拟主机](https://www.rabbitmq.com/vhosts.html)（vhosts）的概念。它们类似于许多流行的 Web 服务器使用的虚拟主机，并提供 AMQP 实体所在的完全隔离的环境。协议客户端指定在连接协商期间他们想要使用的虚拟主机。

#### 1.13AMQP 是可扩展的

AMQP 0-9-1 有几个扩展点：

- [自定义交换类型(Custom exchange types)](https://www.rabbitmq.com/devtools.html#miscellaneous) 让开发人员实施开箱即用提供的交换类型不能很好覆盖的路由方案，例如，基于地理数据的路由。
- 交换器和队列的声明可以包括Broker可以使用的附加属性。 比如RabbitMQ中的 [per-queue message TTL](https://www.rabbitmq.com/ttl.html) 就是这样实现的。
- 特定于Broker的协议扩展。参见，例如， [RabbitMQ 实现的扩展](https://www.rabbitmq.com/extensions.html)。
- 可以引入[新的 AMQP 0-9-1 方法类](https://www.rabbitmq.com/amqp-0-9-1-quickref.html#class.confirm) 。
- Brokers 可以通过 [额外的插件](https://www.rabbitmq.com/plugins.html)进行扩展，例如， [RabbitMQ 管理](https://www.rabbitmq.com/management.html)前端和 HTTP API 作为插件实现。

#### 1.14AMQP 0-9-1 客户端生态系统

许多流行的编程语言和平台有[许多AMQP 0-9-1 客户端](https://www.rabbitmq.com/devtools.html)。其中一些严格遵循 AMQP 术语，仅提供 AMQP 方法的实现。其他一些具有附加功能、简便方法和抽象。一些客户端是异步的（非阻塞），一些是同步的（阻塞），一些同时支持这两种模型。某些客户端支持特定于供应商的扩展（例如，特定于 RabbitMQ 的扩展）。

由于 AMQP 的主要目标之一是互操作性，因此让开发人员了解协议操作而不将自己限制在特定客户端库的术语是一个好主意。通过这种方式与使用不同库的开发人员进行交流将变得更加容易。

#### 获得帮助和提供反馈、帮助我们改进文档 <3

[官方文档](https://www.rabbitmq.com/tutorials/amqp-concepts.html#amqp-clients)

### 

## 扩展知识

## 名词解释

### TLS

安全传输层协议（TLS）用于在两个通信应用程序之间提供保密性和数据完整性。

该协议由两层组成： TLS 记录协议（TLS Record）和 TLS 握手协议（TLS Handshake）。

