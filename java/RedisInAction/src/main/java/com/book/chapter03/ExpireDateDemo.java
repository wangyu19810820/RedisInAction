package com.book.chapter03;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

import java.util.Date;

// 设置剩余过期时间，过期时间点，查看剩余过期时间
public class ExpireDateDemo {

    private static final String KEY_1 = "expire_1";
    private static Jedis jedis = JedisFactory.getJedis();

    // expire设置剩余过期时间，expireAt设置过期时间点
    // pexpire设置剩余过期时间(毫秒数)，pexpireAt设置过期时间点（毫秒精度）
    // persist永不过期
    // ttl查看剩余过期时间，pttl查看剩余过期时间(毫秒精度)
    public static void main(String[] args) {
        jedis.del(KEY_1);

        jedis.set(KEY_1, "a");
        System.out.println(jedis.ttl(KEY_1));
        jedis.expire(KEY_1, 20);
        System.out.println(jedis.ttl(KEY_1));
        jedis.expireAt(KEY_1, new Date().getTime()/1000 + 30);
        System.out.println(jedis.ttl(KEY_1));
        jedis.pexpire(KEY_1, 40000L);
        System.out.println(jedis.pttl(KEY_1) + "毫秒");
        jedis.pexpireAt(KEY_1, new Date().getTime() + 50000);
        System.out.println(jedis.pttl(KEY_1) + "毫秒");
        jedis.persist(KEY_1);
        System.out.println(jedis.ttl(KEY_1));

        jedis.close();
    }
}
