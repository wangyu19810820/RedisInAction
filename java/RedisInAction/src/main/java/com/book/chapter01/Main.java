package com.book.chapter01;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {
//        long userID = UserService.newUser();
//        post(1, userID);
//        post(2, userID);
//        post(3, userID);

//        String str = ArticleService.vote("article:1", "user:" + userID);
//        System.out.println(str);
//        showArticle(ArticleService.ARTICLE_TIME_KEY);

//        showArticle(ArticleService.ARTICLE_SCORE_KEY);

        GroupService.add_remove_groups("article:1",
                Arrays.asList(GroupService.GROUP_ART),
                Arrays.asList(GroupService.GROUP_SPORT));
        GroupService.add_remove_groups("article:2",
                Arrays.asList(GroupService.GROUP_ART),
                Arrays.asList(GroupService.GROUP_SPORT));
        GroupService.add_remove_groups("article:3",
                Arrays.asList(GroupService.GROUP_SPORT),
                Arrays.asList(GroupService.GROUP_ART));
        showGroupArticle(GroupService.GROUP_ART, ArticleService.ARTICLE_TIME_KEY);
        System.out.println("--------------------------------------------------");
        showGroupArticle(GroupService.GROUP_ART, ArticleService.ARTICLE_SCORE_KEY);
        System.out.println("--------------------------------------------------");
        showGroupArticle(GroupService.GROUP_SPORT, ArticleService.ARTICLE_TIME_KEY);
    }

    public static void post(int suffix, long userID) {
        ArticleModel articleModel = new ArticleModel();
        articleModel.setTitle("new article " + suffix);
        articleModel.setLink("http://www.aaaa.com/xxxx" + suffix);
        ArticleService.post(articleModel, UserService.USER_KEY + userID);
    }

    public static void showArticle(String sort) {
        List<ArticleKeyModel> list = ArticleService.getArticleList(sort);
        list.forEach(System.out::println);
    }

    public static void showGroupArticle(String group, String order) {
        List<ArticleKeyModel> list = GroupService.groupArticleList(group, order);
        list.forEach(System.out::println);
    }
}
