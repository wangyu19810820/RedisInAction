package com.book.chapter07.advice;

import com.book.chapter07.search.ParseDocument;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// 为广告建立反向索引，包括地区索引idx:req:locationID、内容索引idx:word
// 在hash中存储广告的类型信息type:
// 将广告的统一价格和原始价格放入到有序集合中idx:ad:value:和ad:base_value:
// 为term:adID建立该广告所有内容关键词集合terms:adID
public class IndexAd {

    // 千次浏览转换率，一千次浏览的点击率，或者一千次浏览动作(action)率
    // 用于统一计费，将按点击计费，按动作计费，统一成按浏览计费

    public static void indexAd(
            Jedis conn, String id, String[] locations,
            String content, PriceType type, double value) {
        Transaction trans = conn.multi();

        // 建立地区索引
        for (String location : locations) {
            trans.sadd("idx:req:" + location, id);
        }

        // 建立内容索引
        Set<String> words = ParseDocument.tokenize(content);
        for (String word : ParseDocument.tokenize(content)) {
            trans.zadd("idx:" + word, 0, id);
        }

        // 在hash中存储广告的类型信息
        trans.hset("type:", id, type.name().toLowerCase());

        // 计算广告的统一价格
        double avg = PriceUtil.AVERAGE_PER_1K.containsKey(type) ? PriceUtil.AVERAGE_PER_1K.get(type) : 1;
        double rvalue = PriceUtil.toEcpm(type, 1000, avg, value);
        // 将统一价格和原始价格放入有序集合中
        trans.zadd("idx:ad:value:", rvalue, id);
        trans.zadd("ad:base_value:", value, id);

        // 广告包括哪些关键词
        for (String word : words){
            trans.sadd("terms:" + id, word);
        }
        trans.exec();
    }
}
