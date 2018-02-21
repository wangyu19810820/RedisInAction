package com.book.chapter05.config;

import com.JedisFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class AutoConfig {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static long lastChecked = 0;                  // 上次检查时间
    public static boolean isUnderMaintenance = false;   // 是否处于维护期

    public static Map<String, Long> checkMap = new HashMap<>();                 // 上次检查时间的map
    public static Map<String, Map<String, Object>> configMap = new HashMap<>(); // 配置项
    public static Map<String, Jedis> connectionMap = new HashMap<>();

    public static void main(String[] args) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("a", "zz");
//        map.put("b", 1);
//        setConfig("type1", "component1", map);

//        Map<String, Object> map1 = getConfig("type1", "component1");
//        System.out.println(map1);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("host", "127.0.0.1");
        map2.put("port", 6378);
        setConfig("redis", "component2", map2);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Jedis jedis1 = redisConnection("component2");
        System.out.println(jedis1);
    }

    // 从远程redis获取配置项，是否处于维护期，并缓存到内存中。
    // 如果据上次检查不足一秒，则取用内存中的数据
    public static boolean isUnderMaintenance() {
        long now = System.currentTimeMillis();
        if (now - lastChecked > 1000) {
            lastChecked = now;
            isUnderMaintenance = "yes".equals(jedis.get("is-under-maintenance"));
        }
        return isUnderMaintenance;
    }

    // 保存配置项
    public static void setConfig(String type, String component, Map<String, Object> config) {
        Gson gson = new Gson();
        String key = "config:" + type + ":" + component;
        configMap.put(key, config);
        jedis.set(key, gson.toJson(config));
    }

    // 获取配置项，并保存到内存中。如果1秒内再次取相同的配置，就从内存中取，否则从redis中取
    public static Map<String, Object> getConfig(String type, String component) {
        long now = System.currentTimeMillis();
        Gson gson = new Gson();
        String key = "config:" + type + ":" + component;
        Long lastCheckTime = checkMap.get(key);
        if (lastCheckTime == null || now - lastCheckTime > 1000) {
            String value = jedis.get(key);
            Map<String, Object> map = null;
            if (value == null || "".equals(value)) {
                map = new HashMap<>();
            } else {
                map = gson.fromJson(value, new TypeToken<Map<String, Object>>() {}.getType());
            }
            configMap.put(key, map);
            return map;
        } else {
            return configMap.get(key);
        }
    }

    // 获取相应模块的redis连接，redis连接信息保存在配置redis配置服务器中
    // 如果配置变化，根据配置新建并缓存新redis连接，否则使用缓存中的redis连接
    public static Jedis redisConnection(String component){
        String key = "config:redis:" + component;
        // getConfig会更新configMap内的值，所以oldConfig需要先取
        Map<String, Object> oldConfig = configMap.get(key);
        Map<String, Object> newConfig = getConfig("redis", component);
        if (oldConfig.equals(newConfig)) {
            return connectionMap.get(key);
        } else {
            String host = (String)newConfig.get("host");
            double port = (Double) newConfig.get("port");
            Jedis jedis = new Jedis(host, (int)port, 3000);
            connectionMap.put(key, jedis);
            return jedis;
        }
    }
}
