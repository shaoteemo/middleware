-- SET无序集合
/*
与List类似是一个列表功能，但Set是自动排重的，当需要存储一个列表数据，又不希望出现重复数据时，Set是一个很好的选择。

Set是String类型的无序集合，它底层其实是一个value为null的hash表，所以添加、删除、查找的时间复杂度都是O(1)。

一般来说，一个算法如果是O(1)，随着数据增加，查找数据的时间不变。

集合中最大的成员数为2^32-1(每个集合超过40亿个元素)。
*/

-- sadd key value1 value...将一个或多个元素添加到集合key中，已经存在的元素将被忽略。
SADD k1 v1 v2 v3 v4 v5 v6 v2

-- smembers key取出该集合的所有元素。
SMEMBERS k1

-- sismember key value判断集合key中是否含有value元素，如有返回1，否则返回0。
SISMEMBER k1 v5

-- scard key返回该集合的元素个数。
SCARD k1

-- srem key value1 value2..删除集合中的一个或多个成员元素，不存在的成员元素会被忽略。
SREM k1 v1 v3

-- spop key随机删除集合中一个元素并返回该元素。
SPOP k1

-- srandmember key count随机取出集合中count个元素，但不会删除。
SRANDMEMBER k1 3

-- smove sourcekey destinationkey value将value元素从sourcekey集合移动到destinationkey集合中。
SMOVE k1 k2 v6

-- sinter key1 key2返回两个集合的交集元素。
SINTER k1 k2

-- sunion key1 key2返回两个集合的并集元素。
SUNION k1 k2

-- sdiff key1 key2返回两个集合的差集元素(key1中的，不包含key2)
SDIFF k1 k2

