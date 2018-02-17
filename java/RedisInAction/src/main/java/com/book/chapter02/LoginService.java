package com.book.chapter02;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

import java.util.Date;
import java.util.Set;

public class LoginService {

    private static Jedis jedis = JedisFactory.getSingleJedis();

    public static final String TOKEN = "token:";
    public static final String TOKEN_GENERATOR = "token_generator";
    public static final String RECENT = "recent:";

    private static int MAX_COUNT = 2;

    public static String login(String user) {
        String token = "token_" + jedis.incr(TOKEN_GENERATOR);
        jedis.hset(TOKEN, token, user);
        jedis.zadd(RECENT, new Date().getTime() / 1000, token);
        return token;
    }

    public static void clean() {
        while (true) {
            long count = jedis.zcard(RECENT);
            if (count <= MAX_COUNT) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            long min = count - MAX_COUNT > 100 ? 100 : count - MAX_COUNT;
//            jedis.zremrangeByRank(RECENT, 0, min - 1);
            Set<String> tokens = jedis.zrange(RECENT, 0, min - 1);
            for (String token : tokens) {
                jedis.hdel(TOKEN, token);
                jedis.zrem(RECENT, token);
            }
        }
    }
}
