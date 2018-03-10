package com.book.chapter07.job;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import java.util.Set;
import java.util.UUID;

// 职位必须一组技能，求职者具备一组技能
// 用集合运算sdiff计算，该求职者是否拥有该职位所需所有技能
public class NormalSearchJob {

    public static void main(String[] args) throws Exception {
        Jedis conn = JedisFactory.getSingleJedis();
//        addJob(conn, "job1", "skill1", "skill2", "skill3");

//        System.out.println(isQualified(conn, "job1", "skill1", "skill2"));
        System.out.println(isQualified(conn, "job1", "skill1", "skill2", "skill3", "skill4"));
    }

    // 添加job所需技能
    public static void addJob(Jedis conn, String jobId, String... requiredSkills) {
        conn.sadd("job:" + jobId, requiredSkills);
    }

    // 用集合运算获知该求职者的技能是否满足该职位的要求
    public static boolean isQualified(Jedis conn, String jobId, String... candidateSkills)
            throws Exception {
        // 新建一个uuid，作为临时键名，存放求职者的技能集合
        String uuid = UUID.randomUUID().toString();

        // 开启事务，添加技能到这个临时键中
        Transaction trans = conn.multi();
        trans.sadd(uuid, candidateSkills);

        // sdiff操作，job:jobID中是否有剩余技能，是求职者不具备的
        Response<Set<String>> result = trans.sdiff("job:" + jobId, uuid);

        // 结束事务返回结果
        trans.exec();
        trans.close();

        return result.get().size() <= 0;
    }
}
