package com.book.chapter02;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.Date;
import java.util.Set;

// 数据缓存Service，分为设置程序和处理程序
public class DataCacheService {

    public static final String INV = "inv:";
    public static final String SCHEDULE = "schedule:";
    public static final String DELAY = "delay:";
    private static Jedis jedis = JedisFactory.getSingleJedis();

    // 设置要缓存哪条数据，重做缓存的时间间隔
    public static void schedule(int id, int delay) {
        double now = new Date().getTime() / 1000;
        jedis.zadd(SCHEDULE, now, String.valueOf(id));
        jedis.zadd(DELAY, delay, String.valueOf(id));
    }

    // 取有序集合SCHEDULE的第一个个值，如果SCHEDULE中没值，或者触发时间大于当前时间，休眠0.5秒继续
    // 如果小于等于当前时间，再取DELAY的分值，如果为0，删除数据缓存，删除SCHEDULE和DELAY中的值。
    // 如果不为0，重做缓存，重新设置SCHEDULE
    public static void cache() {
        while (true) {
            Set<Tuple> set = jedis.zrangeWithScores(SCHEDULE, 0, 0);
            if (set == null) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            Tuple[] array = set.toArray(new Tuple[0]);
            if (array.length <=0 || array[0].getScore() > new Date().getTime() / 1000) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            String rowId = array[0].getElement();
            double delay = jedis.zscore(DELAY, rowId);
            if (delay <= 0) {
                jedis.zrem(SCHEDULE, rowId);
                jedis.zrem(DELAY, rowId);
                jedis.del(INV + rowId);
                continue;
            }
            jedis.set(INV + rowId, generator(Integer.parseInt(rowId)));
            jedis.zadd(SCHEDULE, new Date().getTime() / 1000 + delay, rowId);
        }
    }

    private static String generator(int id) {
        return "{id:" + id + ",name:'a'}";
    }
}
