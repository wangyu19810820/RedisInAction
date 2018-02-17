package com.book.chapter01;

public class ArticleKeyModel {

    private String key;
    private ArticleModel model;

    public ArticleKeyModel(String key, ArticleModel model) {
        this.key = key;
        this.model = model;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ArticleModel getModel() {
        return model;
    }

    public void setModel(ArticleModel model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return "ArticleKeyModel{" +
                "key='" + key + '\'' +
                ", model=" + model +
                '}';
    }
}
