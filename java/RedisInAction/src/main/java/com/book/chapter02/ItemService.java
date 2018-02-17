package com.book.chapter02;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// 商品Service
public class ItemService {

    private static Jedis jedis = JedisFactory.getSingleJedis();
    private static final int MAX_RECENT_VIEW_COUNT = 25;

    public static final String VIEW = "view:";

    public static List<String> itemList = Arrays.asList("item1", "item2", "item3");

    // 浏览商品，更新用户的活跃情况，更新用户最后浏览商品
    public static void view(String token, String item) {
        double now = new Date().getTime() / 1000;
        jedis.zadd(LoginService.RECENT, now, token);
        jedis.zadd(VIEW + token, now, item);
        jedis.zremrangeByRank(VIEW + token, 0, -MAX_RECENT_VIEW_COUNT - 1);
        jedis.zincrby(VIEW, -1, item);
    }
}
