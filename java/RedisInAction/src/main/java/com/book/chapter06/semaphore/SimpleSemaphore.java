package com.book.chapter06.semaphore;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

public class SimpleSemaphore {

    static Jedis jedis = JedisFactory.getSingleJedis();

    // 计数信号量，有序集合，成员是uuid，分值是时间的秒数。该有序集合，有一个长度，只允许有限个数的值
    // 在有序集合中，把超时的成员移除
    // 往有序集合中，添加uuid和插入时间
    // 这个值处于limit内，返回uuid。否则，从有序集合中删除该值。
    public static String acquireSemaphore(String semaphoreName, int limit, int timeout) {
        String key = "semaphore:" + semaphoreName;
        String identifier = UUID.randomUUID().toString();
        Transaction trans = jedis.multi();
        trans.zadd(key, System.currentTimeMillis() / 1000, identifier);
        trans.zrank(key, identifier);
        List<Object> list = trans.exec();

        return null;
    }
}
