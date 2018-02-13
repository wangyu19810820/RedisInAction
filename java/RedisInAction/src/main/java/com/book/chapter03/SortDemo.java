package com.book.chapter03;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.SortingParams;

public class SortDemo {

    private static final String KEY_1 = "sort_1";
    private static Jedis jedis = JedisFactory.getJedis();

    public static void main(String[] args) {
        jedis.del(KEY_1);
        jedis.rpush(KEY_1, "7", "15", "23", "110");
        System.out.println(jedis.sort(KEY_1));
        SortingParams sortingParams = new SortingParams();
        sortingParams.alpha();
        System.out.println(jedis.sort(KEY_1, sortingParams));
    }
}
