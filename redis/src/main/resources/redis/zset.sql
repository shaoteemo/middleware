-- ZSET有序集合

/*
    Zset与Set非常相似，是一个没有重复元素的String集合。

    不同之处是Zset的每个元素都关联了一个分数(score)，这个分数被用来按照从低分到高分的方式排序集合中的元素。集合的元素是唯一的，但分数可以重复。

    因为元素是有序的，所以可以根据分数(score)或者次序(position）来获取一个范围内的元素。

*/

/*
    zadd key score1 value1 score2 value..…将一个或多个元素(value)及分数(score)加入到有序集key中。

    如果某个元素已经是有序集的元素，那么更新这个元素的分数值，并通过重新插入这个元素，来保证该元素在正确的位置上。

    分数值可以是整数值或双精度浮点数。

    如果有序集合key不存在，则创建一个空的有序集并执行zadd操作。
*/
ZADD k1 1 Java 2 PHP 3 C++ 4 Python

-- zrange key start end [withscores]返回key集合中的索引start和索引end之间的元素（包含start和end)其中元素的位置按分数值递增(从小到大)来排序。
-- 其中0表示列表的第一个元素,.-1表示最后一个元素。withscores是可选参数，是否返回分数。
ZRANGE k1 0 -1
ZRANGE k1 0 -1 WITHSCORES

--zrangebyscore key minscore maxscore [withscores]返回key集合中的分数minscore和分数maxscore之间的元素(包含minscore和maxscore )。
-- 其中元素的位置按分数值递增(从小到大)来排
ZRANGEBYSCORE k1 2 3

-- zrevrangebyscore key maxscore minscore [withscores]返回key集合中的分数maxscore和分数minxscore之间的元素（包含maxscore和minxscore )。
-- 其中元素的位置按分数值递减(从大到小)来排序。
ZREVRANGEBYSCORE k1 3 2

-- zincrby key increment value为元素value的score加上increment的值。
ZINCRBY k1 10 C++

-- zrem key value删除该集合下value的元素。
ZREM k1 PHP

-- zcount key minscore maxscore统计该集合在minscore到maxscore分数区间中元素的个数。
ZCOUNT k1 10 100

-- zrank key value返回value在集合中的排名，从0开始。
ZRANK k1 Java