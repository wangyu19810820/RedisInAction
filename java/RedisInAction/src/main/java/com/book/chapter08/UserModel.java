package com.book.chapter08;

public class UserModel {

    private int id;
    private String login;
    private String name;
    private int followers;
    private int folowing;
    private int posts;
    private int signup;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFolowing() {
        return folowing;
    }

    public void setFolowing(int folowing) {
        this.folowing = folowing;
    }

    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }

    public int getSignup() {
        return signup;
    }

    public void setSignup(int signup) {
        this.signup = signup;
    }
}
