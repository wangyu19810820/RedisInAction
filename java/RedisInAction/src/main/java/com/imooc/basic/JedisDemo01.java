package com.imooc.basic;

import redis.clients.jedis.Jedis;

public class JedisDemo01 {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.set("name", "abc");
        String name = jedis.get("name");
        System.out.println(name);
        jedis.close();
    }
}
