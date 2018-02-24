package com.book.chapter06.semaphore;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

// 计数信号量，允许一定数量的线程获取资源
// 用有序集合实现，分值是申请时间，排名在限定数量内的信号量有效，否则无效
public class SimpleSemaphore {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) {
        String result1 = acquireSemaphore("market", 2, 10);
        String result2 = acquireSemaphore("market", 2, 10);
        String result3 = acquireSemaphore("market", 2, 10);
        System.out.println(result1);
        System.out.println(result2);
        System.out.println(result3);
    }

    // 计数信号量，有序集合，成员是uuid，分值是时间的秒数。该有序集合，有一个长度，只允许有限个数的值
    // 在有序集合中，把超时的成员移除
    // 往有序集合中，添加uuid和插入时间
    // 这个值处于limit内，返回uuid。否则，从有序集合中删除该值。
    public static String acquireSemaphore(String semaphoreName, int limit, int timeout) {
        String key = "semaphore:" + semaphoreName;
        String identifier = UUID.randomUUID().toString();
        Transaction trans = jedis.multi();
        trans.zremrangeByScore(key, "-inf", String.valueOf(System.currentTimeMillis() / 1000 - timeout));
        trans.zadd(key, System.currentTimeMillis() / 1000, identifier);
        Response<Long> response = trans.zrank(key, identifier);
        List<Object> list = trans.exec();
//        if (list.get(list.size() - 1))
        System.out.println(response.get());
        if (response.get() < limit) {
            return identifier;
        } else {
            jedis.zrem(key, identifier);
            return null;
        }
    }
}
