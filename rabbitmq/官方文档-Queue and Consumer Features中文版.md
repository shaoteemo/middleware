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

### 独占队列（Exclusive Queues）

独占队列只能由其声明的连接使用（使用、清除、删除等）。尝试使用来自不同连接的独占队列将导致通道级异常 `RESOURCE_LOCKED`，并显示一条错误消息，指出`cannot obtain exclusive access to locked queue`（无法获得对锁定队列的独占访问）。

独占队列在声明连接关闭或消失时被删除（例如，由于底层 TCP 连接丢失）。因此，它们仅适用于客户端特定的瞬态。

使独占队列以服务器命名是很常见的。

在 RabbitMQ 3.9 及以下版本中，独占队列受限于领导者位置选择过程（[leader location selection process](https://www.rabbitmq.com/ha.html#queue-leader-location)）。为确保它位于建立连接的同一集群节点上，请在声明队列时设置 `x-queue-master-locator="client-local"`。

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

## 队列和消息 TTL（Queue and Message TTL）

## 队列长度限制（Queue Length Limits）

## 延迟队列（Lazy Queues）

## 死信（Dead Lettering）

## 优先队列（Priority Queues）

## 消费者取消通知（Consumer Cancellation Notifications）

## 消费者预取（Consumer Prefetch）

## 消费者优先事项（Consumer Priorities）

## Streams