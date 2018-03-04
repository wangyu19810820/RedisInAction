package com.book.chapter07;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.UUID;

// 进行集合运算，包括并集、交集、差集
public class AggregationUtil {

    public static void main(String[] args) throws IOException {
        Jedis conn = JedisFactory.getSingleJedis();
        Transaction trans = conn.multi();
        String key = difference(trans, 30, "abc", "efg");
        trans.exec();
        trans.close();
        System.out.println(key);
        System.out.println(conn.smembers("idx:" + key));
    }

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
