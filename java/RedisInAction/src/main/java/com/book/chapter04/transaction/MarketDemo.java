package com.book.chapter04.transaction;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

// 将商品投放到市场和从市场上购买商品来演示java中的事务
// 用户hash，键为users:17，field有name，funds
// 某用户包裹set,键为inventory:17，值有item1，item2...
// 商场有序集合，键为market:，分值为price，值为itemid.userid
public class MarketDemo {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) {
        testSell();
    }

    public static void testSell() {
        String userID = "1";
//        jedis.sadd("inventory:" + userID, "item1", "item2");
        sell("item1", userID, 100);
    }

    // 将商品放入市场
    public static boolean sell(String itemID, String sellerID, double price) {
        // 用户包裹set的键
        String inventory = "inventory:" + sellerID;
        // 商场内的商品成员
        String item = itemID + "." + sellerID;
        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            // 监控用户包裹
            jedis.watch(inventory);
            if (!jedis.sismember(inventory, itemID)) {
                // 包裹内的物品没有了，交易失败，退出
                jedis.unwatch();
                return false;
            }
            Transaction trans = jedis.multi();
            trans.zadd("market:", price, item);
            trans.srem(inventory, itemID);
            List<Object> list = trans.exec();
            if (list == null) {
                // trans.exec()返回null，代表在执行过程中，监视的对象发生了变化
                continue;
            } else {
                return true;
            }
        }

        return false;
    }

    // 购买商品，将商品从市场上撤下，添加到购买者背包
    // 购买者扣除价格，贩卖者增加相等的金钱
    public static boolean buy(String buyerID, String sellerID, String itemID, double price) {
        // 用户包裹set的键
        String inventory = "inventory:" + buyerID;
        // 商场内的商品成员
        String item = itemID + "." + sellerID;
        // 购买者信息hash的key
        String buyer = "user:" + buyerID;
        // 贩卖者信息hash的key
        String seller = "seller:" + sellerID;

        long end = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < end) {
            jedis.watch("market:", buyer);
            // 市场上仍有商品，购买者有足够多的金钱
            double itemPrice = jedis.zscore("market:", item);
            double userFunds = Double.parseDouble(jedis.hget(buyer, "funds"));
            if (itemPrice != price && userFunds > price) {
                jedis.unwatch();
                return false;
            }

            // 购买商品，将商品从市场上撤下，添加到购买者背包
            // 购买者扣除价格，贩卖者增加相等的金钱
            Transaction trans = jedis.multi();

            List<Object> list = trans.exec();
            if (list == null) {
                // trans.exec()返回null，代表在执行过程中，监视的对象发生了变化
                continue;
            } else {
                return true;
            }
        }
        return false;
    }
}
