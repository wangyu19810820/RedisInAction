package com.book.chapter03.sub_pub;

import redis.clients.jedis.Jedis;

/**
 * 发布/订阅的例子
 * 发布消息的Demo，配合SubscribeDemo一起运行
 */
public class PublishDemo {

    public static void main(String[] args) {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        for (int i = 0; i < 25; i++) {
            jedis.publish("channel", "message" + i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        jedis.close();
    }
}
