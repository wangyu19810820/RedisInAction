package com.book.chapter02;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

import java.util.Date;

// 购物车Service
public class CartService {

    public static final String CART = "cart:";
    private static Jedis jedis = JedisFactory.getSingleJedis();

    // 将商品加入到购物车
    public static void addCart(String token, String item, int count) {
        double now = new Date().getTime() / 1000;
        jedis.zadd(LoginService.RECENT, now, token);    // 更新用户活跃情况
        if (count <= 0) {
            jedis.hdel(CART + token, item);
        } else {
            jedis.hset(CART + token, item, item);
        }
    }
}
