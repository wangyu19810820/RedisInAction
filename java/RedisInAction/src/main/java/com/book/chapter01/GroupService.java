package com.book.chapter01;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ZParams;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GroupService {

    public static final String GROUP_SPORT = "group:sport";
    public static final String GROUP_ART = "group:art";

    private static Jedis jedis = JedisFactory.getSingleJedis();

    public static void add_remove_groups(String articleID, List<String> addGroupList, List<String> removeList) {
        for (String group : addGroupList) {
            jedis.sadd(group, articleID);
        }
        for (String group : removeList) {
            jedis.srem(group, articleID);
        }
    }

    public static List<ArticleKeyModel> groupArticleList(String group, String order) {
        String key = group + ":" + order;
        if (!jedis.exists(key)) {
            ZParams zParams = new ZParams();
            zParams.aggregate(ZParams.Aggregate.MAX);
            jedis.zinterstore(key, zParams, group, order);
            jedis.expire(key, 60);
        }
        return ArticleService.getArticleList(key);
    }
}
