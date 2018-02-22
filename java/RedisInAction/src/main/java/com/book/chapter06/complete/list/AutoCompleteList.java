package com.book.chapter06.complete.list;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;

public class AutoCompleteList {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws Exception {
        addUpdateContact("user1", "aaa");
        addUpdateContact("user1", "abc");
        addUpdateContact("user1", "zzz");
        List<String> result = fetchAutoComplete("user1", "a");
        result.forEach(System.out::println);
    }

    public static void addUpdateContact(String user, String contact) throws Exception {
        String key = "recent:" + user;
        Transaction trans = jedis.multi();

        trans.lrem(key, 1, contact);
        trans.lpush(key, contact);
        trans.ltrim(key, 0, 99);

        trans.exec();
        trans.close();
    }

    public static void removeContact(String user, String contact) {
        jedis.lrem("recent:" + user, 0, contact);
    }

    public static List<String> fetchAutoComplete(String user, String prefix) {
        List<String> candidates = jedis.lrange("recent:" + user, 0, -1);
        List<String> result = new ArrayList<>();
        for (String value : candidates) {
            if (value.startsWith(prefix)) {
                result.add(value);
            }
        }
        return result;
    }
}
