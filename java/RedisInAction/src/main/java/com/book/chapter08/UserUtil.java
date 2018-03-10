package com.book.chapter08;

import com.JedisFactory;
import com.book.chapter06.lock.SimpleLockUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.HashMap;
import java.util.Map;

// userId生成器user:id:
public class UserUtil {

    public static void main(String[] args) throws Exception {
        Jedis conn = JedisFactory.getSingleJedis();
        UserModel userModel = new UserModel();
        userModel.setLogin("login1");
        userModel.setName("name1");
        createUser(conn, userModel);
    }

    public static long createUser(Jedis conn, UserModel userModel)
            throws Exception {
        // 将账号（login）转换成小写，并且加锁
        String llogin = userModel.getLogin().toLowerCase();
        String lock = SimpleLockUtil.acquire_lock_with_timeout("user:" + llogin, 10, 10);

        // 加锁失败，返回-1
        if (lock == null){
            return -1;
        }

        // 用户名已经存在，返回-1
        if (conn.hget("users:", llogin) != null) {
            return -1;
        }

        // 新建用户hash
        long id = conn.incr("user:id:");
        Transaction trans = conn.multi();
        trans.hset("users:", llogin, String.valueOf(id));
        Map<String,String> values = new HashMap<String,String>();
        values.put("login", userModel.getLogin());
        values.put("id", String.valueOf(id));
        values.put("name", userModel.getName());
        values.put("followers", "0");
        values.put("following", "0");
        values.put("posts", "0");
        values.put("signup", String.valueOf(System.currentTimeMillis()));
        trans.hmset("user:" + id, values);
        trans.exec();
        trans.close();
        SimpleLockUtil.release_lock("user:" + llogin, lock);
        return id;
    }
}
