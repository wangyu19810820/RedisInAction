package com.book.chapter04.pipe;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.UUID;

// 模拟登陆浏览商品的过程，一个实现方式是普通方式，一个是管道方式
// 将用户token和用户id存入：用户token关系hash中
// 将用户token和当前时间存入：最近活跃用户zset中
// 将商品id和当前时间存入当前用户的浏览历史zset中
// 当前用户浏览历史zset，只保留最近25个商品id
// 热门商品zset中，将该商品的分值+1
public class PipeDemo {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws IOException {
        jedis.flushAll();
        int count = 0;
        long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
            String userID = String.valueOf(count + 1);
            String token = UUID.randomUUID().toString();
            String itemID = "item" + count;
//            updateToken(userID, token, itemID);
            updateTokenWithPiple(userID, token, itemID);
            count++;
        }
        System.out.println(count);
    }

    public static void updateToken(String userID, String token, String itemID) {
        double now = System.currentTimeMillis() / 1000;
        jedis.hset("token:", token, userID);
        jedis.zadd("current:", now, token);
        jedis.zadd("view:" + token, now, itemID);
        jedis.zremrangeByRank("view:" + token, 0, -26);
        jedis.zincrby("view:", 1, itemID);
    }

    public static void updateTokenWithPiple(String userID, String token, String itemID) throws IOException {
        double now = System.currentTimeMillis() / 1000;
        Pipeline pipe = jedis.pipelined();
        pipe.multi();
        pipe.hset("token:", token, userID);
        pipe.zadd("current:", now, token);
        pipe.zadd("view:" + token, now, itemID);
        pipe.zremrangeByRank("view:" + token, 0, -26);
        pipe.zincrby("view:", 1, itemID);
        pipe.exec();
        pipe.close();
    }
}
