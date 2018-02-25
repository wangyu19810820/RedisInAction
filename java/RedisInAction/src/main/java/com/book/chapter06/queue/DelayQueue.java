package com.book.chapter06.queue;

import com.JedisFactory;
import com.book.chapter06.lock.SimpleLockUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class DelayQueue {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) {
        execute_later("market", "c1", "a,b,c", 1000);
        poll_queue();
    }

    public static String execute_later(String queue, String name, String args, int delay) {
        String identifier = UUID.randomUUID().toString();
        Map<String, String> map = new HashMap<>();
        map.put("id", identifier);
        map.put("queue", queue);
        map.put("name", name);
        map.put("args", args);
        Gson gson = new Gson();
        String str = gson.toJson(map);
        if (delay > 0) {
            jedis.zadd("delayed:", System.currentTimeMillis() + delay, str);
        } else {
            jedis.rpush("queue:" + queue, str);
        }
        return identifier;
    }

    // 从延迟队列（有序集合）中取出数据，插入到普通队列中
    // 延迟队列是否有到期数据，没有就休眠10毫秒
    // 有就取出数据，转换成json
    // 获取锁，插入到合适的普通队列中，删除有序集合内的数据，释放锁
    public static void poll_queue() {
        while (true) {
            Set<Tuple> set = jedis.zrangeWithScores("delayed:", 0, 0);
            Tuple item = (set == null || set.size() <= 0) ? null : set.iterator().next();
            if (item == null || item.getScore() > System.currentTimeMillis()) {
                try {
                    Thread.sleep(10);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Gson gson = new Gson();
            Map<String, String> map = gson.fromJson(item.getElement(), new TypeToken<Map<String, String>>(){}.getType());
            String lock = SimpleLockUtil.acquire_lock(map.get("id"), 5);
            if (lock == null) {
                continue;
            }
            try {
                if (jedis.zrem("delayed:", item.getElement()) == 1) {
                    jedis.rpush("queue:" + map.get("queue"), item.getElement());
                }
            } finally {
                SimpleLockUtil.release_lock(map.get("id"), lock);
            }
        }
    }
}
