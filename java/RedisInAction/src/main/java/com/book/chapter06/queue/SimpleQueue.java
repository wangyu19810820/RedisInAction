package com.book.chapter06.queue;

import com.JedisFactory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleQueue {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) {
//        sendSoldEmailViaQueue("seller1", "item1", "11.01", "buyer1");
//        processSoldEmailQueue();

    }

    public static void sendSoldEmailViaQueue(String seller, String item, String price, String buyer) {
        Map<String, Object> map = new HashMap<>();
        map.put("sellerId", seller);
        map.put("itemId", item);
        map.put("price", price);
        map.put("buyer_id", buyer);
        map.put("time", new Date());
        Gson gson = new Gson();
        String str = gson.toJson(map);
        jedis.rpush("queue:email", str);
    }
//    config = (Map<String,Object>)gson.fromJson(
//    value, new TypeToken<Map<String,Object>>(){}.getType());
    public static void processSoldEmailQueue() {
        while (true) {
            List<String> list = jedis.blpop(30, "queue:email");
            if (list == null || list.size() == 0) {
                continue;
            }
            Gson gson = new Gson();
            Map<String, Object> map = gson.fromJson(list.get(1), new TypeToken<Map<String, Object>>(){}.getType());
            System.out.println(map);
        }
    }

}
