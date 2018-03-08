package com.book.chapter07.search;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.ZParams;

import java.io.IOException;
import java.util.UUID;

// 进行集合运算，包括并集、交集、差集
public class AggregationUtil {

    public static void main(String[] args) throws IOException {
        Jedis conn = JedisFactory.getSingleJedis();
//        Transaction trans = conn.multi();
//        String key = difference(trans, 30, "abc", "efg");
//        trans.exec();
//        trans.close();
//        System.out.println(key);
//        System.out.println(conn.smembers("idx:" + key));

        Transaction trans = conn.multi();
        ZParams params = new ZParams();
        params.weightsByDouble(0, 3, 2);
        String sortResult = zintersect(trans, 30000, params, "s1", "sort:update", "sort:votes");
        trans.exec();
        trans.close();
        System.out.println(sortResult);
    }

    // 有序集合运算（实际上参数也能包含普通集合，分值为1），运算结果做为一个新的临时集合，返回
    private static String zsetCommon(
            Transaction trans, String method, int ttl, ZParams params, String... sets)
    {
        String[] keys = new String[sets.length];
        for (int i = 0; i < sets.length; i++) {
            keys[i] = "idx:" + sets[i];
        }

        String id = UUID.randomUUID().toString();
        switch (method) {
            case "zinterstore":
                trans.zinterstore("idx:" + id, params, keys);
                break;
            case "zunionstore":
                trans.zunionstore("idx:" + id, params, keys);
                break;
            default:break;
        }

        trans.expire("idx:" + id, ttl);
        return id;
    }

    public static String zintersect(
            Transaction trans, int ttl, ZParams params, String... sets) {
        return zsetCommon(trans, "zinterstore", ttl, params, sets);
    }

    // 集合运算，运算结果做为一个新的临时集合，返回
    private static String setCommon(Transaction trans, String method, int ttl, String... items)
    {
        String[] keys = new String[items.length];
        for (int i = 0; i < items.length; i++){
            keys[i] = "idx:" + items[i];
        }

        String id = UUID.randomUUID().toString();
        switch (method) {
            case "sdiffstore":
                trans.sdiffstore("idx:" + id, keys);
                break;
            case "sinterstore":
                trans.sinterstore("idx:" + id, keys);
                break;
            case "sunionstore":
                trans.sunionstore("idx:" + id, keys);
                break;
            default:break;
        }
//        try{
//            trans.getClass()
//                    .getDeclaredMethod(method, String.class, String[].class)
//                    .invoke(trans, "idx:" + id, keys);
//        }catch(Exception e){
//            throw new RuntimeException(e);
//        }
        trans.expire("idx:" + id, ttl);
        return id;
    }

    public static String intersect(Transaction trans, int ttl, String... items) {
        return setCommon(trans, "sinterstore", ttl, items);
    }

    public static String union(Transaction trans, int ttl, String... items) {
        return setCommon(trans, "sunionstore", ttl, items);
    }

    public static String difference(Transaction trans, int ttl, String... items) {
        return setCommon(trans, "sdiffstore", ttl, items);
    }

}
