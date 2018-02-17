package com.book.chapter03;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

import java.util.Set;

// 这些例子来源于视频，而非《Redis实战》
// select选择某个数据库，keys显示当前数据的所有匹配指定模式的键（*）代表匹配全部
// flushDB清空当前数据库所有键，flushAll清空当前服务器所有数据库的所有键
public class OtherDemo {

    private static Jedis jedis = JedisFactory.getJedis();

    public static void main(String[] args) {
        System.out.println(jedis.select(0));
        Set<String> keys = jedis.keys("*");
        System.out.println(keys);
        System.out.println("-------------------------------------------");

        jedis.select(0);
        jedis.set("a", "1");
        jedis.select(1);
        jedis.set("a", "1");
        jedis.select(1);
        jedis.flushDB();
        System.out.println(jedis.keys("*"));
        System.out.println(jedis.select(0));
        System.out.println(jedis.keys("*"));
        System.out.println("-------------------------------------------");

        jedis.select(0);
        jedis.set("a", "1");
        jedis.select(1);
        jedis.set("a", "1");
        jedis.select(1);
        jedis.flushAll();
        System.out.println(jedis.keys("*"));
        System.out.println(jedis.select(0));
        System.out.println(jedis.keys("*"));
        System.out.println("-------------------------------------------");

    }
}
