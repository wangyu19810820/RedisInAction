package com.book.chapter02;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

public class PageCacheService {

    public static final String CACHE = "cache:";
    private static Jedis jedis = JedisFactory.getSingleJedis();
    private static final int MAX_CACHE_COUNT = 1000;

    public static String getCachePage(String url) {
        if (!canCache(url)) {
            return generatorPageContent(url);
        }
        String key = CACHE + url;
        String content = jedis.get(key);
        if (content != null) {
            return content;
        } else {
            content = generatorPageContent(url);
            jedis.setex(key, 300, content);
            return content;
        }
    }

    // 该页面是否能缓存, 该页面的浏览频率较高
    public static boolean canCache(String url) {
        long rank = jedis.zrank(ItemService.VIEW, url);
        return rank <= MAX_CACHE_COUNT;
    }

    // 生成页面内容
    public static String generatorPageContent(String url) {
        return "page content:" + url;
    }


}
