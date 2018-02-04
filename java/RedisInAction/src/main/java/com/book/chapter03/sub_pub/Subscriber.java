package com.book.chapter03.sub_pub;

import redis.clients.jedis.JedisPubSub;

/**
 * 发布/订阅的例子
 * SubscribeDemo中被调用，收到一定数量的消息后，退订频道
 */
public class Subscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        System.out.println("receive:" + message);
        if (message.endsWith("20")) {
            this.unsubscribe();
        }
    }
}
