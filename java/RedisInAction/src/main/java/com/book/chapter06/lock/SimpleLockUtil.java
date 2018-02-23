package com.book.chapter06.lock;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

public class SimpleLockUtil {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) {
        testAcquireLockWithTimeout();
    }

    public static void testAcquireLockWithTimeout() {
//        String identifier = acquire_lock("market", 10);
//        System.out.println("first:" + identifier);
        String identifier1 = acquire_lock_with_timeout("market", 10, 10);
        System.out.println("second:" + identifier1);
    }

    public static void testAcquireLock() {
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

    // 在acquireTimeout期内，设置锁键的值（仅新增成功模式），
    // 如果该锁没有设置超时时间，设置超时时间
    public static String acquire_lock_with_timeout(String lockName, int acquireTimeout, int lockTimeout) {
        String key = "lock:" + lockName;
        String identifier = UUID.randomUUID().toString();
        long end = System.currentTimeMillis() + acquireTimeout * 1000;
        while (System.currentTimeMillis() < end) {
            if (jedis.setnx(key, identifier) == 1) {
                jedis.expire(key, lockTimeout);
                return identifier;
            } else if (jedis.ttl(key) == -1) {
                jedis.expire(key, lockTimeout);
            }
            try {
                Thread.sleep(1);
            }catch(InterruptedException ie){
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }
}
