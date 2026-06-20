-- HASH类型
/*
    Hash是一个键值对的集合。

    Hash是一个String类型的 field (字段)和value(值)的映射表，hash 特别适合用于存储对象。
    每个Hash可以存储2^32―1键值对(40多亿)。

*/
-- hset key field value给key集合中的field赋值value。
-- 如果哈希表不存在，一个新的哈希表被创建并进行HSET操作。如果字段已经存在于哈希表中，旧值将被重写。
HSET k1 apple app.inc
HSET k1 location USA

-- hget key field从key哈希中，取出field字段的值。
HGET k1 apple
HGET k1 location

-- hmset key field1 value1 field2 value2.....批量设置哈希的字段及值。
HMSET k2 name zhangsan age 64 gender 1

-- hexists key field判断指定key中是否存在field
HEXISTS k2 apple
HEXISTS k2 gender

-- hkeys key获取该哈希中所有的field。
HKEYS k2

-- hvals key获取该哈希中所有的value。
HVALS k2

-- hincrby key field increment为哈希表key中的field字段的值加上增量increment.
-- 增量也可以为负数，相当于对指定字段进行减法操作。
--如果哈希表的key不存在，一个新的哈希表被创建并执行hincrby命令。
-- 如果指定的字段不存在，那么在执行命令前，字段的值被初始化为0。
-- 对一个储存字符串值的字段执行hincrby命令将造成一个错误。
HINCRBY k2 name 10
HINCRBY k2 age 10

-- hdel key fied1 field2..….删除哈希表key中的一个或多个指定字段，不存在的字段将被忽略。
HDEL k1 apple location

-- hsetnx key field value给key哈希表中不存在的的字段赋值。
-- 如果哈希表不存在，一个新的哈希表被创建并进行hsetnx操作。如果字段已经存在于哈希表中，操作无效。
-- 如果key不存在，一个新哈希表被创建并执行hsetnx命令。
HSETNX k2 name lisi
HSETNX k2 location china
