package com.book.chapter02;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

public class UserService {

    private static Jedis jedis = JedisFactory.getSingleJedis();

    public static final String USER = "user:";

    public static String newUser() {
        return USER + jedis.incr(USER);
    }
}
