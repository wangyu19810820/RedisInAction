package com.book.chapter03;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import redis.clients.jedis.ZParams;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// ZSET，有序集合，带分值的集合
public class ZSetDemo {

    private static final String KEY_1 = "zset_1";
    private static final String KEY_2 = "zset_2";
    private static final String KEY_3 = "zset_3";
    private static Jedis jedis = JedisFactory.getJedis();

    public static void main(String[] args) {
        jedis.del(KEY_1);
        jedis.del(KEY_2);
        jedis.del(KEY_3);
//        basic();
//        range();
        math();
        jedis.close();
    }

    // zadd添加一个或多个, zrem移除一个或多个，zscore单个分值，zrank单个排名
    // zcard数量，zcount分值区间内的数量（包含开始和结尾）
    public static void basic() {
        jedis.zadd(KEY_1, 100, "a");
        Map<String, Double> map = new HashMap<>();
        map.put("b", 200.0);
        map.put("c", 300.0);
        jedis.zadd(KEY_1, map);
        System.out.println(jedis.zcard(KEY_1));
        jedis.zincrby(KEY_1, 10.0, "a");
        System.out.println("a的排名：" + jedis.zrank(KEY_1, "a"));
        System.out.println("b的排名：" + jedis.zrank(KEY_1, "b"));
        System.out.println(jedis.zscore(KEY_1, "a"));
        System.out.println(jedis.zcard(KEY_1));
        System.out.println(jedis.zcount(KEY_1, 200, 300));
        System.out.println(jedis.zrem(KEY_1, "a", "b"));
        System.out.println(jedis.zcard(KEY_1));
    }

    // zrange排名区间内的元素，zrangeWithScores排名区间内的元素和分值
    // zrevrange排名区间内的元素(分值由大到小)，zrevrangeWithScores排名区间内的元素和分值(分值由大到小)
    // zrangeByScore分值区间内的元素，zrangeByScoreWithScores分值区间内的元素和分值
    // zrevrangeByScore分值区间内的元素(分值由大到小)，zrevrangeByScoreWithScores分值区间内的元素和分值(分值由大到小)
    public static void range() {
        Map<String, Double> map = new HashMap<>();
        map.put("a", 100.0);
        map.put("b", 200.0);
        map.put("c", 300.0);
        map.put("d", 400.0);
        map.put("e", 500.0);
        jedis.zadd(KEY_1, map);

        System.out.println(jedis.zrange(KEY_1, 0, -1));
        Set<Tuple> set1 = jedis.zrangeWithScores(KEY_1, 0, -1);
        set1.forEach(t -> {System.out.print(t.getElement() + ":" + t.getScore() + "\t");});
        System.out.println();
        System.out.println(jedis.zrevrange(KEY_1, 0, -1));
        Set<Tuple> set2 = jedis.zrevrangeWithScores(KEY_1, 0, -1);
        set2.forEach(t -> {System.out.print(t.getElement() + ":" + t.getScore() + "\t");});
        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println(jedis.zrangeByScore(KEY_1, 200, 400));
        Set<Tuple> set3 = jedis.zrangeByScoreWithScores(KEY_1, 200, 400);
        set3.forEach(t -> {System.out.print(t.getElement() + ":" + t.getScore() + "\t");});
        System.out.println();
        System.out.println(jedis.zrevrangeByScore(KEY_1, 400, 200));
        Set<Tuple> set4 = jedis.zrevrangeByScoreWithScores(KEY_1, 400, 200);
        set4.forEach(t -> {System.out.print(t.getElement() + ":" + t.getScore() + "\t");});
        System.out.println();
        System.out.println("------------------------------------------------");
        jedis.zremrangeByRank(KEY_1, 0, 1);
        jedis.zremrangeByScore(KEY_1, 200, 300);
        System.out.println(jedis.zrange(KEY_1, 0, -1));
    }

    // zinterstore交集（都有的元素），zunionstore并集（所有的元素）
    // ZParams:聚合函数（计算交集和并集的方式），包括求和，最大值，最小值。默认求和
    public static void math() {
        Map<String, Double> map = new HashMap<>();
        map.put("a", 100.0);
        map.put("b", 200.0);
        map.put("c", 300.0);
        map.put("d", 400.0);
        map.put("e", 500.0);
        jedis.zadd(KEY_1, map);

        Map<String, Double> map1 = new HashMap<>();
        map1.put("a", 10.0);
        map1.put("b", 20.0);
        map1.put("c", 30.0);
        jedis.zadd(KEY_2, map1);

        jedis.zinterstore(KEY_3, KEY_1, KEY_2);
        Set<Tuple> set1 = jedis.zrangeWithScores(KEY_3, 0, -1);
        set1.forEach(t -> {System.out.print(t.getElement() + ":" + t.getScore() + "\t");});
        System.out.println();

        ZParams zParams = new ZParams();
        zParams.aggregate(ZParams.Aggregate.MAX);
        jedis.zinterstore(KEY_3, zParams, KEY_1, KEY_2);
        Set<Tuple> set2 = jedis.zrangeWithScores(KEY_3, 0, -1);
        set2.forEach(t -> {System.out.print(t.getElement() + ":" + t.getScore() + "\t");});
        System.out.println();

        jedis.zunionstore(KEY_3, KEY_1, KEY_2);
        Set<Tuple> set3 = jedis.zrangeWithScores(KEY_3, 0, -1);
        set3.forEach(t -> {System.out.print(t.getElement() + ":" + t.getScore() + "\t");});
        System.out.println();

        jedis.zunionstore(KEY_3, zParams, KEY_1, KEY_2);
        Set<Tuple> set4 = jedis.zrangeWithScores(KEY_3, 0, -1);
        set4.forEach(t -> {System.out.print(t.getElement() + ":" + t.getScore() + "\t");});
        System.out.println();

    }
}
