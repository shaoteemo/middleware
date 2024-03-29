本文档翻译官方文档：http://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html

<h1>ZooKeeper 程序员指南-官方中文版</h1>

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

**3.2.0 中添加：**可选的“chroot”后缀也可以附加到连接字符串。这将在解释与此根路径相关的所有路径的同时运行客户端命令（类似于 unix chroot 命令）。如果使用该示例将类似于：“127.0.0.1:4545/app/a”或“127.0.0.1:3000,127.0.0.1:3001,127.0.0.1:3002/app/a”，其中客户端将基于 “/app/a” 路径和所有路径都将相对于这个路径 - 即获取/设置/等...“/foo/bar”将导致在“/app/a/foo/bar”上运行操作（基于服务器的角度）。此功能在多租户环境中特别有用，在这种环境中，特定 ZooKeeper 服务的每个用户都可能具有不同的 root 权限。 这使得重用变得更加简单，因为每个用户都可以编码他的应用程序，就好像它以“/”为根路径，而实际位置（比如 /app/a）可以在部署时确定。

当客户端获得 ZooKeeper 服务的句柄时，ZooKeeper 会创建一个 ZooKeeper 会话，表示为一个 64 位数字，并将其分配给客户端。 如果客户端连接到不同的 ZooKeeper 服务器，它将发送会话 ID 作为连接握手的一部分。 作为一种安全措施，服务器为 session id 创建一个密码，任何 ZooKeeper 服务器都可以验证该密码。当客户端建立会话时，密码将与 session id 一起发送到客户端。 每当客户端与新服务器重新建立会话时，客户端都会发送带有会话 ID 的密码。

用于创建 ZooKeeper 会话的 ZooKeeper 客户端库调用的参数之一是以毫秒为单位的会话超时。客户端发送请求的超时，服务器以它可以给客户端响应超时。当前的实现要求超时时间最少是tickTime 的2 倍（在服务器配置中设置），最多是tickTime 的20 倍。 ZooKeeper 客户端 API 允许访问协商超时。

当客户端（会话）从 ZK 服务集群中分离出来，它将开始搜索在会话创建期间指定的服务器列表。最终，当客户端和至少一台服务器之间的连接重新建立时，会话将再次转换为“已连接”状态（如果在会话超时值内重新连接）或将转换为“过期”状态 （如果在会话超时后重新连接）。不建议为断开连接而创建新的会话对象（新的 ZooKeeper.class 或 c 绑定中的 zookeeper 句柄）。 ZK 客户端库将为您处理重新连接。 特别是我们在客户端库中内置了启发式方法来处理诸如“羊群效应”之类的事情......只有在您收到会话到期通知（强制）时才创建新会话。

会话过期由 ZooKeeper 集群本身管理，而不是由客户端管理。当 ZK 客户端与集群建立会话时，它会提供上面详述的“timeout”值。集群使用此值来确定客户端的会话何时到期。当集群在指定的会话超时期限内（即没有心跳）没有收到来自客户端的消息时，就会发生过期。在会话到期时，集群将删除该会话拥有的所有临时节点，并立即将更改通知所有连接的客户端（任何监视这些 znode 的客户端）。此时，过期会话的客户端仍然与集群断开连接，直到它能够重新建立与集群的连接，否则不会通知会话过期。 客户端将一直处于断开状态，直到与集群重新建立 TCP 连接，此时过期会话的观察者将收到“会话过期”通知。

过期会话的观察者监听的过期会话的状态转换示例：

1. 'connected' : 会话已建立，客户端正在与集群通信（客户端/服务器通信正常运行）。
2. .... 客户端从集群中分离
3. 'disconnected' : 客户端与集群失去连接
4. .... 在“超时”期后，集群使会话到期，客户端从集群断开连接时什么消息也收不到。
5. .... 客户端重新获得与集群的网络级连接
6. 'expired' : 最终客户端重新连接到集群，然后通知它到期

ZooKeeper 会话建立调用的另一个参数是默认观察者。 当客户端发生任何状态更改时，会通知观察者。 例如，如果客户端失去与服务器的连接，客户端将收到通知，或者如果客户端的会话过期等......这个观察者应该考虑断开连接的初始状态（即在任何状态更改事件被发送给观察者之前）。 在新连接的情况下，发送给观察者的第一个事件通常是会话连接事件。

会话通过客户端发送请求保持活动状态。 如果会话空闲的时间会导致会话超时，客户端将发送 PING 请求以保持会话活动。 这个 PING 请求不仅允许 ZooKeeper 服务器知道客户端仍然处于活动状态，而且还允许客户端验证其与 ZooKeeper 服务器的连接是否仍然处于活动状态。 PING 的时间足够保守，以确保有合理的时间来检测断开的连接并重新连接到新服务器。

一旦与服务器的连接成功建立（已连接），基本上有两种情况客户端库会在同步或 执行异步操作，并且具有以下情况之一：

1. 应用程序对不再活动/有效的会话调用操作
2. 当服务器有待处理的操作时，ZooKeeper 客户端会与服务器断开连接，即有待处理的异步调用。

**Added in 3.2.0 -- SessionMovedException。**有一个内部异常，称为 SessionMovedException。发生此异常是因为在连接上收到了对已在不同服务器上重新建立的会话的请求。 此错误的正常原因是客户端向服务器发送请求，但网络数据包延迟，因此客户端超时并连接到新服务器。 当延迟的数据包到达第一个服务器时，旧服务器检测到会话已移动，并关闭客户端连接。 客户端通常不会看到此错误，因为它们不会从这些旧连接中读取数据。 （旧连接通常被关闭。）可以看到这种情况之一是当两个客户端尝试使用保存的会话 ID 和密码重新建立相同的连接时，其中一个客户端将重新建立连接，第二个客户端将断开连接（导致这对客户端无限期地尝试重新建立其连接/会话）。

