package com.book.chapter03;

import redis.clients.jedis.BitOP;
import redis.clients.jedis.Jedis;

/**
 * Redis的基本类型操作，操作的时候自动识别是否是整数，浮点，字符串
 */
public class StringDemo {

    static Jedis jedis = new Jedis("127.0.0.1", 6379);

    public static void main(String[] args) {
        basic();
//        num();
//        str();
//        bit();
        jedis.close();
    }

    // set设置（新增或修改），get获取，expire设置超时时间（单位为秒）,exists是否存在，del删除
    // set函数中nx为只新增，xx为只修改，但是不成功也不抛异常
    public static void basic() {
        jedis.del("key1");  // 删除键值
        jedis.set("key1", "value1", "nx");  // 新增键值，已存在不新增，也不会抛异常
        jedis.set("key1", "value2", "xx");  // 修改键对应的值，已存在则修改，不存在不新增也不抛异常
        System.out.println(jedis.get("key1"));
        jedis.expire("key1", 3);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
        }
        System.out.println(jedis.get("key1"));
        jedis.set("key1", "value1");
        System.out.println(jedis.exists("key1"));
    }

    // incr加1，incrBy加一个整数，decr减1，decrBy减一个整数，incrByFloat加一个浮点数
    public static void num() {
        jedis.set("num1", "0");             // 可省略，如果没有num1，自动初始为0
        Long num1 = jedis.incr("num1");
        Long num2 = jedis.incrBy("num1", 3);
        Long num3 = jedis.decr("num1");
        Long num4 = jedis.decrBy("num1", 4);
        Double num5 = jedis.incrByFloat("num1", 2.5);
//        Long num6 = jedis.incr("num1");   // num1已经是浮点数了，抛异常JedisDataException
        System.out.println(num1);
        System.out.println(num2);
        System.out.println(num3);
        System.out.println(num4);
        System.out.println(num5);
    }

    // append添加字符串到末尾，
    // getrange取子字符串，参数为开始索引，结束索引，且包含开始和结束索引。-1代表结尾索引
    // setrange指定位置插入字符串
    public static void str() {
        jedis.set("str1", "");              // 可省略，如果没有num1，自动初始为""
        jedis.append("str1", "12345");
        System.out.println(jedis.get("str1"));
        String s1 = jedis.getrange("str1", 1, 3);
        System.out.println(s1 + "|" + jedis.get("str1"));
        jedis.setrange("str1", 2, "aa");
        System.out.println(jedis.get("str1"));
    }

    // setbit设置指定索引的二进制位
    // getbit获取指定索引的二进制位
    // bitcount二进制位值为1的个数
    // bitop多个值进行位运算存入指定键值中
    public static void bit() {
        jedis.setbit("bit1", 2, "1");
        jedis.setbit("bit1", 7, "1");
        System.out.println(jedis.get("bit1"));
        System.out.println((char) 33);
        System.out.println(jedis.getbit("bit1", 2));
        Long bitcount = jedis.bitcount("bit1");
        System.out.println(bitcount);
        jedis.setbit("bit2", 3, "1");
        System.out.println(jedis.get("bit2"));
        jedis.bitop(BitOP.OR, "bit3", "bit1", "bit2");
        System.out.println(jedis.get("bit3"));
        System.out.println((int)"1".charAt(0));
    }
}
