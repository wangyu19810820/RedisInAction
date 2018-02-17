package com.book.chapter01;

import java.util.HashMap;
import java.util.Map;

// 文章的模型类
public class ArticleModel {

    private String title;
    private String link;
    private String poster;
    private double time;
    private long votes;

    public ArticleModel() {
    }

    public ArticleModel(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("title", title);
        map.put("link", link);
        map.put("poster", poster);
        map.put("time", String.valueOf(time));
        map.put("votes", String.valueOf(votes));
        return map;
    }

    public static ArticleModel fromMap(Map<String, String> map) {
        ArticleModel model = new ArticleModel();
        model.title = map.get("title");
        model.link = map.get("link");
        model.poster = map.get("poster");
        model.time = Double.valueOf(map.get("time"));
        model.votes = Long.parseLong(map.get("votes"));
        return model;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public long getVotes() {
        return votes;
    }

    public void setVotes(long votes) {
        this.votes = votes;
    }

    @Override
    public String toString() {
        return "ArticleModel{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", poster='" + poster + '\'' +
                ", time=" + time +
                ", votes=" + votes +
                '}';
    }
}
