package com.book.chapter04.benchmark;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

// 和redis自带的性能测试工具redis-benchmark.exe做对比
// 如果性能在50%-60%算正常情况，否则需排查原因
public class BenchmarkDemo {

    static Jedis jedis = JedisFactory.getSingleJedis();
//    static Jedis jedis = new Jedis("192.168.211.130", 6379, 3000);

    public static void main(String[] args) {
//        jedis.flushAll();
        int count = 0;
        long end = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < end) {
//            jedis.set("a" + count, "1" + count);
            jedis.get("a" + count);
            count++;
        }
        System.out.println(count);
    }
}
