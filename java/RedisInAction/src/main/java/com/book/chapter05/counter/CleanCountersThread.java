package com.book.chapter05.counter;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.lang.Thread.sleep;

// 裁剪多余的点击统计数据
// 有序集合known:，其成员1:hits，5:hits等等
// hash，键名为1:hits，5:hits等等，存储的field是时间节点，value是点击数
public class CleanCountersThread extends Thread {

    private int sampleCount = 100;
    private boolean quit;
    private long timeOffset; // 当前时间的之前多少秒的数据需要被裁剪

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws Exception {
        CleanCountersThread thread = new CleanCountersThread(10000, 0L);
        thread.start();
        Thread.sleep(10000);
    }

    public CleanCountersThread(int sampleCount, long timeOffset){
        this.sampleCount = sampleCount;
        this.timeOffset = timeOffset;
    }

    public void quit(){
        quit = true;
    }

    public void run() {
        int passes = 0;
        while (!quit) {
            long start = System.currentTimeMillis() + timeOffset;
            int index = 0;
            while (index < jedis.zcard("known:")) {
                // 取出第一组计数器
                Set<String> hashSet = jedis.zrange("known:", index, index);
                index++;
                if (hashSet.size() == 0) {
                    break;
                }
                String hash = hashSet.iterator().next();
                int prec = Integer.parseInt(hash.substring(0, hash.indexOf(':')));
                int bprec = (int)Math.floor(prec / 60);
                if (bprec == 0){
                    bprec = 1;
                }
                if ((passes % bprec) != 0){
                    continue;
                }

                String hkey = "count:" + hash;
                String cutoff = String.valueOf(
                        ((System.currentTimeMillis() + timeOffset) / 1000) - sampleCount * prec);
                ArrayList<String> samples = new ArrayList<String>(jedis.hkeys(hkey));
                Collections.sort(samples);
                // 找到裁剪的索引点
                int remove = bisectRight(samples, cutoff);

                if (remove != 0){
                    // 删除多余数据
                    jedis.hdel(hkey, samples.subList(0, remove).toArray(new String[0]));
                    if (remove == samples.size()){
                        // 如果该计数器的值都被移除了，则在有序集合known:中也移除该成员
                        jedis.watch(hkey);
                        if (jedis.hlen(hkey) == 0) {
                            Transaction trans = jedis.multi();
                            trans.zrem("known:", hash);
                            trans.exec();
                            try {
                                trans.close();
                            } catch (IOException e) {
                            }
                            index--;
                        }else{
                            jedis.unwatch();
                        }
                    }
                }
            }

            passes++;
            // 如果执行期超过了60秒，休息1秒，继续裁剪其它计数器
            long duration = Math.min(
                    (System.currentTimeMillis() + timeOffset) - start + 1000, 60000);
            try {
                sleep(Math.max(60000 - duration, 1000));
            }catch(InterruptedException ie){
                Thread.currentThread().interrupt();
            }
        }
    }

    public int bisectRight(List<String> values, String key) {
        int index = Collections.binarySearch(values, key);
        return index < 0 ? Math.abs(index) - 1 : index + 1;
    }
}
