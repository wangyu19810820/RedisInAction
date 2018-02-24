package com.book.chapter06.semaphore;

import com.JedisFactory;
import com.book.chapter06.lock.SimpleLockUtil;
import redis.clients.jedis.Jedis;

// 在获取计数信号量的时候，添加值和判断是否有效其实分成了两步
// 通过锁的机制，取锁成功，再获取计数信号量，比较完善
public class LockSemaphore {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws Exception {
        System.out.println(acquire_semaphore_with_lock("market", 2, 10));
    }

    // 获取锁，成功，再获取计数信号量
    public static String acquire_semaphore_with_lock(String semname, int limit, int timeout) throws Exception {
        String lock = SimpleLockUtil.acquire_lock_with_timeout("semaphore", 1, timeout);
        try {
            if (lock != null) {
                return FairSemaphore.acquire(semname, limit, timeout);
            } else {
                return null;
            }
        } finally {
            SimpleLockUtil.release_lock("semaphore", lock);
        }
    }
}
