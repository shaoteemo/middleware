-- 选择数据库
SELECT 0

-- 查看当前库key数量
DBSIZE

-- 清空当前库
FLUSHDB

-- 清空所有库
FLUSHALL

-- 获取所有key
KEYS *

-- 存储值（String）
SET k1 v1
SET k2 v2

-- 判断值是否存在
EXISTS k1 k2 k3

-- 移动值至其他库
MOVE k1 8

-- 查看key值类型
TYPE k1

-- 删除已存在的key
DEL k1

-- 设置key过期时间（秒）
EXPIRE k1 10

-- 当前key剩余过期时间（秒）
TTL k1

-- 移除key过期时间
PERSIST k1

-- 手动触发数据持久化（持久化需要配置save）
-- 阻塞同步数据
SAVE
-- 异步同步快照
BGSAVE

/*Redis事务在MULTI阶段出错都会回滚。但在执行阶段出错，出错的语句不会生效，其他正常处理*/
-- 开启事务
MULTI
-- 事务提交
EXEC
-- 事务遗弃（回滚）
DISCARD

-- 监视key（乐观锁）
WATCH key
--乐观锁配合事务使用，当一个事务变更了key时，其他事务被打断。

-- 取消对所有key监听 。如果在执行watch命令之后，exec命令或discard命令先执行的话，那么就不需要再执行unwatch。
UNWATCH

/* 以下为Redis集权相关的命令 */

-- 查看Redis信息
INFO replication

/* 主从复制：重启会恢复主节点。slave节点写数据会报错 */
-- 指定当前为从（slave）节点服务器并连接主（master）节点服务器
SLAVEOF [master node ip] port
-- 恢复主节点
SLAVEOF NO ONE

/*
Document: https://redis.io/docs/management/sentinel/
哨兵模式：用于解决主从复制Master宕机导致的写与数据同步复制问题。是一个独立进程。需单独启动。
    创建：sentinel.conf 并添加参数
        sentinel monitor mymaster 127.0.0.1 6379 1
    启动：redis-sentinel并附带sentinel.conf配置文件即可。
*/

/*
    Document: https://redis.io/docs/management/scaling/
    集群模式：
        集群链接: ~bash: ./redis-cli -c -p 6379

        Redis 集群中有 16384 个哈希槽（Slot），为了计算给定键的哈希槽，我们只需对键的 CRC16 取模 16384 即可。
*/
-- 查看集群下的节点信息
CLUSTER NODES
