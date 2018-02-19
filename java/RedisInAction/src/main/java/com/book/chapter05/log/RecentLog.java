package com.book.chapter05.log;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

public class RecentLog {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws Exception {
        logRecent(LogName.SYS, "admin login", Severity.INFO);
        logRecent(LogName.BP, "a new bid", Severity.DEBUG);
    }

    // 将日志记录到不同的分类中，并保留最新的1000条
    // 分类为：名称_级别
    public static void logRecent(String name, String message, String level) throws Exception {
        String type = name + "_" + level;
        Pipeline pipeline = jedis.pipelined();
        pipeline.multi();
        pipeline.lpush(type, message);
        pipeline.ltrim(type, 0, 999);
        pipeline.exec();
        pipeline.close();
    }
}
