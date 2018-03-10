package com.book.chapter07.job;

import com.JedisFactory;
import com.book.chapter07.search.AggregationUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.ZParams;

import java.util.Set;

// 反向索引idx:skill:skillID记录该技能包含的职位
// 有序集合idx:jobs:req记录该职位所需技能数量
public class IndexSearchJob {

    public static void main(String[] args) throws Exception {
        Jedis conn = JedisFactory.getSingleJedis();
        indexJob(conn, "job1", "skill1", "skill2", "skill3");
        indexJob(conn, "job2", "skill3", "skill4");

        Set<String> jobs = findJobs(conn, "skill1", "skill2", "skill3");
        jobs.forEach(System.out::println);
    }

    // 建立jobID和skill之间的反向索引
    public static void indexJob(Jedis conn, String jobID, String... skills)
            throws Exception {
        Transaction trans = conn.multi();
        for (String skill : skills) {
            trans.sadd("idx:skill:" + skill, jobID);
        }
        trans.zadd("idx:jobs:req", skills.length, jobID);
        trans.exec();
        trans.close();
    }

    // 查找符合条件的职位
    public static Set<String> findJobs(Jedis conn, String... candidateSkills)
            throws Exception {
        // 技能名和反向索引的键名对应起来，权重都为1
        String[] skills = new String[candidateSkills.length];
        int[] weights = new int[candidateSkills.length];
        for (int i = 0; i < candidateSkills.length; i++) {
            skills[i] = "skill:" + candidateSkills[i];
            weights[i] = 1;
        }
        ZParams zParams = new ZParams();
        zParams.weights(weights);

        Transaction trans = conn.multi();
        // 各个反向索引的并集运算，权重为1。生成一个临时有序集合，成员是jobID，分值是匹配到的次数
        String jobScores = AggregationUtil.zunion(trans, 3000, zParams, skills);
        // 临时有序集合和idx:jobs:req做交集运算，权重-1,1。
        // 最后生成的临时集合，分值为0的成员是技能完全匹配职位需要的职位
        // 分值是正数的成员，是只拥有该职位部分技能
        ZParams zParams1 = new ZParams();
        zParams1.weights(-1, 1);
        String result = AggregationUtil.zintersect(trans, 3000, zParams1, jobScores, "jobs:req");

        trans.exec();
        trans.close();

        System.out.println("idx:" + jobScores);
        System.out.println("idx:" + result);
        return conn.zrangeByScore("idx:" + result, 0, 0);
    }
}
