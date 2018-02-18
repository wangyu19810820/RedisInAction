package com.book.chapter04.sync;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

// 主从配置的demo，
// 主服务器写入一个唯一虚构值(unique dummy value)
// 从服务器上读取复制状态为成功，从服务器上读取该值成功，等待从服务器保存数据到aof文件后（状态值判断，或者等待1秒）
// 删除唯一虚构值
public class SyncDemo {

    static Jedis jedisWrite = new Jedis("127.0.0.1", 6379, 3000);
    static Jedis jedisRead = new Jedis("127.0.0.1", 6378, 3000);
    public static final String SYNC_WAIT = "sync_wait";

    public static void main(String[] args) throws Exception {
        double now = new Date().getTime() / 1000;
        String uuid = UUID.randomUUID().toString();
        jedisWrite.zadd(SYNC_WAIT, now, uuid);

        while (true) {
            String info = jedisRead.info();
            System.out.println(info);
            Properties properties = new Properties();
            properties.load(new StringReader(info));
            // 主从复制成功
            if (properties.getProperty("master_link_status").equals("up")) {
                System.out.println("-------------------------主从复制成功！");
                break;
            } else {
                Thread.sleep(1);
            }
        }
        while (true) {
            double score = jedisRead.zscore(SYNC_WAIT, uuid);
            System.out.println("score:" + score);
            if (score > 0) {
                System.out.println("-------------------------获取到唯一虚构值！");
                break;
            } else {
                Thread.sleep(1);
            }
        }
        double oneSecDelayTime = new Date().getTime() + 1001;
        while (new Date().getTime() < oneSecDelayTime) {
            String info = jedisRead.info();
            System.out.println(info);
            Properties properties = new Properties();
            properties.load(new StringReader(info));
            // 主从复制成功
            if (properties.getProperty("aof_pending_bio_fsync").equals("0")) {
                System.out.println("-------------------------保存到AOF文件成功！");
                break;
            } else {
                Thread.sleep(1);
            }
        }

        // 删除唯一虚构值
        jedisWrite.zrem(SYNC_WAIT, uuid);
        jedisWrite.zremrangeByScore(SYNC_WAIT, 0, new Date().getTime() / 1000 - 900);
    }
}
