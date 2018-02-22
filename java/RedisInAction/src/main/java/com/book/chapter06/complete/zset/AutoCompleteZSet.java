package com.book.chapter06.complete.zset;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AutoCompleteZSet {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws Exception  {
//        System.out.println(Arrays.toString(findPrefixRange("aaa")));

//        jedis.zadd("members:m1", 0, "aa");
//        jedis.zadd("members:m1", 0, "ab");
//        jedis.zadd("members:m1", 0, "abb");
//        jedis.zadd("members:m1", 0, "abc");
//        jedis.zadd("members:m1", 0, "ac");
//        jedis.zadd("members:m1", 0, "ad");

        Set<String> result = autocompleteOnPrefix("m1", "ab");
        result.forEach(System.out::println);
    }

    private static final String VALID_CHARACTERS = "`abcdefghijklmnopqrstuvwxyz{";

    // 查找指定单词的前一个单词，后一个单词，用以搜索。如输入abb,返回aba{和abb{，这样就能匹配所有abb*的数据了
    public static String[] findPrefixRange(String prefix) {
        int posn = VALID_CHARACTERS.indexOf(prefix.charAt(prefix.length() - 1));
        char suffix = VALID_CHARACTERS.charAt(posn > 0 ? posn - 1 : 0);
        String start = prefix.substring(0, prefix.length() - 1) + suffix + '{';
        String end = prefix + '{';
        return new String[]{start, end};
    }

    // 先找到搜索单词的前导，后续单词。添加唯一标识位，插入有序集合中，找到起始排名，取符合条件前10位
    // 开启事务，删除前导，后续单词。取出符合条件的单词。
    public static Set<String> autocompleteOnPrefix(String guild, String prefix) throws Exception {
        String[] range = findPrefixRange(prefix);
        String start = range[0];
        String end = range[1];
        String uuid = UUID.randomUUID().toString();
        start += uuid;
        end += uuid;
        String key = "members:" + guild;
        jedis.zadd(key, 0, start);
        jedis.zadd(key, 0, end);
        while (true) {
            jedis.watch(key);
            long startRank = jedis.zrank(key, start);
            long endRank = jedis.zrank(key, end);
            long scope = Math.min(startRank + 9, endRank - 2);
            Transaction transaction = jedis.multi();
            transaction.zrem(key, start, end);
            transaction.zrange(key, startRank, scope);
            List<Object> list = transaction.exec();
            if (list != null) {
                transaction.close();
                return (Set<String>)list.get(list.size() - 1);
            }
        }
    }
}
