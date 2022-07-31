> 本文档翻译[官方文档](https://www.rabbitmq.com/documentation.html)的***队列和消费者特性（Queue and Consumer Features）***整章。其中包含如下主题：
>
> 1. [Queues guide](https://www.rabbitmq.com/queues.html)：队列指南
> 2. [Consumers guide](https://www.rabbitmq.com/consumers.html)：消费者指南
> 3. [Queue and Message TTL](https://www.rabbitmq.com/ttl.html)：队列和消息 TTL
> 4. [Queue Length Limits](https://www.rabbitmq.com/maxlength.html)：队列长度限制
> 5. [Lazy Queues](https://www.rabbitmq.com/lazy-queues.html)：延迟队列
> 6. [Dead Lettering](https://www.rabbitmq.com/dlx.html)：死信
> 7. [Priority Queues](https://www.rabbitmq.com/priority.html)：优先队列
> 8. [Consumer Cancellation Notifications](https://www.rabbitmq.com/consumer-cancel.html)：消费者取消通知
> 9. [Consumer Prefetch](https://www.rabbitmq.com/consumer-prefetch.html)：消费者预取
> 10. [Consumer Priorities](https://www.rabbitmq.com/consumer-priority.html)：消费者优先事项
> 11. [Streams](https://www.rabbitmq.com/streams.html)

# 《RabbitMQ-队列和消费者特性中文版》

## 队列指南（Queues guide）

### Queues

### 介绍

本指南概述了 RabbitMQ 中的队列。由于消息传递系统中的许多功能都与队列相关，因此它并不是一份详尽的指南，而是提供指向其他指南的链接的概述。

本指南主要涵盖 <u>AMQP 0-9-1</u><sup>已翻译《官方文档-AMQP 0-9-1 Model Explained中文版》</sup> 上下文中的队列，但是，大部分内容适用于其他支持的协议。

一些协议（例如 STOMP 和 MQTT）基于主题的思想。对他们来说，队列充当消费者的数据积累缓冲区。但是，了解队列所扮演的角色仍然很重要，因为许多功能仍然在队列级别运行，即使对于这些协议也是如此。

Streams 是 RabbitMQ 中可用的替代消息传递数据结构。Streams 提供与队列不同的功能。

### 基础知识

队列（[queue](https://en.wikipedia.org/wiki/Queue_(abstract_data_type))）是具有两个主要操作的顺序数据结构：一个项目可以在尾部**<u>入队</u><sup>ENQUEUED</sup>**（添加）并从头部**<u>出队</u><sup>DEQUEUED</sup>**（消耗）。队列在消息传递技术领域发挥着重要作用：许多消息传递协议和工具假设发布者（[publishers](https://www.rabbitmq.com/publishers.html)）和消费者（consumers）使用类似队列的存储机制进行通信。

RabbitMQ 中的队列是 [FIFO（“先进先出”）](https://en.wikipedia.org/wiki/FIFO_(computing_and_electronics))的。一些队列特征，即优先级和消费者[重新排队](https://www.rabbitmq.com/confirms.html)，会影响消费者观察到的排序。

### Names（名称）

队列具有名称，以便应用程序可以引用它们。

应用程序可以选择队列名称或要求代理（broker）为它们<u>生成一个名称</u><sup><服务器命名的队列></sup>。

以“amq”开头的队列名称。保留供Broker内部使用。尝试使用违反此规则的名称声明队列将导致通道级异常，回复代码为 403 (`ACCESS_REFUSED`)。

#### 服务器命名的队列

在 AMQP 0-9-1 中，broker 可以代表应用程序生成唯一的队列名称。要使用此功能，请传递一个空字符串作为队列名称参数：同一个通道中的后续方法可以通过使用期望队列名称的空字符串获得相同的生成名称。这是有效的，因为通道记住最后一个服务器生成的队列名称。

服务器命名的队列旨在用于本质上是瞬态且特定于特定使用者（应用程序实例）的状态。应用程序可以在消息元数据中共享这些名称，让其他应用程序响应它们（如[教程六](https://www.rabbitmq.com/getstarted.html)中所示）。否则，服务器命名队列的名称应该是已知的，并且只能由声明的应用程序实例使用。实例还应该为队列设置适当的绑定（路由 routing），以便发布者可以使用众所周知的交换器而不是直接使用服务器生成的队列名称。

### Properties（属性）

队列具有定义其行为方式的属性。有一组强制性属性和可选属性的映射：

- Name
- Durable (队列将在服务器重启后继续存在（持久性）)
- Exclusive (仅由一个连接使用，当该连接关闭时队列将被删除)
- Auto-delete (当最后一个消费者取消订阅时，删除至少有一个消费者的队列)
- Arguments (可选的;由插件和特定于代理的功能使用，例如消息 TTL、队列长度限制等)

请注意，并非所有属性组合在实践中都有意义。例如，自动删除和独占队列应该以<u>服务器命名</u><sup><服务器命名的队列></sup>。此类队列应该用于特定于客户端或特定于连接（会话）的数据。

当自动删除或独占队列使用众所周知的（静态）名称时，在客户端断开连接并立即重新连接的情况下，RabbitMQ 节点之间将存在自然竞争条件，这将删除此类队列并恢复将尝试重新声明它们的客户端。这可能会导致客户端连接恢复失败或异常，并造成不必要的混乱或影响应用程序可用性。

#### 声明和属性等同

在使用队列之前，必须先声明它。如果队列不存在，则声明队列将创建该队列。如果队列已经存在并且其属性与声明中的属性相同，则声明将不起作用。当现有队列属性与声明中的属性不同时，将引发代码为 406 (`PRECONDITION_FAILED`) 的通道级异常。

#### 可选参数

可选的队列参数，也被称为“x-arguments”，因为它们在 AMQP 0-9-1 协议中的字段名称，是任意 key/value 对的映射（字典），当队列被声明时可由客户端提供。

该地图由各种功能和插件使用，例如

- 队列类型（e.g. [quorum](https://www.rabbitmq.com/quorum-queues.html) or classic）
- 队列和消息 TTL<sup><在本文档中-队列和消息 TTL></sup>
- 队列长度限制<sup><在本文档中-队列长度限制></sup>
- 传统[经典队列镜像](https://www.rabbitmq.com/ha.html)设置
- 最大优先级<sup><在本文档中-优先队列></sup>
- [消费者优先事项](https://www.rabbitmq.com/consumer-priority.html)

等等。

大多数可选参数可以在队列声明后动态更改，但也有例外。例如，队列类型[queue type](https://www.rabbitmq.com/quorum-queues.html)（`x-queue-type`）和最大队列优先级（`x-max-priority`）必须在队列声明时设置，之后不能更改。

可选的队列参数可以通过几种方式设置：

- 使用策略（[policies](https://www.rabbitmq.com/parameters.html#policies)）的队列组（推荐）。
- 当客户端声明队列时，基于每个队列。

前一种选择更灵活、非侵入性，不需要修改和重新部署应用程序。因此，强烈推荐给大多数用户。请注意，某些可选参数（例如队列类型或最大优先级）只能由客户端提供，因为它们不能动态更改并且必须在声明时给出。

客户端提供可选参数的方式因客户端库而异，但通常是`durable`、`auto_delete` 和声明队列的函数（方法）的其他参数旁带的参数。

### 消息排序

RabbitMQ 中的队列是消息的有序集合。消息以先进先出（FIFO）的方式入队和出队（传递给消费者）。

优先级和[分片队列](https://github.com/rabbitmq/rabbitmq-sharding/)不能保证 FIFO 排序。

排序也会受到多个竞争消费者的存在、消费者优先级、消息重新传递的影响。这适用于任何类型的重新交付：在渠道关闭和消费者拒绝确认后自动交付。

应用程序可以假设在单个通道上发布的消息将按发布顺序排列在它们路由到的所有队列中。当发布发生在多个连接或通道上时，它们的消息序列将被并发路由和交错。

消费应用程序可以假设对单个消费者的初始交付（`redelivered`属性设置为`false`的那些）按照与入队相同的 FIFO 顺序执行。对于**重复交付**（`redelivered`属性设置为true`），原始订阅可能会受到消费者确认和重新交付时间的影响，因此无法保证。

在多个消费者的情况下，消息将按 FIFO 顺序出队交付，但实际交付将发生在多个消费者身上。如果所有的消费者都有相同的优先级，他们将在[循环（调度）的基础](https://en.wikipedia.org/wiki/Round-robin_scheduling)上被挑选出来。只有未超过其预取值（未确认交付数量）的通道上的消费者才会被考虑。

### 持久性（Durability）

队列可以是持久的或暂时的。持久队列的元数据存储在磁盘上，而临时队列的元数据尽可能存储在内存中。在某些协议中，[发布时的消息](https://www.rabbitmq.com/publishers.html#message-properties)也有相同的区别，例如AMQP 0-9-1 和 MQTT。

在持久性很重要的环境和用例中，应用程序必须使用持久队列并确保发布以及将发布的消息标记为持久化。

临时队列将在节点启动时被删除。因此，按照设计，它们将无法在节点重启后保存下来。临时队列中的消息也将被丢弃。

持久队列将在节点启动时恢复，包括其中发布为持久性的消息。发布为瞬态的消息将在恢复期间被**丢弃**，即使它们存储在持久队列中。

#### 如何选择

在大多数其他情况下，推荐使用持久队列。对于复制队列（replicated queues），唯一合理的选择是使用持久队列。

大多数情况下，队列的吞吐量和延迟不受队列是否持久的影响。只有具有非常高的队列或绑定流失的环境（即，队列每秒被删除和重新声明数百次或更多次）才会看到某些操作（即绑定）的延迟改进。因此，持久队列和瞬态队列之间的选择归结为用例的语义。

对于具有临时客户端的工作负载，临时队列可能是一个合理的选择，例如，用户界面中的临时 WebSocket 连接、移动应用程序和预计会脱机或使用交换机身份的设备。此类客户端通常具有固有的瞬态状态，应在客户端重新连接时替换该状态。

某些队列类型不支持临时队列。例如，由于底层复制协议的假设和要求，仲裁队列（[Quorum queues](https://www.rabbitmq.com/quorum-queues.html)）必须是持久的。

### 临时队列（Temporary Queues）

对于某些工作负载，队列应该是短暂的。虽然客户端可以在断开连接之前删除他们声明的队列，但这并不总是很方便。最重要的是，客户端连接可能会失败，可能会留下未使用的资源（队列）。

自动删除队列有以下三种方式：

- 独占队列（见下文）
- TTL（也在下面介绍）
- 自动删除（Auto-delete）队列

当最后一个消费者被取消（例如使用 AMQP 0-9-1 中的 `basic.cancel`）或消失（关闭通道或连接，或与服务器的 TCP 连接丢失）时，自动删除队列将被删除。

如果一个队列从来没有任何消费者，例如，当所有消费都使用 `basic.get` 方法（“pull”API）时，它不会被自动删除。对于这种情况，请使用独占队列或队列 TTL。

### 互斥队列（Exclusive Queues）

互斥队列只能由其声明的连接使用（使用、清除、删除等）。尝试使用来自不同连接的独占队列将导致通道级异常 `RESOURCE_LOCKED`，并显示一条错误消息，指出`cannot obtain exclusive access to locked queue`（无法获得对锁定队列的独占访问）。

互斥队列在声明连接关闭或消失时被删除（例如，由于底层 TCP 连接丢失）。因此，它们仅适用于客户端特定的瞬态。

使互斥队列以服务器命名是很常见的。

在 RabbitMQ 3.9 及以下版本中，互斥队列受限于领导者位置选择过程（[leader location selection process](https://www.rabbitmq.com/ha.html#queue-leader-location)）。为确保它位于建立连接的同一集群节点上，请在声明队列时设置 `x-queue-master-locator="client-local"`。

### 复制和分布式队列

队列可以复制到多个集群节点，并在松散耦合的节点或集群之间联合（[federated](https://www.rabbitmq.com/federated-queues.html)）。提供了两种复制队列类型：

- 仲裁队列
- 启用镜像（ [mirroring](https://www.rabbitmq.com/ha.html)）的经典队列

仲裁队列指南中介绍了它们之间的区别。仲裁队列是大多数工作负载和用例的推荐选项。

请注意，集群内复制和联合是正交特征（译著：两个操作互不依赖），不应被视为直接替代方案。

### 生存时间和长度限制

队列可以限制其长度。队列和消息可以有一个[TTL](https://www.rabbitmq.com/ttl.html)。

这两个功能都是可用于数据过期和限制队列最多可以使用多少资源（RAM、磁盘空间）的方式，例如当消费者离线或他们的吞吐量低于发布者时。

### 内存与持久存储

队列将消息保存在 RAM 和（或）磁盘上。在某些协议（例如 AMQP 0-9-1）中，这部分由客户端控制。在 AMQP 0-9-1 中，这是通过消息属性（`delivery_mode` 或在某些客户端中，`persistent`）完成的。

将消息发布为瞬态表明 RabbitMQ 应该在 RAM 中保留尽可能多的消息。然而，当队列发现自己处于[内存压力](https://www.rabbitmq.com/memory.html)下时，它们甚至会将临时消息分页到磁盘。

路由到持久队列的持久性消息会分批或经过一定时间（几分之一秒）后持久化。

<u>延迟队列</u><sup>已翻译 Lazy Queues</sup>更积极地将消息分页到磁盘，而不管它们的持久性如何。

有关详细信息，请参阅 [Memory Usage](https://www.rabbitmq.com/memory-use.html), [Alarms](https://www.rabbitmq.com/alarms.html) [Memory Alarms](https://www.rabbitmq.com/memory.html), [Free Disk Space Alarms](https://www.rabbitmq.com/disk-alarms.html), [Production Checklist](https://www.rabbitmq.com/production-checklist.html), and [Message Store Configuration](https://www.rabbitmq.com/persistence-conf.html)。

### 优先级

队列可以有 0 个或多个<u>优先级</u><sup>已翻译 Priority Queues</sup>。此功能是可选的：只有通过可选参数（见上文）配置了最大优先级数的队列才会进行优先级排序。

发布者使用消息属性中的`priority`字段指定消息优先级。

如果需要优先级队列，我们建议使用 1 到 10。目前使用更多的优先级会消耗更多的资源（Erlang 进程）。

### CPU 利用率和并行性注意事项

当前，单个队列副本（无论是领导者还是跟随者）仅限于其热代码路径上的单个 CPU 内核。因此，此设计假设大多数系统在实践中使用多个队列。单个队列通常被认为是一种反模式（不仅仅是出于资源利用的原因）。

如果需要为并行性（更好的 CPU 核心利用率）权衡消息排序，[rabbitmq-sharding](https://github.com/rabbitmq/rabbitmq-sharding/) 提供了一种对客户端透明的独特的方法。

### 指标和监控

RabbitMQ 收集有关队列的多个指标。它们中的大部分都可以通过 [RabbitMQ HTTP API 和管理 UI](https://www.rabbitmq.com/management.html) 获得，该 UI 专为监控而设计。这包括队列长度、进出率（吞吐率）、消费者数量、各种状态下的消息数量（例如准备交付或[未确认](https://www.rabbitmq.com/confirms.html)）、RAM 中的消息数量与磁盘上的消息数量等。

[rabbitmqctl](https://www.rabbitmq.com/man/rabbitmqctl.8.html) 可以列出队列和一些基本指标。

可以使用 [rabbitmq-top](https://github.com/rabbitmq/rabbitmq-top) 插件和管理 UI 中的各个队列页面访问 VM 调度程序使用情况、队列 (Erlang) 进程 GC 活动、队列进程使用的 RAM 量、队列进程消息长度等运行时指标。

### 消费者和消息确认

可以通过注册消费者（订阅）来消费消息，RabbitMQ 会将消息推送到客户端，或者为支持此功能的协议（例如 basic.get AMQP 0-9-1 方法）单独获取消息，类似于 HTTP GET。

一旦将传递写入连接套接字，消费者就可以显式或自动确认传递的消息。（[acknowledged by consumer](https://www.rabbitmq.com/confirms.html)）

自动确认模式通常会提供更高的吞吐率并使用更少的网络带宽。但是，它在[失败](https://www.rabbitmq.com/reliability.html)时提供的保证最少。根据经验，首先考虑使用手动确认模式。

### 消息预取和消费者过载

自动确认模式也可以压制消费者性能，他们无法像传递消息一样快速处理消息。这可能导致消费者进程的内存使用量和（或）操作系统交换永久增长。

手动确认模式提供了一种[设置未完成（未确认）交付数量限制的方法](https://www.rabbitmq.com/confirms.html)：通道 QoS（预取）。

使用更高（数千或更多）预取级别的消费者可能会遇到与使用自动确认的消费者相同的过载问题。

大量未确认的消息将导致服务器使用更高的内存。

#### 消息状态

因此，入队消息可以处于以下两种状态之一：

- 准备发送消息
- 已交付但尚未被[消费者确认](https://www.rabbitmq.com/confirms.html)

可以在管理 UI 中找到按状态细分的消息。

### 确定队列长度

可以通过多种方式确定队列长度：

- 对于 AMQP 0-9-1，使用 `queue.declare` 方法响应属性(`queue.declare-ok`) 。字段名称是 `message_count`。它的访问方式因客户端库而异。
- 使用 [RabbitMQ HTTP API](https://www.rabbitmq.com/management.html)。
- 使用 [rabbitmqctl](https://www.rabbitmq.com/man/rabbitmqctl.8.html) `list_queues` 命令。

队列长度定义为准备发送的消息数。

## 消费者指南（Consumers guide）

### Consumers

### 概述

本指南涵盖了与消费者相关的各种主题：

- 基础知识
- 消费者生命周期
- 如何注册消费者（订阅，“push API”）
- 确认模式
- 消息属性和传递元数据
- 如何使用预取限制未完成交付的数量
- 送达确认超时
- 消费者容量指标
- 如何取消消费者
- 消费者专有权
- 单一活跃消费者
- 消费者活动
- 消费者优先级
- 连接故障恢复
- 异常处理
- 并发考虑

及更多。

### 术语

术语“消费者”在不同的上下文中表示不同的事物。一般来说，在消息传递中，消费者是消费消息的应用程序（或应用程序实例）。 同一个应用程序还可以发布消息，从而同时成为发布者。

消息传递协议还具有持久订阅消息传递的概念。订阅是通常用于描述此类实体的一个术语。 消费者是另一个。 RabbitMQ 支持的消息传递协议使用这两个术语，但 RabbitMQ 文档倾向于使用后者。

从这个意义上说，消费者是消息传递的订阅，必须在传递开始之前注册并且可以被应用程序取消。

### 基础知识

RabbitMQ 是一个消息代理（broker。服务器）。 它接受来自发布者的消息并路由它们，如果有队列路由到，存储它们以供消费或立即交付给消费者（如果有的话）。

消费者从队列中消费。 为了消费消息，必须有一个队列。 当添加一个新的消费者时，假设队列中已经有消息准备好，交付将立即开始。

消费者注册时，目标队列可以为空。 在这种情况下，当新消息入队时，将发生第一次交付。

尝试从不存在的队列中消费将导致通道级异常，代码为`404 Not Found`并呈现它试图关闭的通道。

#### 消费者标签

每个使用者都有一个标识符，客户端库使用该标识符来确定要为给定交付调用哪个处理程序。它们的名称因协议而异。消费者标签和订阅 ID 是两个最常用的术语。 RabbitMQ 文档倾向于使用前者。

消费者标签也用于取消消费者。

#### 消费者生命周期

消费者应该是长期存在的：也就是说，在消费者的整个生命周期中，它都会收到多次交付。注册消费者以消费单个消息并不是最佳选择。

消费者通常在应用程序启动期间注册。只要他们的连接，甚至应用程序运行，他们通常就可以运行。

消费者可以更加动态地注册以响应系统事件，不再需要时取消订阅。这在通过 [Web STOMP](https://www.rabbitmq.com/web-stomp.html) 和 [Web MQTT](https://www.rabbitmq.com/web-mqtt.html) 插件、移动客户端等使用的 WebSocket 客户端中很常见。

#### 连接恢复

客户端可能会失去与 RabbitMQ 的连接。当检测到连接丢失时，消息传递停止。

一些客户端库提供涉及消费者恢复的自动连接恢复功能。[Java](https://www.rabbitmq.com/api-guide.html#recovery)、[.NET](https://www.rabbitmq.com/dotnet-api-guide.html#recovery)和[Bunny](http://rubybunny.info/articles/error_handling.html)是此类库的示例。虽然连接恢复不能覆盖 100% 的场景和工作负载，但它通常适用于消费应用程序，建议使用。

对于其他客户端库，应用程序开发人员负责执行连接恢复。通常以下恢复顺序效果很好：

- 恢复连接
- 恢复通道
- 恢复队列
- 恢复交换器
- 恢复绑定
- 恢复消费者

换句话说，消费者通常最后恢复，在他们的目标队列和那些队列的绑定到位之后。

### 注册消费者（订阅，“push API”）

RabbitMQ 可以向订阅应用程序推送入队消息（传递）。这是通过在队列上注册消费者（订阅）来完成的。完成订阅后，RabbitMQ 将开始传递消息。对于每次交付，将调用用户提供的处理程序。根据所使用的客户端库，这可以是用户提供的函数或符合特定接口的对象。

成功的订阅操作会返回订阅标识符（消费者标签）。它可以稍后用于取消消费者（即取消订阅）。

#### **Java Client**

查看 [Java client guide](https://www.rabbitmq.com/api-guide.html#consuming) 示例。

#### **.NET Client**

查看 [.NET client guide](https://www.rabbitmq.com/dotnet-api-guide.html#consuming) 示例。

#### 消息属性和传递元数据

每次传递都结合了消息元数据和传递信息。不同的客户端库使用稍微不同的方式来提供对这些属性的访问。通常，交付处理程序可以访问交付数据结构。

以下属性是交付和路由详细信息；它们本身不是消息属性，而是由 RabbitMQ 在路由和交付时设置的：

| 属性           | 类型               | 描述                                                                                          |
|--------------|------------------|---------------------------------------------------------------------------------------------|
| Delivery tag | Positive integer | 交付标识符，见 [Confirms](https://www.rabbitmq.com/confirms.html).                                 |
| Redelivered  | Boolean          | 如果此消息先前[已传递并重新排队](https://www.rabbitmq.com/confirms.html#consumer-nacks-requeue)，则设置为“true” |
| Exchange     | String           | 路由此消息的交换器                                                                                   |
| Routing key  | String           | 发布者使用的路由key                                                                                 |
| Consumer tag | String           | 消费者（订阅）标识符                                                                                  |

以下是消息属性。其中大部分是可选的。它们由发布者在消息发布时设置：

| 属性               | 类型                  | 描述                                                                      | 是否必须 |
|------------------|---------------------|-------------------------------------------------------------------------|------|
| Delivery mode    | Enum (1 or 2)       | 2 代表“persistent”，1 代表“transient”。一些客户端库将此属性公开为布尔值或枚举。                   | Yes  |
| Type             | String              | 特定于应用程序的消息类型，例如“orders.created”                                         | No   |
| Headers          | Map (string => any) | 带有字符串标题名称的标题的任意映射                                                       | No   |
| Content type     | String              | 内容类型, e.g. "application/json"。 由应用程序使用，而不是核心 RabbitMQ                   | No   |
| Content encoding | String              | 内容编码, e.g. "gzip"。由应用程序使用，而不是核心 RabbitMQ                                | No   |
| Message ID       | String              | 任意消息 ID                                                                 | No   |
| Correlation ID   | String              | 帮助将请求与响应相关联, see [tutorial 6](https://www.rabbitmq.com/getstarted.html) | No   |
| Reply To         | String              | 携带响应队列名称， see [tutorial 6](https://www.rabbitmq.com/getstarted.html)    | No   |
| Expiration       | String              | [每条消息的 TTL](https://www.rabbitmq.com/ttl.html)                          | No   |
| Timestamp        | Timestamp           | 应用程序提供的时间戳                                                              | No   |
| User ID          | String              | 用户 ID，如果设置则[验证](https://www.rabbitmq.com/validated-user-id.html)        | No   |
| App ID           | String              | 应用名称                                                                    | No   |

#### 消息类型

消息的类型属性是一个任意字符串，可帮助应用程序传达消息的类型。它由发布者在消息发布时设置。该值可以是发布者和消费者同意的任何特定于域的字符串。

RabbitMQ 不验证或使用此字段，它用于供应用程序和插件使用和解释。

实践中的消息类型自然分为组，点分隔的命名约定是常见的（但 RabbitMQ 或客户端不需要），例如`orders.created` 或 `logs.line` 或 `profiles.image.changed`。

如果消费者收到未知类型的交付，强烈建议记录此类事件，以便更轻松地进行故障排除。

#### Content Type and Encoding

内容（MIME 媒体）类型（Content Type）和内容编码（Encoding）字段允许发布者传达消费者应如何反序列化和解码消息有效负载。

RabbitMQ 不验证或使用这些字段，它用于供应用程序和插件使用和解释。

例如，带有 JSON 负载的消息[应该使用](http://www.ietf.org/rfc/rfc4627.txt)`application/json`。如果负载是使用 LZ77 (GZip) 算法压缩的，则其内容编码应为 `gzip`。

可以通过用逗号分隔来指定多种编码。

### 确认模式

注册消费者应用程序时可以选择两种交付模式之一：

- Automatic（自动交付不需要确认，又名“即发即忘”）
- Manual（手动交付需要客户确认）

消费者确认是一个[单独的文档指南](https://www.rabbitmq.com/confirms.html)的主题，连同发布确认，发布者的一个密切相关的概念。

### 使用预取限制同时传送

使用手动确认模式，消费者有一种方法可以限制“进行中”（通过网络传输或已交付但未确认）的交付数量。这可以避免消费者过载。

此功能以及消费者确认是[单独文档指南的](https://www.rabbitmq.com/confirms.html)主题。

### 消费者容量指标

RabbitMQ [管理 UI](https://www.rabbitmq.com/management.html) 以及[监控数据](https://www.rabbitmq.com/monitoring.html)端点（例如 [Prometheus 抓取](https://www.rabbitmq.com/prometheus.html)的数据端点）显示了一个称为单个队列的消费者容量（以前称为消费者利用率）的指标。

该指标计算为队列能够立即将消息传递给消费者的时间的一小部分。它有助于操作员注意可能值得向队列添加更多消费者（应用程序实例）的情况。

如果此数字小于 100%，则在以下情况下，队列领导副本可能能够更快地传递消息：

- 有更多的消费者或
- 消费者花费更少的时间处理交付或
- 消费者渠道使用了更高的预取值（见上一小节）

对于没有消费者的队列，消费者容量将为 0%。对于有在线消费者但没有消息流的队列，该值将是 100%：这个想法是任何数量的消费者都可以维持这种交付率。

请注意，消费者容量只是一个提示。消费者应用程序可以而且应该收集有关其操作的更具体的指标，以帮助调整大小和任何可能的容量变化。

#### **Java Client**

查看 [Java client guide](https://www.rabbitmq.com/api-guide.html#consuming) 示例。

#### **.NET Client**

查看 [.NET client guide](https://www.rabbitmq.com/dotnet-api-guide.html#consuming) 示例。

### 获取单个消息（“Pull API”）

使用 AMQP 0-9-1 可以使用`basic.get`协议方法一一获取消息。按 FIFO 顺序获取消息。可以使用自动或手动确认，就像消费者（订阅）一样。

非常不推荐一条一条地获取消息，因为与常规的长期消费者相比，它的效率非常低。与任何基于轮询的算法一样，在消息发布是零星的并且队列可以长时间保持为空的系统中，这将是非常浪费的。

如有疑问，更推荐使用普通的长寿命消费者。

#### **Java Client**

查看 [Java client guide](https://www.rabbitmq.com/api-guide.html#consuming) 示例。

#### **.NET Client**

查看 [.NET client guide](https://www.rabbitmq.com/dotnet-api-guide.html#consuming) 示例。

### 送达确认超时

在现代 RabbitMQ 版本中，对消费者交付确认强制执行超时。这有助于检测从不确认交付的错误（卡住）消费者。此类消费者可能会影响节点的磁盘数据压缩，并可能使节点耗尽磁盘空间。

如果消费者未确认其交付超过超时值（默认为 30 分钟），则其通道将关闭，并显示 `PRECONDITION_FAILED` 通道异常。该错误将由消费者连接到的节点[记录](https://www.rabbitmq.com/logging.html)。

超时值可在 [`rabbitmq.conf`] 中配置（以毫秒为单位）：

```ini
# 30 minutes in milliseconds
consumer_timeout = 1800000
```

```ini
# one hour in milliseconds
consumer_timeout = 3600000
```

可以使用`advanced.config`禁用超时。这是非常不推荐的：

```erlang
%% advanced.config
[
  {rabbit, [
    {consumer_timeout, undefined}
  ]}
].
```

与其完全禁用超时，不如考虑使用较高的值（例如，几个小时）。

### 互斥性

当使用 AMQP 0-9-1 客户端注册消费者时，可以将[exclusive](https://www.rabbitmq.com/amqp-0-9-1-reference.html#basic.consume)标志设置为 true 以请求消费者成为目标队列中唯一的消费者。仅当当时没有消费者已注册到队列时，调用才会成功。这允许确保一次只有一个消费者从队列中消费。

如果独占消费者被取消或宕机，则应用程序负责注册一个新消费者以继续从队列中消费。

如果需要独占消费和消费连续性，单一活跃消费者可能更合适。

### 单一活跃消费者

单个活动消费者允许在一个时间消耗队列中只有一个消费者，并在活动消费者被取消或宕机的情况下故障转移到另一个注册消费者。当消息必须按照消息到达队列的相同顺序被消费和处理时，仅使用一个消费者是很有用的。

一个典型的事件序列如下：

- 一个队列被声明，一些消费者几乎同时注册到它。
- 第一个注册的消费者成为唯一的活动消费者：消息被分派给它，其他消费者被忽略。
- 由于某种原因，单个活跃的消费者被取消或直接宕机。注册的消费者之一成为新的单一活动消费者，现在将消息分派给它。换句话说，队列自动故障转移到另一个消费者。

请注意，如果没有启用单个活动消费者功能，消息将使用循环法分发给所有消费者。

声明队列时可以启用单个活动消费者，并将 `x-single-active-consumer` 参数设置为 `true`，例如使用 Java 客户端：

```java
Channel ch = ...;
Map<String, Object> arguments = new HashMap<String, Object>();
arguments.put("x-single-active-consumer", true);
ch.queueDeclare("my-queue", false, false, false, arguments);
```

与AMQP互斥性<sup>见本文《互斥性》</sup>消费者相比，单一活跃消费者对应用端保持消费连续性的压力更小。消费者只需要注册并自动处理故障转移，无需检测活动消费者故障并注册新消费者。

 [management UI](https://www.rabbitmq.com/management.html)  和 [CLI](https://www.rabbitmq.com/rabbitmqctl.8.html) 可以报告<sup>见本文《消费者活动》</sup>哪个消费者是启用该功能的队列中的当前活动消费者。

请注意以下有关单个活跃消费者的信息：

- 对所选的活跃消费者没有保证，它是随机选择的，即使消费者优先级<sup>见本文《优先级》</sup>正在使用中。
- 如果在队列上启用了单个活动消费者，则尝试注册一个将独占消费标志设置为 true 的消费者将导致错误（不可以即启用single-active-consumer又启用exclusive consumer）。
- 消息总是传递给活跃的消费者，即使它在某个时候太忙了。当使用手动确认和 `basic.qos` 时，可能会发生这种情况，消费者可能忙于处理它使用 `basic.qos` 请求的最大数量的未确认消息。在这种情况下，其他消费者将被忽略并且消息被排队。
- 不可能通过[策略](https://www.rabbitmq.com/parameters.html#policies)启用单个活动消费者。这是原因。RabbitMQ 中的策略本质上是动态的，它们可以来来去去，启用和禁用它们声明的功能。想象一下突然禁用队列中的单个活动消费者：broker将开始向非活动消费者发送消息，并且消息将被并行处理，这与单个活动消费者试图实现的完全相反。由于单个活动消费者的语义不能很好地与策略的动态特性配合使用，因此只能在使用队列参数声明队列时启用此功能。

### 消费者活动

[management UI](https://www.rabbitmq.com/management.html)  和 `list_consumers`  [CLI](https://www.rabbitmq.com/rabbitmqctl.8.html#list_consumers) 命令为消费者报告`active`标志。此标志的值取决于几个参数。

- 对于经典队列，当未启用单一活跃消费者时，该标志始终为`true`。
- 对于仲裁队列，当单个活动消费者未启用时，默认情况下该标志为`true`，如果消费者所连接的节点被怀疑已关闭，则该标志设置为`false`。
- 如果启用单个活动消费者，则该标志仅针对当前单个活动消费者设置为`true`，如果活动消费者消失，队列中的其他消费者正在等待提升，因此他们的活动设置为`false`。

### 优先事项

通常，连接到队列的活动消费者以循环(round-robin)方式从队列接收消息。

消费者优先级允许您确保高优先级消费者在活动时接收消息，只有当高优先级消费者被阻止时，消息才会发送到低优先级消费者，例如通过有效的预取设置<sup>见本文《使用预取限制同时传送》</sup>。

当使用消费者优先级时，如果存在多个具有相同高优先级的活动消费者，则消息将循环传递。

消费者优先事项包含在[单独的指南中](https://www.rabbitmq.com/consumer-priority.html)<sup>在本文当《Consumer Priorities》</sup>。

### 异常处理

消费者应处理在处理交付或任何其他消费者操作期间出现的任何异常。应该记录、收集和忽略此类异常。

如果消费者由于依赖项不可用或类似原因而无法处理交付，它应该清楚地记录下来并取消本身，直到它能够再次处理交付。这将使消费者的不可用状态对 RabbitMQ 和[监控系统](https://www.rabbitmq.com/monitoring.html)可见。

### 并发注意事项

消费者并发主要是客户端库实现细节和应用程序配置的问题。对于大多数客户端库（例如 Java、.NET、Go、Erlang），交付被分派到处理所有异步消费者操作的线程池（或类似的）。池通常具有可控的并发度。

Java 和 .NET 客户端保证，无论并发程度如何，单个通道上的交付都将按照收到的相同顺序进行分派。请注意，一旦分派，并发处理交付将导致执行处理的线程之间的自然竞争条件。

某些客户端（例如 Bunny）和框架可能会选择将消费者调度池限制为单个线程（或类似线程），以避免在并发处理交付时出现自然竞争条件。一些应用程序依赖于严格的交付顺序处理，因此必须使用并发因子为 1 或在自己的代码中处理同步。可以并发处理交付的应用程序，可以使用最多可用于它们的内核数量的并发度。

#### 队列并行性注意事项

单个 RabbitMQ 队列绑定到单个核心。使用多个队列来提高节点上的 CPU 利用率。分片和一致性哈希交换等插件有助于提高并行度。---见队列指南

## 队列和消息 TTL（Queue and Message TTL）

### Time-To-Live and Expiration

#### 概述

RabbitMQ 允许您为消息和队列设置 TTL（生存时间）。这由可选的队列<sup>见队列指南部分</sup>参数控制，最好使用[策略](https://www.rabbitmq.com/parameters.html)来完成。

消息 TTL 可以应用于单个队列、一组队列或逐条消息应用。TTL 设置也可以由[操作员策略](https://www.rabbitmq.com/parameters.html#operator-policies)强制执行。

#### 队列中的每条队列消息 TTL

可以通过使用策略设置`message-ttl`参数或在队列声明时指定相同参数来为给定队列设置消息 TTL。

如果消息在队列中的停留时间超过配置的 TTL，则称该消息*dead*。请注意，路由到多个队列的消息可能会在不同的时间消失，或者根本不会在它所在的每个队列中消失。一个队列中消息的死亡不会影响其他队列中同一消息的生命周期。

服务器保证死消息不会使用`basic.deliver`传递（给消费者）或包含在`basic.get-ok`响应中（用于一次性获取操作）。此外，服务器将尝试在基于 TTL 的到期时或之后不久删除消息。

TTL 参数或策略的值必须是非负整数 (0 <= n)，以毫秒为单位描述 TTL 周期。因此，值 1000 表示添加到队列的消息将在队列中存在 1 秒或直到它被传递给消费者。参数可以是 AMQP 0-9-1 类型的`short-short-int`、`short-int`、`long-int` 或 `long-long-int`。

#### 使用策略为队列定义消息 TTL

要使用策略指定 TTL，请将key“message-ttl”添加到策略定义中：

| rabbitmqctl               | `rabbitmqctl set_policy TTL ".*" '{"message-ttl":60000}' --apply-to queues` |
| :------------------------ | ------------------------------------------------------------ |
| **rabbitmqctl (Windows)** | **`rabbitmqctl set_policy TTL ".*" "{""message-ttl"":60000}" --apply-to queues`** |

这会将 60 秒的 TTL 应用于所有队列。

#### 在声明期间使用x-arguments为队列定义消息 TTL

这个 Java 示例创建了一个队列，其中消息最多可驻留 60 秒：

```java
Map<String, Object> args = new HashMap<String, Object>();
args.put("x-message-ttl", 60000);
channel.queueDeclare("myqueue", false, false, false, args);
```

C# 中的相同示例：

```C#
var args = new Dictionary<string, object>();
args.Add("x-message-ttl", 60000);
model.QueueDeclare("myqueue", false, false, false, args);
```

可以将消息 TTL 策略应用于其中已经有消息的队列，但这涉及一些警告<sup>见下面的内容</sup>。

如果消息被重新排队（例如由于使用具有重新排队参数的 AMQP 方法，或由于通道关闭），则会保留消息的原始到期时间。

将 TTL 设置为 0 会导致消息在到达队列时过期，除非它们可以立即传递给消费者。因此，这为 RabbitMQ 服务器不支持的立即发布标志`immediate`提供了替代方案。与该标志不同，没有发出`basic.returns`，如果设置了死信交换，则消息将是死信的。

### 发布者中的每条消息 TTL

通过在发布消息时设置过期属性，可以在每条消息的基础上指定 TTL。

`expiration`字段的值以毫秒为单位描述 TTL 周期。适用与`x-message-ttl`相同的约束。由于`expiration`字段必须是字符串，因此MQ服务器（broker）将（仅）接受数字的字符串表示形式。

当同时指定每个队列和每条消息的 TTL 时，将选择两者之间的较低值。

此示例使用 RabbitMQ Java 客户端发布消息，该消息最多可在队列中驻留 60 秒：

```java
byte[] messageBodyBytes = "Hello, world!".getBytes();
AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                                   .expiration("60000")
                                   .build();
channel.basicPublish("my-exchange", "routing-key", properties, messageBodyBytes);
```

C# 中的相同示例：

```c#
byte[] messageBodyBytes = System.Text.Encoding.UTF8.GetBytes("Hello, world!");

IBasicProperties props = model.CreateBasicProperties();
props.ContentType = "text/plain";
props.DeliveryMode = 2;
props.Expiration = "60000";

model.BasicPublish(exchangeName,
                   routingKey, props,
                   messageBodyBytes);
```

### 注意事项

当特定事件发生时，追溯应用每条消息 TTL 的队列（当它们已经有消息时）将丢弃这些消息。只有当过期消息到达队列的首条时，它们才会真正被丢弃（或死信）。消费者不会收到已过期的消息传递给他们。请记住，消息过期和消费者交付之间可能存在自然竞争条件，例如：消息可以在写入套接字之后但在到达消费者之前过期。

设置每条消息的 TTL 过期消息时，可以在未过期消息后面排队，直到后者被消耗或过期。因此，此类过期消息使用的资源不会被释放，它们将被计入队列统计信息（例如：队列中的消息数）。

在追溯应用每条消息的 TTL 策略时，建议让消费者在线以确保更快地丢弃消息。

鉴于现有队列上的每个消息 TTL 设置的这种行为，当需要删除消息以释放资源时，应该使用队列 TTL（或队列清除或队列删除）。

### 队列 TTL

TTL 也可以在队列上设置，而不仅仅是队列内容。队列只有在不使用时（例如：没有消费者）才会在一段时间后过期。此功能可以与自动删除队列属性<sup>见队列指南</sup>一起使用。

可以通过将`x-expires`参数设置为`queue.declare`或通过设置`expires`策略来为给定队列设置到期时间。这控制了一个队列在被自动删除之前可以被使用多长时间。未使用表示队列没有消费者，队列最近没有被重新声明（重新声明更新租约），并且至少在到期期间没有调用`basic.get`。例如，这可以用于 RPC 样式的回复队列，其中可以创建许多可能永远不会耗尽的队列。

服务器保证队列将被删除，如果至少在到期期间未使用。不保证在过期期限过后多久移除队列。当服务器重新启动时，持久队列的租约会重新启动。

`x-expires`参数或`expires`策略的值以毫秒为单位描述过期时间。它必须是一个正整数（与消息 TTL 不同，它不能为 0）。因此，值为 1000 表示将删除 1 秒内未使用的队列。

#### 使用策略为队列定义队列 TTL

以下策略使所有队列在自上次使用后 30 分钟后过期：

| rabbitmqctl               | `rabbitmqctl set_policy expiry ".*" '{"expires":1800000}' --apply-to queues` |
| :------------------------ | ------------------------------------------------------------ |
| **rabbitmqctl (Windows)** | **`rabbitmqctl.bat set_policy expiry ".*" "{""expires"":1800000}" --apply-to queues`** |

#### 在声明期间使用 x-arguments 为队列定义队列 TTL

Java 中的这个示例创建了一个队列，该队列在 30 分钟未使用后过期。

```java
Map<String, Object> args = new HashMap<String, Object>();
args.put("x-expires", 1800000);
channel.queueDeclare("myqueue", false, false, false, args);
```

## 队列长度限制（Queue Length Limits）

## 延迟队列（Lazy Queues）

## 死信（Dead Lettering）

## 优先队列（Priority Queues）

## 消费者取消通知（Consumer Cancellation Notifications）

## 消费者预取（Consumer Prefetch）

## 消费者优先事项（Consumer Priorities）

## Streams