package com.book.chapter03;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import java.io.IOException;

/**
 * 使用管道提供基本的事务
 */
public class PipeLineDemo {

    private static final String KEY_1 = "pipe_1";
    private static Jedis jedis1 = JedisFactory.getJedis();
    private static Jedis jedis2 = JedisFactory.getJedis();
    private static Jedis jedis3 = JedisFactory.getJedis();

    public static void main(String[] args) {
        jedis1.del(KEY_1);
        // 不使用管道, 线程内的最终错误，最终结果正确
//        NormalRedis selfThread1 = new NormalRedis(jedis1);
//        NormalRedis selfThread2 = new NormalRedis(jedis2);
//        NormalRedis selfThread3 = new NormalRedis(jedis3);
        // 使用管道, 线程内的结果正确，最终结果正确
        PipelineRedis selfThread1 = new PipelineRedis(jedis1);
        PipelineRedis selfThread2 = new PipelineRedis(jedis2);
        PipelineRedis selfThread3 = new PipelineRedis(jedis3);
        selfThread1.start();
        selfThread2.start();
        selfThread3.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(jedis2.get(KEY_1));
        jedis1.close();
        jedis2.close();
        jedis3.close();
    }

    /**
     * 使用管道
     */
    static class PipelineRedis extends Thread {
        private Jedis jedis;

        public PipelineRedis(Jedis jedis) {
            this.jedis = jedis;
        }

        @Override
        public void run() {
            Pipeline pipeline = jedis.pipelined();
            pipeline.multi();
            Response<Long> v = pipeline.incr(KEY_1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Response<Long> v1 = pipeline.incrBy(KEY_1, -1);
            pipeline.exec();
            try {
                pipeline.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("SelfThread," + hashCode() + ":" + v.get());
            System.out.println("SelfThread," + hashCode() + ":" + v1.get());
        }
    }

    /**
     * 不使用管道
     */
    static class NormalRedis extends Thread {
        private Jedis jedis;

        public NormalRedis(Jedis jedis) {
            this.jedis = jedis;
        }

        @Override
        public void run() {
            long v = jedis.incr(KEY_1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long v1 = jedis.incrBy(KEY_1, -1);
            System.out.println("SelfThread," + hashCode() + ":" + v);
            System.out.println("SelfThread," + hashCode() + ":" + v1);
        }
    }

}
