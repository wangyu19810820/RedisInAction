package com.book.chapter02;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class LoginService {

    private static Jedis jedis = JedisFactory.getSingleJedis();

    public static final String TOKEN = "token:";        // 包含token和用户id
    public static final String TOKEN_GENERATOR = "token_generator";
    public static final String RECENT = "recent:";      // 最近活跃的token，包含token和时间戳

    private static int MAX_COUNT = 2;

    // 登录
    public static String login(String user) {
        String token = "token_" + jedis.incr(TOKEN_GENERATOR);
        jedis.hset(TOKEN, token, user);
        jedis.zadd(RECENT, new Date().getTime() / 1000, token);
        return token;
    }

    // 清理较旧的token，使token数量小于等于MAX_COUNT
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
            List<String> delKeyList = new ArrayList<>();
            for (String token : tokens) {
                delKeyList.add(ItemService.VIEW + token);
                delKeyList.add(CartService.CART + token);
            }
            String[] tokenArr = tokens.toArray(new String[0]);
            jedis.hdel(TOKEN, tokenArr);
            jedis.zrem(RECENT, tokenArr);
            jedis.del(delKeyList.toArray(new String[0]));
        }
    }
}
