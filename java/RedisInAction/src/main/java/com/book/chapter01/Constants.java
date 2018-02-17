package com.book.chapter01;

public class Constants {
    // 一星期的秒数
    public static final int ONE_WEEK_IN_SECONDS = 7 * 86400;

    // 单个投票的分值，文章总分值是发布时间的秒数+投票总分值
    // 初始的，后一天的文章比前一天的文章多86400分，也就是多20个投票
    // 一篇文章每天需要20个投票，才能维持热度
    public static final int VOTE_SCORE = 86400 / 20;
}
