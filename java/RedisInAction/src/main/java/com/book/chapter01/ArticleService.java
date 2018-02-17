package com.book.chapter01;

import com.JedisFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

// 文章的Service
public class ArticleService {

    public final static String ARTICLE_TIME_KEY = "time:";
    public final static String ARTICLE_SCORE_KEY = "score:";

    private static Jedis jedis = JedisFactory.getSingleJedis();
    private final static String ARTICLE_KEY = "article:";
    private final static String ARTICLE_VOTE_KEY_PREFIX = "vote:";

    public static void post(ArticleModel articleModel, String poster) {
        double now = new Date().getTime() / 1000.0;
        articleModel.setPoster(poster);
        articleModel.setTime(now);
        articleModel.setVotes(1L);

        // 新增Article的id
        String articleID = ARTICLE_KEY + jedis.incr(ARTICLE_KEY);
        // 保存文章，键为自增的articleID
        jedis.hmset(articleID, articleModel.toMap());
        // 新增article的投票用户集合，设置超时为一周
        jedis.sadd(ARTICLE_VOTE_KEY_PREFIX + articleID, poster);
        jedis.expire(ARTICLE_VOTE_KEY_PREFIX + articleID, Constants.ONE_WEEK_IN_SECONDS);
        // article的时间戳集合中添加一个记录
        jedis.zadd(ARTICLE_TIME_KEY, now, articleID);
        // article的得分集合中增加一个记录, 分值为当前时间秒数+单个投票分值
        jedis.zadd(ARTICLE_SCORE_KEY, now + Constants.VOTE_SCORE, articleID);
    }

    public static List<ArticleKeyModel> getArticleList(String key) {
        List<ArticleKeyModel> list = new ArrayList<>();
        Set<String> articleKeySet = jedis.zrevrange(key, 0, -1);
        for (String key1 : articleKeySet) {
            ArticleModel articleModel = ArticleModel.fromMap(jedis.hgetAll(key1));
            list.add(new ArticleKeyModel(key, articleModel));
        }
        return list;
    }

    public static String vote(String articleID, String userID) {
        // 判断文章的发布日期是否在一周内
        double publishTime = jedis.zscore(ARTICLE_TIME_KEY, articleID);
        if (publishTime < new Date().getTime() / 1000.0 - Constants.ONE_WEEK_IN_SECONDS) {
            return "文章发表已经超过一周，投票已经被关闭";
        }

        // 判断用户是否已经投过票
        if (jedis.sismember(ARTICLE_VOTE_KEY_PREFIX + articleID, userID)) {
            return "该用户已经投过票了";
        }

        // 更新article得分集合的记录
        jedis.zincrby(ARTICLE_SCORE_KEY, Constants.VOTE_SCORE, articleID);

        // article散列的投票数增长1
        jedis.hincrBy(articleID, "votes", 1);

        // article的投票用户集合增加一个记录
        jedis.sadd(ARTICLE_VOTE_KEY_PREFIX + articleID, articleID);

        return "投票成功";
    }
}
