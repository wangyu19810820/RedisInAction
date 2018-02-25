package com.book.chapter06.queue;

import com.JedisFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PriorityQueue {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) {
        add_worker_watch_queue("callbacks_high", "c1", "arg1", "arg2");
        add_worker_watch_queue("callbacks_medium", "c2", "arg3", "arg4");
        add_worker_watch_queue("callbacks_high", "c3", "arg1", "arg2");

        Consumer<String[]> consumer1 = s -> {
            System.out.println("c1:" + Arrays.toString(s));
        };
        Consumer<String[]> consumer2 = s -> {
            System.out.println("c2:" + Arrays.toString(s));
        };
        Map<String, Consumer<String[]>> map = new HashMap<>();
        map.put("c1", consumer1);
        map.put("c2", consumer2);
        worker_watch_queue(new String[]{"callbacks_high", "callbacks_medium"}, map);
    }

    public static void add_worker_watch_queue(String queue, String name, String... args) {
        Map<String, String> map = new HashMap<>();
        map.put("name", name);
        map.put("args", Arrays.stream(args).collect(Collectors.joining(",")));
        Gson gson = new Gson();
        jedis.rpush(queue, gson.toJson(map));
    }

    public static void worker_watch_queue(String[] queue, Map<String, Consumer<String[]>> callbackMap) {
        while (true) {
            List<String> list = jedis.blpop(30, queue);
            if (list == null || list.size() == 0) {
                continue;
            }
            Gson gson = new Gson();
            Map<String, Object> map1 = gson.fromJson(list.get(1), new TypeToken<Map<String, Object>>(){}.getType());
            if (callbackMap.containsKey(map1.get("name"))) {
                callbackMap.get(map1.get("name")).accept(((String)map1.get("args")).split(","));
            } else {
                System.out.println("没有该回调");
            }
        }
    }

}
