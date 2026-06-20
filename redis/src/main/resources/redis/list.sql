-- List数据类新
-- List是简单的字符串列表，按照插入顺序排序。你可以添加一个元素到列表的头部(左边)）或者尾部(右边)。
-- 底层是一个双向链表，对两段操作性能极高，通过索引操作中间的节点性能较差。
-- 一个List最多可以包含2^32―1个元素（每个列表超过40亿个元素)。
-- lpush key val1 val2 ... 从左边插入元素
LPUSH k1 v1 v2 v3

-- rpush key val1 val2 ...  从右边插入
RPUSH k1 v2 v3 v4

-- lrange key start end返回key列表中的start和end之间的元素(包含start和end)。
-- 其中0表示列表的第一个元素，-1表示最后一个元素。
LRANGE k1 0 -1

-- lpop/rpop key移除并返回第一个值/最后一个值。值在键在，值光键亡。
LPOP k1
RPOP k1

-- lindex key index获取列表index位置的值（从左开始)。
LINDEX k1 1

-- llen key获取列表长度。
LLEN k1

-- lrem key count value从左边开始删除与value相同的count个元素。
LREM k1 2 v1

-- linsert key before/after value newvalue在列表中value值的前边/后边插入一个newvalue值（从左开始)。
LINSERT k1 before v2 v3
LINSERT k1 after v2 v3

-- lset key index value将索引为index的值设置为value
LSET k1 2 v4