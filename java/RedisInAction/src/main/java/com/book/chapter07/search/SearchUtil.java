package com.book.chapter07.search;

import com.JedisFactory;
import redis.clients.jedis.*;

import java.util.*;

// 根据搜索表达式，得到的搜索结果的集合键名(反向索引的文档id集合)
// 解析查询字符串，组装成Query对象
// 对每一组同意词做并集运算，将结果存入新key中，存入列表中
// 对key做交集，满足所有搜索词的搜索结果
// 对key做差集，排除不需要搜索的词
// 最后，对搜索结果进行排序。
// 有一种简单的排序方法：用sort函数根据外部hash值进行排序
// 另一种复杂的排序方式：用带权重（weight）的有序集合运算进行排序（zinterstore）
public class SearchUtil {

    public static void main(String[] args) throws Exception {
        Jedis jedis = JedisFactory.getSingleJedis();
//        String result = parseAndSearch(jedis, "abc -xyz", 50);
//        System.out.println(jedis.smembers("idx:" + result));

        // 简单排序
//        jedis.hset("kb:doc:doc1", "id", "1");
//        jedis.hset("kb:doc:doc1", "updated", "153453453");
//        jedis.hset("kb:doc:doc1", "title是按字母序", "doc1");
//        SearchResult searchResult = searchAndSort(jedis, "abc -xyz", "title");
//        System.out.println(searchResult);

        // 复杂排序
        Map<String, Integer> weightMap = new HashMap<>();
        weightMap.put("update", 2);
        weightMap.put("vote", 3);
        SearchResult searchResult = searchAndZsort(jedis, "abc -xyz", true, weightMap);
        System.out.println(searchResult);
    }

    // 搜索并排序，采用有序集合zinterstore进行带权重的分值运算排序
    public static SearchResult searchAndZsort(
            Jedis conn, String queryString, boolean desc, Map<String,Integer> weights) throws Exception {
        int ttl = 300;
        int start = 0;
        int num = 20;
        String id = parseAndSearch(conn, queryString, ttl);

        int updateWeight = weights.containsKey("update") ? weights.get("update") : 1;
        int voteWeight = weights.containsKey("vote") ? weights.get("vote") : 0;

        String[] keys = new String[]{id, "sort:update", "sort:votes"};
        Transaction trans = conn.multi();
        id = AggregationUtil.zintersect(
                trans, ttl, new ZParams().weights(0, updateWeight, voteWeight), keys);

        trans.zcard("idx:" + id);
        if (desc) {
            trans.zrevrange("idx:" + id, start, start + num - 1);
        }else{
            trans.zrange("idx:" + id, start, start + num - 1);
        }
        List<Object> results = trans.exec();

        return new SearchResult(
                id,
                ((Long)results.get(results.size() - 2)).longValue(),
                // Note: it's a LinkedHashSet, so it's ordered
                new ArrayList<String>((Set<String>)results.get(results.size() - 1)));
    }

    // 搜索并对结果进行排序
    public static SearchResult searchAndSort(Jedis conn, String queryString, String sort) throws Exception {
        // 获取搜索结果
        String id = parseAndSearch(conn, "abc -xyz", 50);

        // 解析排序字符串, -代表为降序，否则为升序
        boolean desc = sort.startsWith("-");
        if (desc){
            sort = sort.substring(1);
        }
        // 可按id，update(时间戳), title排序。title是按字母序，id和update按数字序
        boolean alpha = !"updated".equals(sort) && !"id".equals(sort);
        String by = "kb:doc:*->" + sort;

        // 排序搜索结果
        Transaction trans = conn.multi();
        Response<Long> count = trans.scard("idx:" + id);
        SortingParams sortingParams = new SortingParams();
        if (desc) {
            sortingParams.desc();
        }
        if (alpha) {
            sortingParams.alpha();
        }
        sortingParams.by(by);
        Response<List<String>> res = trans.sort("idx:" + id, sortingParams);
        List<Object> results = trans.exec();

        return new SearchResult(id, count.get(), res.get());
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
