package com.book.chapter02;

public class WebDemo {

    public static void main(String[] args) {
//        newUser();
//        startClean();

//        viewItem();
//        addCartItem();

//        String content = PageCacheService.getCachePage("url_1");
//        System.out.println(content);

        DataCacheService.schedule(1, 5);
        DataCacheService.cache();
    }

    private static void newUser() {
        String userId = UserService.newUser();
        LoginService.login(userId);
    }

    private static void startClean() {
        LoginService.clean();
    }

    private static void viewItem() {
        ItemService.view("token_1", ItemService.itemList.get(1));
    }

    private static void addCartItem() {
        CartService.addCart("token_1", ItemService.itemList.get(0), 1);
    }
}
