package com.book.chapter03;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

// list(列表)的基本操作，和阻塞操作
public class ListDemo {

    private static final String KEY_1 = "list_1";
    private static final String KEY_2 = "list_2";
    private static Jedis jedis = JedisFactory.getJedis();

    public static void main(String[] args) {
        jedis.del(KEY_1);
        jedis.del(KEY_2);
//        basic();
        block();

        jedis.close();
    }

    // rpush右端推入，lpush左端推入，lpop左端推出，rpop右端推出
    // lindex索引处的元素，lrange范围内元素，包含开始和结束
    private static void basic() {
        jedis.rpush(KEY_1, "last");
        jedis.lpush(KEY_1,"first");
        jedis.rpush(KEY_1,"new last");
        System.out.println(jedis.lrange(KEY_1, 0, -1));
        System.out.println(jedis.lindex(KEY_1, 1));
        System.out.println(jedis.lpop(KEY_1));
        System.out.println(jedis.rpop(KEY_1));
        jedis.rpush(KEY_1, "a", "b", "c");
        System.out.println(jedis.lrange(KEY_1, 0, -1));
        jedis.ltrim(KEY_1, 1, 2);
        System.out.println(jedis.lrange(KEY_1, 0, -1));
        jedis.ltrim(KEY_1, -1, -2);
        System.out.println(jedis.lrange(KEY_1, 0, -1));
    }

    // blpop左端弹出，阻塞一定时间
    // brpop右端弹出，阻塞一定时间
    // rpoplpush前一个列表右端弹出，推入第二个列表
    // brpoplpush前一个列表右端弹出，推入第二个列表，阻塞一定时间
    private static void block() {
//        System.out.println(jedis.blpop(10, KEY_1, KEY_2));
//        System.out.println(jedis.brpop(10, KEY_1, KEY_2));

//        jedis.lpush(KEY_1, "a", "b", "c");
//        System.out.println(jedis.rpoplpush(KEY_1, KEY_2));

        jedis.brpoplpush(KEY_1, KEY_2, 10);

        System.out.println(jedis.lrange(KEY_1, 0, -1));
        System.out.println(jedis.lrange(KEY_2, 0, -1));
    }
}
