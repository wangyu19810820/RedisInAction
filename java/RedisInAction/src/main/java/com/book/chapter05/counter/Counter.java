package com.book.chapter05.counter;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Counter {

    static List<Integer> precisionList = Arrays.asList(1, 5, 60, 300, 3600);
    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws Exception {
        updateCount("hits", 1);
        List<CounterModel> list = getCount("hits", 60);
        list.forEach(System.out::println);
    }

    public static void updateCount(String name, int count) throws Exception {
        double now = System.currentTimeMillis() / 1000;
        Pipeline pipeline = jedis.pipelined();
        pipeline.multi();
        for (Integer prec : precisionList) {
            int pnow = (int)(now / prec) * prec;
            String hash = prec + ":" + name;
            pipeline.zadd("known:", 0, hash);
            pipeline.hincrBy("count:" + hash, "" + pnow, count);
        }
        pipeline.exec();
        pipeline.close();
    }

    public static List<CounterModel> getCount(String name, int precision) {
        List<CounterModel> list = new ArrayList<>();
        String hash = precision + ":" + name;
        Map<String, String> data = jedis.hgetAll("count:" + hash);
        for (String key : data.keySet()) {
            list.add(new CounterModel(key, Integer.parseInt(data.get(key))));
        }
        return list;
    }
}
