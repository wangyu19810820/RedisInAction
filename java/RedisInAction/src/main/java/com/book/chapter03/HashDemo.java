package com.book.chapter03;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

// 散列(hash)
public class HashDemo {

    private static final String KEY_1 = "hash_1";
    private static Jedis jedis = JedisFactory.getJedis();

    public static void main(String[] args) {
        jedis.del(KEY_1);
        basic();
        basic1();
        jedis.close();
    }

    // hset, hmset设置
    // hget, hmget获取
    // hdel删除一个或多个键值对
    // hlen键值对总数，hexists是否存在某键
    public static void basic() {
        jedis.hset(KEY_1, "a", "1");
        Map<String, String> map = new HashMap<>();
        map.put("b", "2");
        map.put("c", "3");
        map.put("d", "4");
        System.out.println(jedis.hmset(KEY_1, map));
        System.out.println(jedis.hget(KEY_1, "b"));
        System.out.println(jedis.hmget(KEY_1, "c", "d"));
        System.out.println(jedis.hlen(KEY_1));
        System.out.println(jedis.hexists(KEY_1, "c"));
        System.out.println(jedis.hdel(KEY_1, "c", "b"));
        System.out.println(jedis.hexists(KEY_1, "c"));
    }

    // hgetAll获取全部键值对，hkeys全部键，hvals全部值
    // hincrBy对某个键的值加指定的整数，hincrByFloat对某个键的值加指定的浮点数
    public static void basic1() {
        System.out.println("--------------------------------------");
        System.out.println(jedis.hgetAll(KEY_1));
        System.out.println(jedis.hincrBy(KEY_1, "a", 2));
        System.out.println(jedis.hincrByFloat(KEY_1, "d", 2.2));
//        System.out.println(jedis.hincrBy(KEY_1, "d", 2)); // JedisDataException: ERR hash value is not an integer
        System.out.println(jedis.hkeys(KEY_1));
        System.out.println(jedis.hvals(KEY_1));
    }
}
