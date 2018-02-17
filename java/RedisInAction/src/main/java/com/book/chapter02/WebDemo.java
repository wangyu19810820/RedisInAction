package com.book.chapter02;

public class WebDemo {

    public static void main(String[] args) {
        newUser();
        startClean();
    }

    private static void newUser() {
        String userId = UserService.newUser();
        LoginService.login(userId);
    }

    private static void startClean() {
        LoginService.clean();
    }
}
