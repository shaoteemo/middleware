-- hyperloglog
/*
    在我们做站点流量统计的时候一般会统计页面UV(独立访客:unique visitor)和PV(即页面浏览量: page view)。

    什么是基数?

    数据集{1，2，5，7，5，7，9}，那么这个数据集的基数集为{1，2，5，7，9}，基数(不重复元素)为5，
    基数估计就是在误差可接受范围内，快速计算基数。

    如果是通过Redis来处理，我们可以使用String类型然后自增计数即可达到统计PV，
    统计UV可以使用Set，每个用户id是唯一的可以放到这个集合里。

    以上方案虽然结果准确，但随着数据不断增加，导致占用的内存空间越来越大，对于非常大的数据集是不合适的。
    Hyperloglog是一种基数估算统计，在输入元素的数量特别巨大时，计算基数所需的空间是固定的，并且很小。
    在Redis中，每个Hyperloglog只占用12KB内存，就可以计算接近2^64个不同元素的基数。

    因为HyperLogLog只会更具输入元素来计算基数，而不会存储输入元素本身，所以Hyperloglog不能像集合那样，返回输入的各个元素。

*/

--pfadd key element1 element2....将所有元素参数添加到 Hyperloglog数据结构中。
-- 如果至少有个元素被添加返回1，否则返回0。
PFADD book1 java c++
PFADD book1 java php

-- pfcount key1 key2.….*计算Hyperloglog近似基数，可以计算多个Hyperloglog，统计基数总数。
PFCOUNT book1
PFADD book2 chinese math
PFCOUNT book1 book2

-- pfmerge destkey sourcekey1 sourcekey2...将一个或多个Hyperloglog (sourcekey1)合并成一个Hyperloglog (destkey ) 。
-- 比如每月活跃用户可用每天活跃用户合并后计算。
PFMERGE book book2 book1
PFCOUNT book

