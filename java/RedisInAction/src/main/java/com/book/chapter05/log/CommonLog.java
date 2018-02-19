package com.book.chapter05.log;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class CommonLog {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws Exception {
        logCommon(LogName.SYS, "admin login", Severity.INFO);
        logCommon(LogName.BP, "a new bid", Severity.DEBUG);
        logCommon(LogName.SYS, "admin login", Severity.INFO);
    }

    // 记录到一个有序集合（键为common_name_level）中，如果重复则对分值+1
    // 如果这次记录的时间点是一个新的小时的开端，则将数据转移到common_name_level_last中,这样保证common_name_level中记录的是最近1小时的数据
    // 用事务，控制并发，避免转移上一个小时数据的时候出错
    public static boolean logCommon(String name, String message, String level) throws Exception {
        String logKeyName = "common_" + name + "_" + level;
        String logHourKey = logKeyName + "_start";
        double end = System.currentTimeMillis() + 5000;
        while(System.currentTimeMillis() < end) {
            String logHourValue = jedis.get(logHourKey);    // 上次清理的时间段
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:00:00");
            String hourDate = isoFormat.format(new Date());

            jedis.watch(logHourKey);
            Transaction trans = jedis.multi();

            if (logHourValue != null && Collator.getInstance().compare(hourDate, logHourValue) > 0) {
                trans.rename(logKeyName, logKeyName + "_last");
                trans.rename(logHourKey, logHourKey + "_last");
            } else if (logHourValue != null) {
                trans.set(logHourKey, hourDate);
            }

            // 记录日志
            trans.zincrby(logKeyName, 1, message);

            List<Object> list = trans.exec();
            if (list == null) {
                continue;
            } else {
                trans.close();
                return true;
            }
        }
        return false;
    }
}
