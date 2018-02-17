package com.book.chapter01;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

public class UserService {

    private static Jedis jedis = JedisFactory.getSingleJedis();
    public final static String USER_KEY = "user:";

    public static long newUser() {
        return jedis.incr(USER_KEY);
    }
}
