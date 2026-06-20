-- String类型数据操作。一个KEY对应一个VALUE
-- String是二进制安全类型。可以包含序列化对象或图片
-- 最多可以放512MB数据
-- 设置值相同key则覆盖
SET k1 v1

-- 获取值（没有则返回null）
GET k1

-- 追加原值（如果不存在则新增）
APPEND k1 追加内容

--获取值字符串长度，一个中文字符为3
STRLEN k1

--设置值的时候给定一个过期时间(SETEX key exp val)
SETEX k3 10 v3

-- key不存在是设置值。如果存在则不影响(分布式锁实现)
SETNX k1 v1
SET k1 v1 NX EX 10

-- 自增仅限数字,否则报错.如果不存在key则先初始化0再自增
INCR k3

-- 自减仅限数字,否则报错.如果不存在key则先初始化0再自减
DECR k3

-- 自增/自减指定值仅限数字,否则报错(INCRBY key step).
INCRBY k3 5
DECRBY k3 7

--同时设置多对键值(mset key val ... key val)
MSET k1 v1 k2 v2 k3 v3 k4 v4

-- 获取多个值(mget key ... key)
MGET k1 k2 k3 k4 k5

-- 所有key都不存在时，同时设置一个或多个键值对（msetnx key val ... key val）.此命令具有原子性有一个存在都失败
MSETNX k5 v5 k6 v6

-- 获取指定key中的子字符串。（GETRANGE key start end）包含start与end.下标0开始
GETRANGE k1 1 3
-- 获取所有字符串
GETRANGE k1 0 -1

-- 覆盖指定key值的指定位置.(SETRANGE key start val)
SETRANGE k1 1 hellworld

/*
    Redis String为简单动态字符串。类似ArrayList。使用预分配减少内存的频繁分配。
    内存实际大小一般都要高于字符串实际大小。
    当字符串长度小于1M时，扩容都是加倍现有的空间，如果超过1M，扩容时每次只会多扩1M的空间。字符串最大长度为512M。
*/
