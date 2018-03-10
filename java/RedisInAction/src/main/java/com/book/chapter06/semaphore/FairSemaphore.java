package com.book.chapter06.semaphore;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.ZParams;

import java.util.UUID;

// 公平计数信号量
// 简单计数信号量要求客户端的时间尽量保持一致，如果相差太多，就会出现错误
// 公平计数信号量，有两个有序集合。成员都是uuid，一个分值是时间，一个分值是自增值
// 判断超时的时候使用时间分值，判断先后采用自增值
public class FairSemaphore {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws Exception {
        String sem1 = acquire("market", 2, 5000);
        System.out.println(sem1);

        Thread.sleep(6000);

        String sem2 = acquire("market", 2, 5000);
        System.out.println(sem2);
//        String sem3 = acquire("market", 2, 10000);
//        System.out.println(sem3);


//        System.out.println(release("market", sem1));
        System.out.println(refresh("market", sem1, 5000));
    }

    // 刷新信号量计数器的时间分值
    // 如果semname中仍然有identifier，刷新semname中的分值。
    // 如果semname中没有identifier，刷新失败。删除多余的值。
    // zadd当已经有值的时候，刷新分值，返回0。没有值的时候，设置分值，返回1。
    public static boolean refresh(String semname, String identifier, int timeout) {
        long result = jedis.zadd(semname, timeout, identifier);
        if (result == 1) {
            release(semname, identifier);
            return false;
        } else {
            return true;
        }
    }

    // 释放信号量计数器
    // 开启事务，删除semname中的值和semname:owner中的值
    public static boolean release(String semname, String identifier) {
        Transaction trans = jedis.multi();
        Response<Long> res = trans.zrem(semname, identifier);
        trans.zrem(semname + ":owner", identifier);
        trans.exec();
        return res.get() == 1;
    }

    // 获取信号量计数器
    // semname:counter生成计数，semname:owner存放uuid和计数器，semname存放uuid和时间
    public static String acquire(String semname, int limit, int timeout) throws Exception {
        // 生成uuid，计数器key，计数信号量key
        String uuid = UUID.randomUUID().toString();
        String czset = semname + ":owner";
        String ctr = semname + ":counter";

        // 删除过期的信号量
        long now = System.currentTimeMillis();
        Transaction transaction = jedis.multi();
        transaction.zremrangeByScore(semname.getBytes(), "-inf".getBytes(), String.valueOf(now - timeout).getBytes());
        ZParams zParams = new ZParams();
        zParams.weights(1, 0);// 结果集合中，czset里面的分值将被保留，semname中的分值乘以0也就是完全无效
        transaction.zinterstore(czset, zParams, czset, semname);

        // 获取计数器值
        Response<Long> res = transaction.incr(ctr);
        transaction.exec();
        long counter = res.get();

        // 添加数据
        transaction.close();
        transaction = jedis.multi();
        transaction.zadd(semname, now, uuid);
        transaction.zadd(czset, counter, uuid);

        // 检查信号量的排名是否小于limit
        Response<Long> rankResp = transaction.zrank(czset, uuid);
        transaction.exec();

        long rank = rankResp.get();
        if (rank < limit) {
            return uuid;
        } else {
            transaction.close();
            transaction = jedis.multi();
            transaction.zrem(semname, uuid);
            transaction.zrem(czset, uuid);
            transaction.exec();
            return null;
        }

    }
}
