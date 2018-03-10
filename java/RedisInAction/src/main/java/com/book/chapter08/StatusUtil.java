package com.book.chapter08;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatusUtil {

    public static void main(String[] args) {
        Jedis conn = JedisFactory.getSingleJedis();
        createStatus(conn, 1, "abc");
    }

    public static long createStatus(Jedis conn, long uid, String message) {
        return createStatus(conn, uid, message, null);
    }

    public static long createStatus(
            Jedis conn, long uid, String message, Map<String,String> data) {
        Transaction trans = conn.multi();
        trans.hget("user:" + uid, "login");
        trans.incr("status:id:");
        List<Object> response = trans.exec();

        // 通过uid获取用户登录名称
        String login = (String)response.get(0);
        long id = (Long)response.get(1);

        if (login == null) {
            return -1;
        }

        // 将数据插入到hash中
        if (data == null){
            data = new HashMap<String,String>();
        }
        data.put("message", message);
        data.put("posted", String.valueOf(System.currentTimeMillis()));
        data.put("id", String.valueOf(id));
        data.put("uid", String.valueOf(uid));
        data.put("login", login);

        trans = conn.multi();
        trans.hmset("status:" + id, data);
        trans.hincrBy("user:" + uid, "posts", 1);
        trans.exec();
        return id;
    }

}
