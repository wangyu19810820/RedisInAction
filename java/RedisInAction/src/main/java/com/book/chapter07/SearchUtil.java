package com.book.chapter07;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.ArrayList;
import java.util.List;

// 根据搜索表达式，得到的搜索结果的集合键名(反向索引的文档id集合)
// 解析查询字符串，组装成Query对象
// 对每一组同意词做并集运算，将结果存入新key中，存入列表中
// 对key做交集，满足所有搜索词的搜索结果
// 对key做差集，排除不需要搜索的词
public class SearchUtil {

    public static void main(String[] args) throws Exception {
        Jedis jedis = JedisFactory.getSingleJedis();
        String result = parseAndSearch(jedis, "abc -xyz", 50);
        System.out.println(jedis.smembers("idx:" + result));

    }

    // 根据搜索表达式，得到的搜索结果的集合键名
    public static String parseAndSearch(Jedis conn, String queryString, int ttl) throws Exception {
        // 解析查询字符串，组装成Query对象
        Query query = QueryUtil.parse(queryString);

        // 对每一组同意词做并集运算，将结果存入新key中，存入列表中
        List<String> keyList = new ArrayList<>();
        for (List<String>keys : query.all) {
            if (keys.size() <= 0) {
                continue;
            } else if (keys.size() == 1) {
                keyList.add(keys.get(0));
            } else {
                Transaction trans = conn.multi();
                keyList.add(AggregationUtil.union(trans, 30, keys.toArray(new String[keys.size()])));
                trans.exec();
                trans.close();
            }
        }

        // 对key做交集，满足所有搜索词
        String result = "";
        if (keyList.size() <= 0) {
            return null;
        } else if (keyList.size() == 1) {
            result = keyList.get(0);
        } else {
            Transaction trans = conn.multi();
            result = AggregationUtil.intersect(trans, 30, keyList.toArray(new String[keyList.size()]));
            trans.exec();
            trans.close();
        }

        // 对key做差集，排除不需要搜索的词
        if (query.unwanted != null && query.unwanted.size() > 0) {
            List<String> diffKeys = new ArrayList<>(query.unwanted);
            diffKeys.add(0, result);
            Transaction trans = conn.multi();
            result = AggregationUtil.difference(trans, 30, diffKeys.toArray(new String[diffKeys.size()]));
            trans.exec();
            trans.close();
        }

        return result;
    }
}
