本文档翻译官方文档：http://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html

<h1>ZooKeeper 程序员指南</h1>

<h2>开发使用 ZooKeeper 的分布式应用程序</h2>

# 介绍

本文档是为希望创建利用 ZooKeeper 协调服务的分布式应用程序的开发人员提供的指南。它包含概念和实用信息。

本指南的前四部分对各种 ZooKeeper 概念进行了更高层次的讨论。这些对于理解 ZooKeeper 如何工作以及如何使用它都是必要的。它不包含源代码，但它假定您熟悉与分布式计算相关的问题。第一组中的部分是：

- The ZooKeeper Data Model（Zookeeper数据模型）
- ZooKeeper Sessions（ZooKeeper 会话）
- ZooKeeper Watches（Zookeeper事件处理）
- Consistency Guarantees（一致性）

接下来的四节提供了实用的编程信息。这些是：

- Building Blocks: A Guide to ZooKeeper Operations （ZooKeeper 操作指南）
- Bindings（绑定）
- Gotchas: Common Problems and Troubleshooting（常见问题和故障排除）

本书最后附有一个附录，其中包含指向其他有用的 ZooKeeper 相关信息的链接。

本文档中的大部分信息都可以作为独立的参考资料进行访问。但是，在开始您的第一个 ZooKeeper 应用程序之前，您可能至少应该阅读有关 ZooKeeper 数据模型和 ZooKeeper 基本操作的章节。

# ZooKeeper 数据模型

ZooKeeper 有一个分层的命名空间，很像一个**分布式文件系统**。唯一的区别是命名空间中的每个节点都可以拥有与其关联的数据以及子节点。这就像拥有一个允许文件也成为目录的文件系统。节点的路径总是表示为规范的、绝对的、斜线分隔的路径；没有相对参考。任何 unicode 字符都可以在受以下约束的路径中使用：

- 空字符 (\u0000) 不能是路径名的一部分。 （这会导致 C 绑定出现问题。）
- 不能使用 \u0001 - \u001F 、 \u007F 、\u009F字符，因为它们不能很好地显示或以令人困惑的方式呈现。
- 不允许使用\ud800 - uF8FF、\uFFF0 - uFFFF这些字符：。
- ”.”字符可以用作另一个名称的一部分，但“.”和“..”不能单独用于指示沿路径的节点，因为 **ZooKeeper 不使用相对路径**。以下内容无效：“/a/b/./c”或“/a/b/../c”。
- 标记“zookeeper”是保留字。

## ZNodes

ZooKeeper 树中的每个节点都称为 znode。Znodes 维护一个 stat 结构，其中包括数据更改、acl 更改的版本号。stat 结构也有时间戳。版本号与时间戳一起允许 ZooKeeper 验证缓存并协调更新。每次 znode 的数据更改时，版本号都会增加。例如，每当客户端检索数据时，它也会收到数据的版本。当客户端执行更新或删除时，它必须提供它正在更改的 znode 数据的版本。如果它提供的版本与数据的实际版本不匹配，则更新将失败。（ 此行为可以被覆盖。）

> 在分布式应用工程中，节点这个词可以指通用主机、服务器、集成的成员、客户端进程等。在 ZooKeeper 文档中，znodes 指的是数据节点。服务器是指组成 ZooKeeper 服务的机器；仲裁（投票）对等点是指组成集合的服务器；客户端是指任何使用 ZooKeeper 服务的主机或进程。

Znodes 是程序员访问的主要实体。它们有几个值得一提的特性。

### Watches

客户端可以在 znode 上设置监视。对该 znode 的更改会触发watch ，然后再清除watch 。当 watch 触发时，ZooKeeper 会向客户端发送通知。关于Watches的更多信息可以在 ZooKeeper Watches 部分找到。

### Data Access（数据访问）

存储在命名空间中每个 znode 的数据读写具有原子性。读取与 znode 关联的所有数据字节，写入替换所有数据。每个节点都有一个访问控制列表 (ACL)，它限制了谁可以做什么。

ZooKeeper 并非为通用数据库或大型对象存储设计。相反，它管理协调数据。这些数据可以以配置信息、状态信息、集合点等形式出现。各种形式的协调数据的一个共同属性是它们相对较小：以千字节为单位。ZooKeeper 客户端和服务器实现有健全性检查，以确保 **znodes 的数据少于 1M**，但数据应该比平均少得多。对相对较大的数据大小进行操作会导致某些操作比其他操作花费更多的时间，并且会影响某些操作的延迟，因为在网络上和存储介质上移动更多数据需要额外的时间。如果需要大数据存储，通常处理此类数据的模式是将其存储在大容量存储系统上，例如 NFS 或 HDFS，并在 ZooKeeper 中存储指向存储位置的指针。

### Ephemeral Nodes（临时节点）

ZooKeeper 也有临时节点的概念。只要创建 znode 的会话处于活动状态（注：即不断开客户端），这些 znode 就存在。当会话结束时，znode 被删除。由于这种行为，**临时 znode 不允许有子节点**。可以使用 `getEphemerals()`api 检索会话的临时列表。

getEphemerals()

检索会话为给定路径创建的临时节点列表。如果路径为空，它将列出会话的所有临时节点。用例：一个示例可能是，如果需要收集会话的临时节点列表以进行重复数据输入检查，并且节点是按顺序创建的，因此您不知道重复检查的名称。在这种情况下， `getEphemerals()` api 可用于获取会话的节点列表。这可能是服务发现的典型用例。

