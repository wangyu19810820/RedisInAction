package com.book.chapter03;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

// 数据类型：Set(集合)的操作，包括基本操作和数学集合操作
public class SetDemo {
    private static final String KEY_1 = "set_1";
    private static final String KEY_2 = "set_2";
    private static final String KEY_3 = "set_3";
    private static Jedis jedis = JedisFactory.getJedis();

    public static void main(String[] args) {
        jedis.del(KEY_1);
        jedis.del(KEY_2);
        jedis.del(KEY_3);
//        basic();
        math();
        jedis.close();
    }

    // sadd添加元素，srem移除元素，spop弹出随机元素
    // scard数量，sismember元素是否在集合中，smembers所有元素
    // srandmember一定数量的随机元素集，参数count为正数，返回不重复，count为负数，返回可重复
    private static void basic() {
        jedis.sadd(KEY_1, "a", "b", "c", "b");
        System.out.println(jedis.smembers(KEY_1));
        System.out.println(jedis.scard(KEY_1));
        System.out.println(jedis.sismember(KEY_1, "a"));
        System.out.println(jedis.sismember(KEY_1, "z"));
        System.out.println(jedis.srandmember(KEY_1, 4));
        System.out.println(jedis.srandmember(KEY_1, -4));
        System.out.println(jedis.srem(KEY_1, "a", "b"));
        System.out.println(jedis.smembers(KEY_1));
        System.out.println("------------------------------------------");
        jedis.sadd(KEY_1, "a", "b", "c", "b");
        System.out.println(jedis.smembers(KEY_1));
        jedis.smove(KEY_1, KEY_2, "a");
        System.out.println(jedis.spop(KEY_1));
        System.out.println(jedis.smembers(KEY_1));
        System.out.println(jedis.smembers(KEY_2));
    }

    // sdiff差集，sinter交集，sunion并集
    // sdiffstore差集，存入目标集合中
    // sinterstore交集，存入目标集合中
    // sunionstore并集，存入目标集合中
    private static void math() {
        jedis.sadd(KEY_1, "a", "b", "c");
        jedis.sadd(KEY_2, "a", "b", "x", "y", "z");
        System.out.println(jedis.sdiff(KEY_1, KEY_2));
        System.out.println(jedis.sinter(KEY_1, KEY_2));
        System.out.println(jedis.sunion(KEY_1, KEY_2));
        System.out.println("------------------------------------------");
        jedis.sdiffstore(KEY_3, KEY_1, KEY_2);
        System.out.println(jedis.smembers(KEY_1));
        System.out.println(jedis.smembers(KEY_2));
        System.out.println(jedis.smembers(KEY_3));
        System.out.println("------------------------------------------");
        jedis.sinterstore(KEY_3, KEY_1, KEY_2);
        System.out.println(jedis.smembers(KEY_1));
        System.out.println(jedis.smembers(KEY_2));
        System.out.println(jedis.smembers(KEY_3));
        System.out.println("------------------------------------------");
        jedis.sunionstore(KEY_3, KEY_1, KEY_2);
        System.out.println(jedis.smembers(KEY_1));
        System.out.println(jedis.smembers(KEY_2));
        System.out.println(jedis.smembers(KEY_3));
    }
}
