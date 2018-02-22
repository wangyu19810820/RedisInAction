package com.book.chapter06.lock;

import com.JedisFactory;
import com.book.chapter04.transaction.MarketDemo;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

public class MarketUsingSimpleLock {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) {
        jedis.flushAll();
        MarketDemo.init();
        MarketDemo.testSell();
        purchase_item_with_lock("2", "1", "item1");
    }

    // 获取锁，判断商品价格是否小于用户的现有资金，将商品转到购买者背包，将资金转到贩卖者钱包，
    // 再释放锁
    public static boolean purchase_item_with_lock(String buyer, String seller, String item) {
        String buyerKey = "user:" + buyer;
        String sellerKey = "user:" + seller;
        String itemKey = item + "." + seller;
        String inventory = "inventory:" + buyer;

        String identifier = SimpleLockUtil.acquire_lock("market", 10);
        if (identifier == null) {
            return false;
        }

        try {
            double price = jedis.zscore("market:", itemKey);
            double funds = Double.parseDouble(jedis.hget(sellerKey, "funds"));
            if (price > funds) {
                return false;
            }
            Transaction trans = jedis.multi();
            trans.hincrByFloat(sellerKey, "funds", price);
            trans.hincrByFloat(buyerKey, "funds", -price);
            trans.sadd(inventory, item);
            trans.zrem("market:", itemKey);
            List<Object> list = trans.exec();
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        } finally {
            SimpleLockUtil.release_lock("market", identifier);
        }
    }
}
