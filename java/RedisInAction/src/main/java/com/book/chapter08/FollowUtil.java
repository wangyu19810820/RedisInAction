package com.book.chapter08;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import java.util.List;
import java.util.Set;

// 用户粉丝有序集合followers:userID，成员userID，分值时间戳
// 用户偶像列表following:userID，成员userID，分值时间戳
// 哈希user:userID缓存粉丝数量和偶像数量，字段following存储偶像数量，字段followers存储粉丝数量
public class FollowUtil {

    private static int HOME_TIMELINE_SIZE = 1000;

    // uid关注otherUid
    public boolean followUser(Jedis conn, long uid, long otherUid) {
        String fkey1 = "following:" + uid;
        String fkey2 = "followers:" + otherUid;

        if (conn.zscore(fkey1, String.valueOf(otherUid)) != null) {
            return false;
        }

        long now = System.currentTimeMillis();

        Transaction trans = conn.multi();
        trans.zadd(fkey1, now, String.valueOf(otherUid));
        trans.zadd(fkey2, now, String.valueOf(uid));
        trans.zcard(fkey1);
        trans.zcard(fkey2);
        trans.zrevrangeWithScores("profile:" + otherUid, 0, HOME_TIMELINE_SIZE - 1);

        List<Object> response = trans.exec();
        long following = (Long)response.get(response.size() - 3);
        long followers = (Long)response.get(response.size() - 2);
        Set<Tuple> statuses = (Set<Tuple>)response.get(response.size() - 1);

        trans = conn.multi();
        // 修改hash
        trans.hset("user:" + uid, "following", String.valueOf(following));
        trans.hset("user:" + otherUid, "followers", String.valueOf(followers));
        if (statuses.size() > 0) {
            // 将偶像的发布的消息，搬到自己的消息列表中
            for (Tuple status : statuses){
                trans.zadd("home:" + uid, status.getScore(), status.getElement());
            }
        }
        // 去除多余的消息，只保留最新的1000条消息
        trans.zremrangeByRank("home:" + uid, 0, 0 - HOME_TIMELINE_SIZE - 1);
        trans.exec();

        return true;
    }

    // uid取消关注otherUid
    public boolean unfollowUser(Jedis conn, long uid, long otherUid) {
        String fkey1 = "following:" + uid;
        String fkey2 = "followers:" + otherUid;

        if (conn.zscore(fkey1, String.valueOf(otherUid)) == null) {
            return false;
        }

        Transaction trans = conn.multi();
        trans.zrem(fkey1, String.valueOf(otherUid));
        trans.zrem(fkey2, String.valueOf(uid));
        trans.zcard(fkey1);
        trans.zcard(fkey2);
        trans.zrevrange("profile:" + otherUid, 0, HOME_TIMELINE_SIZE - 1);

        List<Object> response = trans.exec();
        long following = (Long)response.get(response.size() - 3);
        long followers = (Long)response.get(response.size() - 2);
        Set<String> statuses = (Set<String>)response.get(response.size() - 1);

        trans = conn.multi();
        trans.hset("user:" + uid, "following", String.valueOf(following));
        trans.hset("user:" + otherUid, "followers", String.valueOf(followers));
        if (statuses.size() > 0){
            for (String status : statuses) {
                trans.zrem("home:" + uid, status);
            }
        }

        trans.exec();
        return true;
    }
}