**更新服务器列表。**我们允许客户端通过提供一个新的逗号分隔的 host:port 对列表来更新连接参数，每个列表对应一个 ZooKeeper 服务器。 该函数调用概率负载均衡算法，该算法可能会导致客户端与其当前主机断开连接，目的是实现新列表中每个服务器的预期统一连接数。 如果客户端连接的当前主机不在新列表中，则此调用将始终导致连接断开。 否则，将根据服务器数量是增加还是减少以及减少多少来做出决定。

例如，如果之前的连接参数包含 3 台主机，而现在列表包含这 3 台主机和另外 2 台主机，则连接到 3 台主机中的每台主机的 40% 的客户端将移动到新主机之一以平衡负载。 该算法将导致客户端以 0.4 的概率断开与当前主机的连接，在这种情况下，客户端会连接到随机选择的 2 个新主机之一。

另一个例子——假设我们有 5 台主机，现在更新列表以删除 2 台主机，连接到其余 3 台主机的客户端将保持连接，而连接到 2 台已删除主机的所有客户端将需要移动到 3 位主机，随机选择。 如果连接断开，客户端将进入一种特殊模式，在该模式下，他使用概率算法而不只是循环方式选择要连接的新服务器。

在第一个例子中，每个客户端决定以 0.4 的概率断开连接，但是一旦做出决定，它将尝试连接到随机的新服务器，并且只有当它无法连接到任何新服务器时，它才会尝试连接到旧服务器。那些在找到服务器或尝试新列表中的所有服务器但连接失败后，客户端返回正常操作模式，从 connectString 中选择任意服务器并尝试连接到它。 如果失败，它将继续在循环中尝试不同的随机服务器。 （见上面用于最初选择服务器的算法）