### Sequence Nodes -- Unique Naming（序列节点）

创建 znode 时，您还可以请求 ZooKeeper 在路径末尾附加一个单调递增的计数器。此计数器对于父 znode节点是唯一的。计数器的格式为 %010d——即 10 位数字，带有 0（零）填充（计数器以这种方式格式化以简化排序），即“0000000001”。有关此功能的示例使用，请参阅队列 [Queue Recipe](http://zookeeper.apache.org/doc/r3.7.0/recipes.html#sc_recipes_Queues)。注意：用于存储下一个序列号的计数器是一个由父节点维护的有符号整数（4bytes），计数器在增加超过 2147483647 时会溢出（导致名称“-2147483648”）。

### Container Nodes（容器节点）

3.6.0版本中添加

ZooKeeper 有容器 znode 的概念。容器 znode 是特殊用途的 znode，可用于领导者、锁定等。当容器的最后一个子容器被删除时，该容器将成为服务器在未来某个时间点删除的候选对象。

鉴于此属性，您应该准备好获取 KeeperException。在容器 znode 内创建子节点时出现 NoNodeException。 在容器 znode 内创建子 znode 时，始终检查 KeeperException.NoNodeException 并在发生时重新创建容器 znode。

### TTL Nodes（TTL 节点）

3.6.0版本中添加

创建 PERSISTENT 或 PERSISTENT_SEQUENTIAL znode 时，您可以选择为 znode 设置以毫秒为单位的 TTL。如果在 TTL 内没有修改 znode 并且没有子节点，它将成为服务器在未来某个时间点删除的候选节点。

注意：TTL 节点必须通过系统属性启用，因为默认情况下它们是禁用的。有关详细信息，请参阅管理员指南[Administrator's Guide](http://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#sc_configuration)。如果您尝试在没有正确设置系统属性的情况下创建 TTL 节点，服务器将抛出 KeeperException.UnimplementedException。

## ZooKeeper 中的时间

ZooKeeper 以多种方式跟踪时间：

- **Zxid** ZooKeeper 状态的每次更改都会收到一个 zxid（ZooKeeper 事务 ID）形式的标记。这将向 ZooKeeper 公开所有更改的总顺序。每次更改都会有一个唯一的 zxid，如果 zxid1 小于 zxid2，则 zxid1 发生在 zxid2 之前。
- **Version numbers** 对节点的每次更改都会导致该节点的版本号之一增加。三个版本号分别是 version/dataversion（znode 数据更改的次数）、cversion（znode 子节点的更改次数）和 aversion/aclVersion（znode 的 ACL 的更改次数）。
- **Ticks** 使用集群 ZooKeeper 服务器时，服务器使用滴答来定义事件的时间，例如状态上传、会话超时、对等点之间的连接超时等。滴答时间仅通过最小会话超时（滴答时间的 2 倍）间接暴露；如果客户端请求的会话超时小于最小会话超时，服务器会告诉客户端会话超，时实际上是最小会话超时。
- **Real time** 除了在 znode 创建和 znode 修改时将时间戳放入 stat 结构之外，ZooKeeper 根本不使用实时或时钟时间。

## ZooKeeper Stat 结构

ZooKeeper 中每个 znode 的 Stat 结构由以下字段组成：

|          Field          |                         Description                          |
| :---------------------: | :----------------------------------------------------------: |
|        **czxid**        |                    创建此 znode 的 zxid。                    |
|        **mzxid**        |                  上次修改此 znode 的 zxid。                  |
|        **pzxid**        |               上次修改此 znode 子节点的 zxid。               |
|        **ctime**        |            创建此 znode 的时间（以毫秒为单位）。             |
|        **mtime**        |         上次修改此 znode 时的时间（以毫秒为单位）。          |
| **version/dataversion** |                    znode 的数据更改次数。                    |
|      **cversion**       |                  znode 的子节点的更改次数。                  |
| **aversion/aclVersion** |                  znode 的 ACL 的更改次数。                   |
|   **ephemeralOwner**    | 如果 znode 是临时节点，则此值为 znode 所有者的会话 ID。如果它不是临时节点，它将为零。 |
|     **dataLength**      |                   znode 的数据字段的长度。                   |
|     **numChildren**     |                      znode 的子节点数。                      |

# ZooKeeper Sessions

ZooKeeper 客户端通过使用语言绑定创建服务句柄来建立与 ZooKeeper 服务的会话。创建后，句柄以 CONNECTING 状态开始，客户端库尝试连接到构成 ZooKeeper 服务的服务器之一，此时它切换到 CONNECTED 状态。在正常操作期间，客户端句柄将处于这两种状态之一。如果发生不可恢复的错误，例如会话过期或身份验证失败，或者如果应用程序明确关闭句柄，则句柄将移动到 CLOSED 状态。下图显示了 ZooKeeper 客户端可能的状态转换：

![](http://rep.shaoteemo.com/state_dia.jpg)

要创建客户端会话，应用程序代码必须提供一个连接字符串，其中包含以逗号分隔的host：port对列表，每个对应一个 ZooKeeper 服务器（例如“127.0.0.1:4545”或“127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002”）。ZooKeeper 客户端库将选择任意服务器并尝试连接到它。如果此连接失败，或者客户端因任何原因与服务器断开连接，则客户端将自动尝试列表中的下一个服务器，直到（重新）建立连接。

在 3.2.0 中添加：