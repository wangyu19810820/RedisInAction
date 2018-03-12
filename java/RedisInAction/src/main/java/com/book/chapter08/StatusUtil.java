package com.book.chapter08;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import java.lang.reflect.Method;
import java.util.*;

// 有两条时间轴对应两个有序集合，成员是消息id，分值是时间戳
// 一条是主时间轴(home:userID)，包含自己发布的消息和关注者发布的消息
// 另一条是自己时间轴(profile:userID)，只包含自己发布的消息
public class StatusUtil {

    private static int HOME_TIMELINE_SIZE = 1000;
    private static int POSTS_PER_PASS = 1000;

    public static void main(String[] args) {
        Jedis conn = JedisFactory.getSingleJedis();
//        createStatus(conn, 1, "abc");

        test();
    }

    public static void test() {
        Jedis conn = JedisFactory.getSingleJedis();
        Set<Tuple> followers = conn.zrangeByScoreWithScores(
                "z1",
                "-inf", "inf",
                0, 1000);
        followers.forEach(System.out::println);
    }

    // 发布消息
    public long postStatus(Jedis conn, long uid, String message) {
        return postStatus(conn, uid, message, null);
    }

    public long postStatus(Jedis conn, long uid, String message, Map<String,String> data)
    {
        long id = createStatus(conn, uid, message, data);
        if (id == -1){
            return -1;
        }

        // 从hash中取出发布时间
        String postedString = conn.hget("status:" + id, "posted");
        if (postedString == null) {
            return -1;
        }
        // 将消息加入到我的时间线中
        long posted = Long.parseLong(postedString);
        conn.zadd("profile:" + uid, posted, String.valueOf(id));

        // 将消息加入到粉丝时间线中
        syndicateStatus(conn, uid, id, posted, 0);
        return id;
    }

    public void syndicateStatus(Jedis conn, long uid, long postId, long postTime, double start) {
        Set<Tuple> followers = conn.zrangeByScoreWithScores(
                "followers:" + uid,
                String.valueOf(start), "inf",
                0, POSTS_PER_PASS);

        Transaction trans = conn.multi();
        for (Tuple tuple : followers){
            String follower = tuple.getElement();
            start = tuple.getScore();
            trans.zadd("home:" + follower, postTime, String.valueOf(postId));
            trans.zrange("home:" + follower, 0, -1);
            trans.zremrangeByRank(
                    "home:" + follower, 0, 0 - HOME_TIMELINE_SIZE - 1);
        }
        trans.exec();

        if (followers.size() >= POSTS_PER_PASS) {
            try{
                Method method = getClass().getDeclaredMethod(
                        "syndicateStatus", Jedis.class, Long.TYPE, Long.TYPE, Long.TYPE, Double.TYPE);
                executeLater("default", method, uid, postId, postTime, start);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
    }

    public void executeLater(String queue, Method method, Object... args) {
        MethodThread thread = new MethodThread(this, method, args);
        thread.start();
    }

    // 获取主时间轴上的消息
    public List<Map<String,String>> getStatusMessages(Jedis conn, long uid)
            throws Exception{
        return getStatusMessages(conn, uid, 1, 30);
    }

    public List<Map<String,String>> getStatusMessages(Jedis conn, long uid, int page, int count)
            throws Exception {
        Set<String> statusIds = conn.zrevrange(
                "home:" + uid, (page - 1) * count, page * count - 1);

        Transaction trans = conn.multi();
        for (String id : statusIds) {
            trans.hgetAll("status:" + id);
        }

        List<Map<String,String>> statuses = new ArrayList<Map<String,String>>();
        for (Object result : trans.exec()) {
            Map<String,String> status = (Map<String,String>)result;
            if (status != null && status.size() > 0){
                statuses.add(status);
            }
        }
        trans.close();
        return statuses;
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
