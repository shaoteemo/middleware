-- geospatial
/*

    GEO，Geographic,地理信息的缩写。

    该类型就是元素的二维坐标，在地图上就是经纬度。Redis基于该类型，提供了经纬度设置、查询、范围查询、距离查询、经纬度Hash等常见操作。

*/
-- geoadd key longitude latitude member [longitude latitude membe. ....][用于存储指定的地理空间位置，可以将一个或多个经度(longitude)、纬度(latitude)、位置名称(member)添加到指定的 key 中。
-- 有效的经度:-180~+180有效的纬度:-85.05~+85.05，当设置的经度纬重值超过范围会报错。两级无法直接添加。
-- —般会直接下载城市数据，直接通过java程序直接一次性导入。
GEOADD chainacity 116.4052 39.32 BeiJin
GEOADD chainacity 104.4052 30.32 ChengDu 121.47254 31.231706 ShangHai

-- geopos key member [member .....[从给定的 key里返回所有指定名称(member)的位置（经度和纬度)，不存在的返回nil。
GEOPOS chainacity ChengDu ShangHai

/*
    geodist key member1 member2 [m | km |ft|mi]用于返回两个给定位置之间的距离。最后一个距离单位参数说明:
        m:米，默认单位。
        km :千米。
        mi:英里。
        ft:英尺。
*/
GEODIST chainacity ChengDu ShangHai km

/*

    georadius key longitude latitude radius m|km |ft| mi
    以给定的经纬度（(longitude latitude)为中心,返回键包含的位置元素当中，与中心的距离不超过给定最大距离（radius )的所有位置元素。

*/
-- 以经纬度116 40 为中心 1200km 为半径符合chainacity的数据
GEORADIUS chainacity 116 40 1200 km