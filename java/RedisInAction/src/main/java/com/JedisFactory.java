package com;

import redis.clients.jedis.Jedis;

public class JedisFactory {

    static Jedis jedis = new Jedis("127.0.0.1", 6379);

    public static Jedis getJedis() {
        return jedis;
    }
}
