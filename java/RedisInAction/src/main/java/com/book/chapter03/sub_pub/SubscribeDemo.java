package com.book.chapter03.sub_pub;

import redis.clients.jedis.Jedis;

/**
 * 发布/订阅的例子
 * 订阅并接收消息的Demo，配合SubscribeDemo一起运行
 * 退订在Subscriber中执行
 */
public class SubscribeDemo {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.subscribe(new Subscriber(), "channel");
        System.out.println("subscribe success");
        jedis.close();
    }
}
