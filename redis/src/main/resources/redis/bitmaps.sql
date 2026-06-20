--Bitmaps
/*
    在计算机中，用二进制（位）作为存储信息的基本单位，1个字节等于8位。

    例如"abc"字符串是由3个字节组成，计算机存储时使用其二进制表示,"abc"分别对应的ASCII码是97、98、99，对应的二进制是01100001、01100010、01100011.

    合理地使用位能够有效地提高内存使用率和开发效率。

    Redis提供了Bitmaps这个“数据结构”可以实现对位的操作:
        1. Bitmaps 本身不是一种数据结构，实际上它就是字符串(key对应的value就是上图中的一串二进制)，但是它可以对字符串的位进行操作。
        2.Bitmaps 单独提供了一套命令，所以在Redis 中使用Bitmaps和使用字符串的方法不太相同。可以把Bitmaps想象成一个以位为单位的数组，
            数组的每个单元只能存储О和1，数组的下标在Bitmaps中叫做偏移量。
*/
-- setbit key offset value设置Bitmaps中某个偏移量的值。偏移量从O开始，且value值只能为0或1。
SETBIT sign 0 1
SETBIT sign 1 1
SETBIT sign 2 0
SETBIT sign 3 1

--getbit key offset获取Bitmaps中某个偏移量的值。获取key的offset的值。不存在的偏移量为0
GETBIT sign 2
GETBIT sign 99

-- bitcount key [start end]统计字符串被设置为1的bit数量。
-- 一般情况下，给定的整个字符串都会被进行统计，可以选择通过额外的start和end参数，指定字节组(即一组八位二进制数为一组)范围内进行统计（包括start和end)，0表示第一个元素，-1表示最后一个元素。
BITCOUNT sign 0 -1

-- bitop and/or destkey sourcekey1 sourcekey..…将多个Bitmaps通过求交集/并集方式合并成一个新的bitmaps。
/*
    or k3   1 1 0 1 1 1 0 1

    k1      1 0 0 1 1 1 0 1
    k2      1 1 0 1 0 1 0 0

    and k3  1 0 0 1 0 1 0 0
*/
BITOP AND k1 k1 k2
BITOP OR k1 k1 k2