**本地会话。**3.5.0添加, 主要由[ZOOKEEPER-1147](https://issues.apache.org/jira/browse/ZOOKEEPER-1147)实现。

- 背景：在 ZooKeeper 中创建和关闭会话的成本很高，因为它们需要仲裁（投票）确认，当需要处理数千个客户端连接时，它们成为 ZooKeeper 集群的瓶颈。 所以在 3.5.0 之后，我们引入了一种新的会话类型：本地会话，它没有普通（全局）会话的完整功能，通过打开 localSessionsEnabled 可以使用此功能。

当 localSessionsUpgradingEnabled 被禁用时：

- 本地会话无法创建临时节点
- 一旦本地会话丢失，用户将无法使用会话 ID/密码重新建立会话，会话及其监视将永远消失。 注意：失去 tcp 连接并不一定意味着会话丢失。 如果可以在会话超时之前与同一个 zk 服务器重新建立连接，那么客户端可以继续（它根本无法移动到另一个服务器）。
- 当本地会话连接时，会话信息仅在它所连接的 zookeeper 服务器上维护。 领导者不知道这个会话的创建，也没有写入磁盘的状态。
- ping、过期和其他会话状态维护由当前会话连接到的服务器处理。

当启用 localSessionsUpgradingEnabled 时：

- 本地会话可以自动升级为全局会话。

- 创建新会话时，它会在本地保存在包装好的 LocalSessionTracker 中。 随后可以根据需要将其升级为全局会话（例如，创建临时节点）。 如果请求升级，会话将从本地集合中删除，同时保持相同的会话 ID。

- 目前，只有操作：创建临时节点需要将会话从本地升级到全局。 原因是临时节点的创建在很大程度上取决于全局会话。 如果本地会话可以在不升级到全局会话的情况下创建临时节点，则会导致不同节点之间的数据不一致。 领导者还需要了解会话的生命周期，以便在 *关闭/到期* 时清理临时节点。 这需要全局会话，因为本地会话与其特定服务器相关联。

- 一个会话在升级过程中既可以是本地会话，也可以是全局会话，但是升级的操作不能被两个线程同时调用。

- ZooKeeperServer(Standalone) 使用 SessionTrackerImpl； LeaderZookeeper 使用 LeaderSessionTracker，它持有 SessionTrackerImpl(global) 和 LocalSessionTracker(如果启用)； FollowerZooKeeperServer 和 ObserverZooKeeperServer 使用 LearnerSessionTracker 保存 LocalSessionTracker。 关于会话的类的 UML 图：

  +----------------+ +--------------------+ +---------------------+ | | --> | | ----> | LocalSessionTracker | | SessionTracker | | SessionTrackerImpl | +---------------------+ | | | | +-----------------------+ | | | | +-------------------------> | LeaderSessionTracker | +----------------+ +--------------------+ | +-----------------------+ | | | | | | | +---------------------------+ +---------> | | | UpgradeableSessionTracker | | | | | ------------------------+ +---------------------------+ | | | v +-----------------------+ | LearnerSessionTracker | +-----------------------+

Q&A

- 使用配置选项禁用本地会话升级的原因是什么？
  - 在想要处理大量客户端的大型部署中，我们知道客户端通过观察者连接，这应该只是本地会话。 所以这更像是一种防止有人意外创建大量临时节点和全局会话的保护措施。
- 会话是什么时候创建的？
  - 在当前的实现中，它会在处理 ConnectRequest 和 createSession 请求到达 FinalRequestProcessor 时尝试创建本地会话。
- 如果创建会话在服务器 A 上发送并且客户端断开与其他服务器 B 的连接，该服务器最终再次发送创建会话，然后断开连接并连接回服务器 A，会发生什么情况？
  - 当客户端重新连接到 B 时，其 sessionId 将不存在于 B 的本地会话跟踪器中。所以 B 将发送验证包。如果 A 发出的 CreateSession 在验证数据包到达之前提交，则客户端将能够连接。否则，客户端将导致会话过期，因为仲裁（投票）尚不知道此会话。如果客户端还尝试再次连接回 A，则会话已从本地会话跟踪器中删除。所以A需要向leader发送一个验证包。根据请求的时间，结果应该与 B 相同。

# ZooKeeper Watches

ZooKeeper 中的所有读取操作 - getData()、getChildren() 和 exists() - 都可以选择将监视设置为副操作。以下是 ZooKeeper 对 watch 的定义：watch 事件是一次性触发器，发送到设置 watch 的客户端，当设置 watch 的数据发生变化时发生。在对 watch 的定义中，需要考虑三个关键点：

- **一次性触发** 当数据发生变化时，会向客户端发送一个 watch 事件。例如，如果客户端执行 getData("/znode1", true) 并且稍后更改或删除了 /znode1 的数据，则客户端将获得 /znode1 的监视事件。如果 /znode1 再次更改，则不会发送监视事件，除非客户端进行了另一次读取以设置新监视。
- **发送到客户端** 这意味着事件正在到达客户端的途中，但在更改操作的成功返回码到达发起更改的客户端之前可能无法到达客户端。 Watches 异步发送给观察者。 ZooKeeper 提供了排序保证：客户端永远不会看到它设置了监视的更改，直到它第一次看到监视事件。网络延迟或其他因素可能会导致不同的客户端在不同时间查看更新并返回代码。关键是不同客户端看到的一切都会有一个一致的顺序。
- **设置监视的数据** 这是指节点可以更改的不同方式。将 ZooKeeper 视为维护两个监视列表会有所帮助：数据监视和子（节点）监视。 getData() 和 exists() 设置数据监视。 getChildren() 设置子（节点）监视。或者，考虑根据返回的数据类型设置watchs可能会有所帮助。 getData() 和 exists() 返回有关节点数据的信息，而 getChildren() 返回子节点列表。因此， setData() 将触发正在设置的 znode 的数据监视（如果设置成功）。成功的 create() 将触发正在创建的 znode 的数据观察和父 znode 的子观察。成功的 delete() 将同时触发数据观察和子（节点）观察（因为不能再有子节点）、被删除的 znode 以及父 znode 的子（节点）观察。

Watches 在客户端连接到 ZooKeeper 服务器后在本地维护。这允许 Watchs 在设置、维护和调度方面是轻量级的。当客户端连接到新服务器时，任何会话事件都会触发监视。与服务器断开连接时将不会收到Watches。当客户端重新连接时，如果需要，任何先前注册的Whatchs将被重新注册和触发。一般来说，这一切都是公开的。有一种情况可能会丢失监视：如果在断开连接的情况下创建和删除 znode，则会丢失尚未创建的 znode 存在的监视。

**3.6.0 中的新功能：**客户端还可以在 znode 上设置永久的递归监视，这些监视在触发时不会被删除，并且会以递归方式触发注册的 znode 以及任何子 znode 上的更改。

## Watches的语义

我们可以使用三个读取 ZooKeeper 状态的调用来设置监视：exists、getData 和 getChildren。以下列表详细说明了监视可以触发的事件以及启用它们的调用：

- **Created event:** 通过调用exists启用。
- **Deleted event:** 通过调用exists、getData 和getChildren 启用。
- **Changed event:** 通过调用exists 和getData 启用。
- **Child event:** 通过调用 getChildren 启用。

## 持久、递归的Watches

**3.6.0 中的新功能：**现在在上述标准 watch 上有一个变体，您可以设置一个在触发时不会被移除的 watch 。此外，这些监视会触发事件类型 NodeCreated、NodeDeleted 和 NodeDataChanged，并且可以选择递归地针对所有从监视注册的 znode 开始的 znode 节点。请注意，NodeChildrenChanged 事件不会为持久递归观察触发，因为它是多余的。

使用 addWatch() 方法设置持久监视。 触发语义和保证（一次性触发除外）与标准 Watches相同。 关于事件的唯一例外是递归持久观察者永远不会触发子更改事件，因为它们是多余的。 使用具有观察者类型 WatcherType.Any 的 removeWatches() 移除持久观察。

## 移除Watches

我们可以通过调用 removeWatches 来删除在 znode 上注册的Watchs。此外，即使没有服务器连接，ZooKeeper 客户端也可以通过将本地标志设置为 true 来删除本地监视。以下列表详细说明了成功移除Watchs后将触发的事件。

- **Child Remove event:** 通过调用 getChildren 添加的 Watcher。
- **Data Remove event:** 通过调用exists或getData添加的观察者。
- **Persistent Remove event:** Watcher 是通过调用添加的来添加一个持久的 watch。

## ZooKeeper 对Watches的保障

关于Watches，ZooKeeper 维护这些保障：

- Watches是根据其他事件、其他Watches和异步回复进行排序的。 ZooKeeper 客户端库确保按顺序分派所有内容。
- 在监听到对应于该 znode 的新数据之前，客户端将看到它正在监视的 znode 的事件。
- 来自 ZooKeeper 的观察事件的顺序对应于 ZooKeeper 服务所看到的更新顺序。

## 关于Watches的注意事项

- 标准Watches是一次性触发器；如果您收到一个 watch 事件，并且希望收到有关未来更改的通知，则必须设置另一个 watch。
- 因为标准Watches是一次性触发器，并且在获取事件和发送新请求以获取Watches之间存在延迟，所以您无法可靠地看到 ZooKeeper 中节点发生的每一个变化。准备好处理 znode 在获取事件和再次设置 watch 之间多次更改的情况。 （你可能不在乎，但至少意识到它可能会发生。）
- 一个监视对象，或 函数/上下文对 ，对于给定的通知只会被触发一次。例如，如果为同一个文件注册了同一个监视对象，并且为同一个文件调用了 getData，然后删除了该文件，则该监视对象将仅被调用一次，并带有该文件的删除通知。
- 当您与服务器断开连接时（例如，当服务器出现故障时），在重新建立连接之前您将不会获得任何监视。出于这个原因，会话事件被发送到所有未完成的监视处理程序。使用会话事件进入安全模式：断开连接时您不会接收事件，因此您的进程应该在该模式下保守地运行。

# ZooKeeper 访问控制使用 ACL 

ZooKeeper 使用 ACL 来控制对其 znode（ZooKeeper 数据树的数据节点）的访问。ACL 实现与 UNIX 文件访问权限非常相似：它使用权限来允许/禁止针对节点的各种操作以及这些权限适用的范围。与标准 UNIX 权限不同，ZooKeeper 节点不受用户（文件所有者）、组和全局（其他）三个标准范围的限制。 ZooKeeper 没有 znode 所有者的概念。相反，ACL 指定了与这些 id 相关联的 id 和权限集。

另请注意，ACL 仅适用于特定的 znode。尤其不适用于子节点。例如，如果 /app 只能被 ip:172.16.16.1 读取，而 /app/status 是全局可读的，那么任何人都可以读取 /app/status； ACL 不是递归的。

ZooKeeper 支持可插入的身份验证方案。Id 使用形式 scheme:expression 指定，其中 scheme 是 id 对应的身份验证方案。有效表达式集由方案定义。例如，ip:172.16.16.1 是使用 ip 方案的地址为 172.16.16.1 的主机的 ID，而digest:bob:password 是使用摘要方案的名称为 bob 的用户的 ID。

当客户端连接到 ZooKeeper 并对其进行身份验证时，ZooKeeper 将与客户端对应的所有 id 与客户端连接相关联。当客户端尝试访问节点时，会根据 znode 的 ACL 检查这些 id。 ACL 由成对的 (scheme:expression, perms) 组成。表达式的格式特定于方案。例如，对 向 IP (ip:19.22.0.0/16, READ) 地址以 19.22 开头的任何客户端授予 READ 权限。

## ACL 权限

ZooKeeper 支持以下权限：

- **CREATE**: 允许创建一个子节点
- **READ**: 允许从节点获取数据并列出其子节点。
- **WRITE**: 允许为节点设置数据
- **DELETE**: 允许删除一个子节点
- **ADMIN**: 允许设置权限

CREATE 和 DELETE 权限已脱离 WRITE 权限，以实现更细粒度的访问控制。 CREATE 和 DELETE 的情况如下：

您希望 A 能够在 ZooKeeper 节点上执行设置，但不能*创建*或*删除*子节点。

CREATE without DELETE：客户端通过在父目录中创建 ZooKeeper 节点来创建请求。您希望所有客户端都可以添加，但只有请求处理器可以删除。 （这有点像文件的 APPEND 权限。）

此外，还有 ADMIN 权限，因为 ZooKeeper 没有文件所有者的概念。在某种意义上，ADMIN 权限将实体指定为所有者。 ZooKeeper 不支持 LOOKUP 权限（对目录执行权限以允许您LOOKUP，即使您无法列出目录）。每个人都隐含地拥有 LOOKUP 权限。这允许您统计一个节点，但仅此而已。 （问题是，如果你想在一个不存在的节点上调用 zoo_exists() ，则没有权限检查。）

ADMIN 权限在 ACL 方面也有特殊作用：为了检索 znode 用户的 ACL，必须具有 READ 或 ADMIN 权限，但没有 ADMIN 权限，摘要哈希值将被屏蔽。

### 内置 ACL 方案

ZooKeeper 有以下内置方案：

- **world** 有一个 id，代表任何人。
- **auth** 是一种特殊方案，它忽略任何提供的表达式，而是使用当前用户、凭据和方案。在保留 ACL 时，ZooKeeper 服务器将忽略提供的任何表达式（无论是用户是 SASL 身份验证还是 user:password 以 DIGEST 身份验证）。但是，表达式仍必须在 ACL 中提供，因为 ACL 必须与格式 scheme:expression:perms 匹配。提供此方案是为了方便，因为它是用户创建 znode 并将对该 znode 的访问权限限制为仅该用户的常见用例。如果没有经过身份验证的用户，则使用 auth 方案设置 ACL 将失败。
- **digest** 使用 username:password 字符串生成 MD5 哈希，然后将其用作于 ACL ID 身份。身份验证是通过以明文形式发送用户名：密码来完成的。在 ACL 中使用时，表达式将是 username:base64 编码的 SHA1 密码摘要。
- **ip** 使用客户端主机 IP 作为 ACL ID 身份。 ACL 表达式的格式为 addr/bits，其中 addr 的最高有效位与客户端主机 IP 的最高有效位相匹配。
- **x509** 使用客户端 X500 Principal 作为 ACL ID 身份。 ACL 表达式是客户端的准确 X500 主体名称。使用安全端口时，客户端会自动进行身份验证并设置其 x509 方案的身份验证信息。

### ZooKeeper C 客户端 API

本节略：http://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html#ZooKeeper+C+client+API

# 可插入的 ZooKeeper 身份验证

ZooKeeper 运行在各种不同的环境中，具有各种不同的身份验证方案，因此它具有完全可插拔的身份验证框架。甚至内置的身份验证方案也使用可插入的身份验证框架。

要了解身份验证框架的工作原理，首先必须了解两个主要的身份验证操作。框架首先必须对客户端进行身份验证。这通常在客户端连接到服务器后立即完成，包括验证从客户端发送或收集的有关客户端的信息并将其与连接相关联。框架处理的第二个操作是在 ACL 中查找对应于客户端的条目。 ACL 条目是 <idspec, permissions> 对。 idspec 可以是与连接关联的身份验证信息匹配的简单字符串，也可以是针对该信息进行评估的表达式。由身份验证插件的实现来进行匹配。这是身份验证插件必须实现的接口：

```java
public interface AuthenticationProvider {
    String getScheme();
    KeeperException.Code handleAuthentication(ServerCnxn cnxn, byte authData[]);
    boolean isValid(String id);
    boolean matches(String id, String aclExpr);
    boolean isAuthenticated();
}
```

第一个方法 getScheme 返回插件标识的字符串。由于我们支持多种身份验证方法，因此身份验证凭据或 idspec 将始终以 scheme: 为前缀。 ZooKeeper 服务器使用身份验证插件返回的方案来确定该方案适用于哪些 id。

当客户端发送与连接相关联的身份验证信息时，将调用 handleAuthentication。客户端指定信息对应的方案。 ZooKeeper 服务器将信息传递给身份验证插件，其 getScheme 与客户端传递的方案相匹配。 handleAuthentication 的实现者通常会在确定信息错误时返回错误，或者使用 cnxn.getAuthInfo().add(new Id(getScheme(), data)) 将信息与连接相关联。

身份验证插件涉及设置和使用 ACL。当为 znode 设置 ACL 时，ZooKeeper 服务器会将条目的 id 部分传递给 isValid(String id) 方法。由插件来验证 id 是否具有正确的形式。例如，ip:172.16.0.0/16 是一个有效的 id，但 ip:host.com 不是。如果新的 ACL 包含“auth”条目，则使用 isAuthenticated 来查看是否应将与连接关联的此方案的身份验证信息添加到 ACL 中。某些方案不应包含在 auth.例如，如果指定了 auth，则客户端的 IP 地址不被视为应添加到 ACL 的 id。

ZooKeeper 在检查 ACL 时调用matches(String id, String aclExpr)。它需要将客户端的认证信息与相关的 ACL 条目进行匹配。为了找到适用于客户端的条目，ZooKeeper 服务器将找到每个条目的方案，如果该方案有来自该客户端的身份验证信息，则将调用matches(String id, String aclExpr) 并将 id 设置为身份验证先前由 handleAuthentication 添加到连接的信息，并将 aclExpr 设置为 ACL 条目的 id。身份验证插件使用自己的逻辑和匹配方案来确定 id 是否包含在 aclExpr 中。

有两个内置的身份验证插件：ip 和digest。可以使用系统属性添加其他插件。在启动时，ZooKeeper 服务器将查找以“zookeeper.authProvider”开头的系统属性。并将这些属性的值解释为身份验证插件的类名。可以使用 -Dzookeeeper.authProvider.X=com.f.MyAuth 或在服务器配置文件中添加如下条目来设置这些属性：

```properties
authProvider.1=com.f.MyAuth
authProvider.2=com.f.MyAuth2
```

应注意确保属性上的后缀是唯一的。如果有 -Dzookeeeper.authProvider.X=com.f.MyAuth -Dzookeeper.authProvider.X=com.f.MyAuth2 等重复项，则只会使用一个。此外，所有服务器都必须定义相同的插件，否则使用插件提供的身份验证方案的客户端将无法连接到某些服务器。

**在 3.6.0 中添加：**替代抽象可用于可插拔身份验证。它提供了额外的参数。

```java
public abstract class ServerAuthenticationProvider implements AuthenticationProvider {
    public abstract KeeperException.Code handleAuthentication(ServerObjs serverObjs, byte authData[]);
    public abstract boolean matches(ServerObjs serverObjs, MatchValues matchValues);
}
```

不是实现 AuthenticationProvider，而是扩展 ServerAuthenticationProvider。然后您的 handleAuthentication() 和matches() 方法将接收额外的参数（通过ServerObjs 和MatchValues）。

- **ZooKeeperServer** ZooKeeperServer 实例
- **ServerCnxn** 当前连接
- **path** 正在操作的 ZNode 路径（如果未使用，则为 null）
- **perm** 操作值或0
- **setAcls** 当 setAcl() 方法被操作时，正在设置的 ACL 列表

# 一致性保证

ZooKeeper 是一种高性能、可扩展的服务。读取和写入操作都设计得很快，尽管读取比写入快。这样做的原因是在读取的情况下，ZooKeeper 可以为较旧的数据提供服务，而这又是由于 ZooKeeper 的一致性保证：

- **顺序一致性：**来自客户端的更新将按照发送的顺序应用。
- **原子性：**更新要么成功要么失败——没有部分结果。
- **单一系统映像：**客户端将看到相同的最新服务视图，无论它连接到哪个服务器。即使客户端故障转移到具有相同会话的不同服务器，客户端也永远不会看到系统的旧视图。
- **可靠性：**一旦应用更新，它将从那一刻起持续存在，直到客户端覆盖更新。这个保证有两个推论：
  1. 如果客户端获得成功的返回码，则更新将被应用。在某些失败（通信错误、超时等）时，客户端将不知道更新是否已应用。我们采取措施尽量减少失败，但保证只存在于成功的返回代码中。 （这在 Paxos 中称为单调性条件。）
  2. 从服务器故障中恢复时，客户端通过读取请求或成功更新看到的任何更新数据将永远不会回滚。
- **及时性：** 系统的客户端视图保证在一定的时间范围内（大约几十秒）是最新的。在此范围内的客户端将看到系统更改，或者客户端将检测到服务中断。

使用这些一致性保证可以很容易地构建更高级别的功能，例如领导者选举、屏障（barriers）、队列和仅在 ZooKeeper 客户端上的 读/写 可撤销锁（无需添加到 ZooKeeper）。有关更多详细信息，请参阅[Recipes and Solutions](http://zookeeper.apache.org/doc/r3.7.0/recipes.html)。

> 有时，开发人员错误地假设了 ZooKeeper 实际上没有提供的另一种保证。如： “同时一致的跨客户端视图”：ZooKeeper 不保证在每个时间实例中，两个不同的客户端将具有相同的 ZooKeeper 数据视图。由于网络延迟等因素，一个客户端可能会在另一个客户端收到更改通知之前执行更新。考虑两个客户端A和B的场景，如果客户端A将一个znode /a的值从0设置为1，然后告诉客户端B读取/a，客户端B可能会读取旧值0，具体取决于哪个服务器它连接到。如果客户端 A 和客户端 B 读取相同的值很重要，客户端 B 应该在执行读取之前从 ZooKeeper API 方法调用 sync() 方法。因此，ZooKeeper 本身并不能保证所有服务器同步发生更改，但 ZooKeeper 原语可用于构建提供有用客户端同步的更高级别的函数。 （有关更多信息，请参阅 [ZooKeeper Recipes](http://zookeeper.apache.org/doc/r3.7.0/recipes.html)）。

# 绑定

本节略：http://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html#ch_bindings

The ZooKeeper client libraries come in two languages: Java and C. The following sections describe these.

## Java Binding

There are two packages that make up the ZooKeeper Java binding: **org.apache.zookeeper** and **org.apache.zookeeper.data**. The rest of the packages that make up ZooKeeper are used internally or are part of the server implementation. The **org.apache.zookeeper.data** package is made up of generated classes that are used simply as containers.

The main class used by a ZooKeeper Java client is the **ZooKeeper** class. Its two constructors differ only by an optional session id and password. ZooKeeper supports session recovery across instances of a process. A Java program may save its session id and password to stable storage, restart, and recover the session that was used by the earlier instance of the program.

When a ZooKeeper object is created, two threads are created as well: an IO thread and an event thread. All IO happens on the IO thread (using Java NIO). All event callbacks happen on the event thread. Session maintenance such as reconnecting to ZooKeeper servers and maintaining heartbeat is done on the IO thread. Responses for synchronous methods are also processed in the IO thread. All responses to asynchronous methods and watch events are processed on the event thread. There are a few things to notice that result from this design:

- All completions for asynchronous calls and watcher callbacks will be made in order, one at a time. The caller can do any processing they wish, but no other callbacks will be processed during that time.
- Callbacks do not block the processing of the IO thread or the processing of the synchronous calls.
- Synchronous calls may not return in the correct order. For example, assume a client does the following processing: issues an asynchronous read of node **/a** with *watch* set to true, and then in the completion callback of the read it does a synchronous read of **/a**. (Maybe not good practice, but not illegal either, and it makes for a simple example.) Note that if there is a change to **/a** between the asynchronous read and the synchronous read, the client library will receive the watch event saying **/a** changed before the response for the synchronous read, but because of the completion callback blocking the event queue, the synchronous read will return with the new value of **/a** before the watch event is processed.

Finally, the rules associated with shutdown are straightforward: once a ZooKeeper object is closed or receives a fatal event (SESSION_EXPIRED and AUTH_FAILED), the ZooKeeper object becomes invalid. On a close, the two threads shut down and any further access on zookeeper handle is undefined behavior and should be avoided.

### Client Configuration Parameters

The following list contains configuration properties for the Java client. You can set any of these properties using Java system properties. For server properties, please check the [Server configuration section of the Admin Guide](http://zookeeper.apache.org/doc/r3.7.0/zookeeperAdmin.html#sc_configuration). The ZooKeeper Wiki also has useful pages about [ZooKeeper SSL support](https://cwiki.apache.org/confluence/display/ZOOKEEPER/ZooKeeper+SSL+User+Guide), and [SASL authentication for ZooKeeper](https://cwiki.apache.org/confluence/display/ZOOKEEPER/ZooKeeper+and+SASL).

- *zookeeper.sasl.client* : Set the value to **false** to disable SASL authentication. Default is **true**.
- *zookeeper.sasl.clientconfig* : Specifies the context key in the JAAS login file. Default is "Client".
- *zookeeper.server.principal* : Specifies the server principal to be used by the client for authentication, while connecting to the zookeeper server, when Kerberos authentication is enabled. If this configuration is provided, then the ZooKeeper client will NOT USE any of the following parameters to determine the server principal: zookeeper.sasl.client.username, zookeeper.sasl.client.canonicalize.hostname, zookeeper.server.realm Note: this config parameter is working only for ZooKeeper 3.5.7+, 3.6.0+
- *zookeeper.sasl.client.username* : Traditionally, a principal is divided into three parts: the primary, the instance, and the realm. The format of a typical Kerberos V5 principal is primary/instance@REALM. zookeeper.sasl.client.username specifies the primary part of the server principal. Default is "zookeeper". Instance part is derived from the server IP. Finally server's principal is username/IP@realm, where username is the value of zookeeper.sasl.client.username, IP is the server IP, and realm is the value of zookeeper.server.realm.
- *zookeeper.sasl.client.canonicalize.hostname* : Expecting the zookeeper.server.principal parameter is not provided, the ZooKeeper client will try to determine the 'instance' (host) part of the ZooKeeper server principal. First it takes the hostname provided as the ZooKeeper server connection string. Then it tries to 'canonicalize' the address by getting the fully qualified domain name belonging to the address. You can disable this 'canonicalization' by setting: zookeeper.sasl.client.canonicalize.hostname=false
- *zookeeper.server.realm* : Realm part of the server principal. By default it is the client principal realm.
- *zookeeper.disableAutoWatchReset* : This switch controls whether automatic watch resetting is enabled. Clients automatically reset watches during session reconnect by default, this option allows the client to turn off this behavior by setting zookeeper.disableAutoWatchReset to **true**.
- *zookeeper.client.secure* : **New in 3.5.5:** If you want to connect to the server secure client port, you need to set this property to **true** on the client. This will connect to server using SSL with specified credentials. Note that it requires the Netty client.
- *zookeeper.clientCnxnSocket* : Specifies which ClientCnxnSocket to be used. Possible values are **org.apache.zookeeper.ClientCnxnSocketNIO** and **org.apache.zookeeper.ClientCnxnSocketNetty** . Default is **org.apache.zookeeper.ClientCnxnSocketNIO** . If you want to connect to server's secure client port, you need to set this property to **org.apache.zookeeper.ClientCnxnSocketNetty** on client.
- *zookeeper.ssl.keyStore.location and zookeeper.ssl.keyStore.password* : **New in 3.5.5:** Specifies the file path to a JKS containing the local credentials to be used for SSL connections, and the password to unlock the file.
- *zookeeper.ssl.trustStore.location and zookeeper.ssl.trustStore.password* : **New in 3.5.5:** Specifies the file path to a JKS containing the remote credentials to be used for SSL connections, and the password to unlock the file.
- *zookeeper.ssl.keyStore.type* and *zookeeper.ssl.trustStore.type*: **New in 3.5.5:** Specifies the file format of keys/trust store files used to establish TLS connection to the ZooKeeper server. Values: JKS, PEM, PKCS12 or null (detect by filename). Default: null. **New in 3.6.3, 3.7.0:** The format BCFKS was added.
- *jute.maxbuffer* : In the client side, it specifies the maximum size of the incoming data from the server. The default is 0xfffff(1048575) bytes, or just under 1M. This is really a sanity check. The ZooKeeper server is designed to store and send data on the order of kilobytes. If incoming data length is more than this value, an IOException is raised. This value of client side should keep same with the server side(Setting **System.setProperty("jute.maxbuffer", "xxxx")** in the client side will work), otherwise problems will arise.
- *zookeeper.kinit* : Specifies path to kinit binary. Default is "/usr/bin/kinit".

## C Binding

The C binding has a single-threaded and multi-threaded library. The multi-threaded library is easiest to use and is most similar to the Java API. This library will create an IO thread and an event dispatch thread for handling connection maintenance and callbacks. The single-threaded library allows ZooKeeper to be used in event driven applications by exposing the event loop used in the multi-threaded library.

The package includes two shared libraries: zookeeper_st and zookeeper_mt. The former only provides the asynchronous APIs and callbacks for integrating into the application's event loop. The only reason this library exists is to support the platforms were a *pthread* library is not available or is unstable (i.e. FreeBSD 4.x). In all other cases, application developers should link with zookeeper_mt, as it includes support for both Sync and Async API.

### Installation

If you're building the client from a check-out from the Apache repository, follow the steps outlined below. If you're building from a project source package downloaded from apache, skip to step **3**.

1. Run `ant compile_jute` from the ZooKeeper top level directory (*.../trunk*). This will create a directory named "generated" under *.../trunk/zookeeper-client/zookeeper-client-c*.
2. Change directory to the*.../trunk/zookeeper-client/zookeeper-client-c* and run `autoreconf -if` to bootstrap **autoconf**, **automake** and **libtool**. Make sure you have **autoconf version 2.59** or greater installed. Skip to step**4**.
3. If you are building from a project source package, unzip/untar the source tarball and cd to the* zookeeper-x.x.x/zookeeper-client/zookeeper-client-c* directory.
4. Run `./configure <your-options>` to generate the makefile. Here are some of options the **configure** utility supports that can be useful in this step:

- `--enable-debug` Enables optimization and enables debug info compiler options. (Disabled by default.)
- `--without-syncapi` Disables Sync API support; zookeeper_mt library won't be built. (Enabled by default.)
- `--disable-static` Do not build static libraries. (Enabled by default.)
- `--disable-shared` Do not build shared libraries. (Enabled by default.)

#### Note

> See INSTALL for general information about running **configure**. 1. Run `make` or `make install` to build the libraries and install them. 1. To generate doxygen documentation for the ZooKeeper API, run `make doxygen-doc`. All documentation will be placed in a new subfolder named docs. By default, this command only generates HTML. For information on other document formats, run `./configure --help`

### Building Your Own C Client

In order to be able to use the ZooKeeper C API in your application you have to remember to

1. Include ZooKeeper header: `#include <zookeeper/zookeeper.h>`
2. If you are building a multithreaded client, compile with `-DTHREADED` compiler flag to enable the multi-threaded version of the library, and then link against the *zookeeper_mt* library. If you are building a single-threaded client, do not compile with `-DTHREADED`, and be sure to link against the_zookeeper_st_library.

#### Note

> See *.../trunk/zookeeper-client/zookeeper-client-c/src/cli.c* for an example of a C client implementation

# 构建块：ZooKeeper 操作指南

本节略：http://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html#ch_guideToZkOperations

## Building Blocks: A Guide to ZooKeeper Operations

This section surveys all the operations a developer can perform against a ZooKeeper server. It is lower level information than the earlier concepts chapters in this manual, but higher level than the ZooKeeper API Reference. It covers these topics:

- [Connecting to ZooKeeper](http://zookeeper.apache.org/doc/r3.7.0/zookeeperProgrammers.html#sc_connectingToZk)

### Handling Errors

Both the Java and C client bindings may report errors. The Java client binding does so by throwing KeeperException, calling code() on the exception will return the specific error code. The C client binding returns an error code as defined in the enum ZOO_ERRORS. API callbacks indicate result code for both language bindings. See the API documentation (javadoc for Java, doxygen for C) for full details on the possible errors and their meaning.

### Connecting to ZooKeeper

Before we begin, you will have to set up a running Zookeeper server so that we can start developing the client. For C client bindings, we will be using the multithreaded library(zookeeper_mt) with a simple example written in C. To establish a connection with Zookeeper server, we make use of C API - *zookeeper_init* with the following signature:

```c
int zookeeper_init(const char *host, watcher_fn fn, int recv_timeout, const clientid_t *clientid, void *context, int flags);
```

- **host* : Connection string to zookeeper server in the format of host:port. If there are multiple servers, use comma as separator after specifying the host:port pairs. Eg: "127.0.0.1:2181,127.0.0.1:3001,127.0.0.1:3002"
- *fn* : Watcher function to process events when a notification is triggered.
- *recv_timeout* : Session expiration time in milliseconds.
- **clientid* : We can specify 0 for a new session. If a session has already establish previously, we could provide that client ID and it would reconnect to that previous session.
- **context* : Context object that can be associated with the zkhandle_t handler. If it is not used, we can set it to 0.
- *flags* : In an initiation, we can leave it for 0.

We will demonstrate client that outputs "Connected to Zookeeper" after successful connection or an error message otherwise. Let's call the following code *zkClient.cc* :

```c
#include <stdio.h>
#include <zookeeper/zookeeper.h>
#include <errno.h>
using namespace std;

// Keeping track of the connection state
static int connected = 0;
static int expired   = 0;

// *zkHandler handles the connection with Zookeeper
static zhandle_t *zkHandler;

// watcher function would process events
void watcher(zhandle_t *zkH, int type, int state, const char *path, void *watcherCtx)
{
    if (type == ZOO_SESSION_EVENT) {

        // state refers to states of zookeeper connection.
        // To keep it simple, we would demonstrate these 3: ZOO_EXPIRED_SESSION_STATE, ZOO_CONNECTED_STATE, ZOO_NOTCONNECTED_STATE
        // If you are using ACL, you should be aware of an authentication failure state - ZOO_AUTH_FAILED_STATE
        if (state == ZOO_CONNECTED_STATE) {
            connected = 1;
        } else if (state == ZOO_NOTCONNECTED_STATE ) {
            connected = 0;
        } else if (state == ZOO_EXPIRED_SESSION_STATE) {
            expired = 1;
            connected = 0;
            zookeeper_close(zkH);
        }
    }
}

int main(){
    zoo_set_debug_level(ZOO_LOG_LEVEL_DEBUG);

    // zookeeper_init returns the handler upon a successful connection, null otherwise
    zkHandler = zookeeper_init("localhost:2181", watcher, 10000, 0, 0, 0);

    if (!zkHandler) {
        return errno;
    }else{
        printf("Connection established with Zookeeper. \n");
    }

    // Close Zookeeper connection
    zookeeper_close(zkHandler);

    return 0;
}
```

Compile the code with the multithreaded library mentioned before.

```
> g++ -Iinclude/ zkClient.cpp -lzookeeper_mt -o Client
```

Run the client.

```
> ./Client
```

From the output, you should see "Connected to Zookeeper" along with Zookeeper's DEBUG messages if the connection is successful.

# 常见问题和故障排除

现在你知道了 ZooKeeper。它快速、简单，您的应用程序可以运行，但是等等……出了点问题。以下是 ZooKeeper 用户陷入的一些陷阱：

1. 如果您使用watches，则必须查找已连接的 watch 事件。 当 ZooKeeper 客户端与服务器断开连接时，在重新连接之前您不会收到更改通知。如果您正在观察 znode 是否存在，如果在您断开连接时创建和删除 znode，您将错过该事件。
2. 您必须测试 ZooKeeper 服务器故障。只要大多数服务器处于活动状态，ZooKeeper 服务就可以在出现故障时幸免于难。 要问的问题是：您的应用程序可以处理吗？在现实世界中，客户端与 ZooKeeper 的连接可能会中断。 （ZooKeeper 服务器故障和网络断开是连接丢失的常见原因。）ZooKeeper 客户端库负责恢复您的连接并让您知道发生了什么，但您必须确保恢复您的状态和任何未完成的失败请求。 看看你是否在测试实验室中做对了，而不是在生产中 - 使用由多个服务器组成的 ZooKeeper 服务进行测试，并使它们重新启动。
3. 客户端使用的 ZooKeeper 服务器列表必须与每个 ZooKeeper 服务器拥有的 ZooKeeper 服务器列表相匹配。如果客户端列表是 ZooKeeper 服务器真实列表的一个子集，那么可以工作，尽管不是最佳状态，但如果客户端列出不在 ZooKeeper 集群中的 ZooKeeper 服务器，则可能不会正常工作。
4. 请小心放置事务日志的位置。ZooKeeper 对性能最关键的部分是事务日志。ZooKeeper 必须在返回响应之前将事务同步到日志文件。专用的事务日志设备是保持良好性能的关键。将日志放在繁忙的设备上会对性能产生不利影响。如果你只有一个存储设备，把跟踪文件放在NFS上并增加snapshotCount；它不能消除问题，但可以减轻负担。
5. 正确设置您的 Java 最大堆大小。避免交换非常重要。不必要地访问磁盘几乎肯定会降低您的性能，令人无法接受。请记住，在 ZooKeeper 中，一切都是有序的，因此如果一个请求访问磁盘数据，则所有其他排队的请求都会访问磁盘数据。为避免交换，请尝试将堆大小设置为您拥有的物理内存量减去操作系统和缓存所需的量。为您的配置确定最佳堆大小的最佳方法是运行负载测试。如果由于某种原因您不能进行测试，请保守估计并选择一个远低于会导致您的机器交换的限制的数字。例如，在 4G 机器上，3G 堆是一个保守的估计。

## Links to Other Information

Outside the formal documentation, there're several other sources of information for ZooKeeper developers.

- *[API Reference](https://zookeeper.apache.org/doc/current/apidocs/zookeeper-server/index.html)* : The complete reference to the ZooKeeper API
- *[ZooKeeper Talk at the Hadoop Summit 2008](https://www.youtube.com/watch?v=rXI9xiesUV8)* : A video introduction to ZooKeeper, by Benjamin Reed of Yahoo! Research
- *[Barrier and Queue Tutorial](https://cwiki.apache.org/confluence/display/ZOOKEEPER/Tutorial)* : The excellent Java tutorial by Flavio Junqueira, implementing simple barriers and producer-consumer queues using ZooKeeper.
- *[ZooKeeper - A Reliable, Scalable Distributed Coordination System](https://cwiki.apache.org/confluence/display/ZOOKEEPER/ZooKeeperArticles)* : An article by Todd Hoff (07/15/2008)
- *[ZooKeeper Recipes](http://zookeeper.apache.org/doc/r3.7.0/recipes.html)* : Pseudo-level discussion of the implementation of various synchronization solutions with ZooKeeper: Event Handles, Queues, Locks, and Two-phase Commits.

