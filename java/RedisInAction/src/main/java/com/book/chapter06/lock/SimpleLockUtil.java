package com.book.chapter06.lock;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

public class SimpleLockUtil {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) {
        String identifier = acquire_lock("market", 10);
        System.out.println("first:" + identifier);
        identifier = acquire_lock("market", 10);
        System.out.println("second:" + identifier);

        release_lock("market", identifier);
    }

    // 在数秒内，获取锁，超时没有获取成功，返回失败
    public static String acquire_lock(String lockName, int timeout) {
        String uuid = UUID.randomUUID().toString();
        long end = System.currentTimeMillis() + timeout * 1000;
        while (System.currentTimeMillis() < end) {
            long result = jedis.setnx("lock:" + lockName, uuid);
            if (result == 1) {
                return uuid;
            }
            try {
                Thread.sleep(1);
            }catch(InterruptedException ie){
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }

    // 释放锁，循环监视锁是否改动，当锁的值不是自身值的时候，取消监视，返回
    // 如果锁的值是自身值的时候，删除锁，返回
    public static boolean release_lock(String lockName, String identifier) {
        String key = "lock:" + lockName;
        while (true) {
            jedis.watch(key);
            if (identifier.equals(jedis.get(key))) {
                Transaction trans = jedis.multi();
                trans.del(key);
                List<Object> list = trans.exec();
                if (list == null) {
                    continue;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
    }
}
