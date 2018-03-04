package com.book.chapter06.mapreduce;

import com.JedisFactory;
import com.book.chapter06.group.ChatMessagesModel;
import com.book.chapter06.group.GroupDemo;
import com.book.chapter06.group.MessageModel;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.List;
import java.util.zip.GZIPInputStream;

// 收集数据，传到Redis中，适当控制中间数据量大小。另一台电脑接收数据，并处理。
// 收集+接收，其机制采用类似群组聊天室的代码架构
// 好处是分布式计算，将cpu负载平分到多台电脑上。redis仅仅是中间数据容器，汇总结果存放地。
public class MapReduceDemo {

    public static void main(String[] args) throws Exception {
        // 模拟的时候，无法同时运行，只能先传数据给Redis，然后再从Redis中取数据

        // 模拟从日志文件中传数据到Redis中
        File file = new File("C:\\code\\github\\RedisInAction\\java\\RedisInAction\\files");
        CopyLogsThread copyLogsThread = new CopyLogsThread(file, "channel", 1, 1000_1000);
        copyLogsThread.start();

        // 模拟从Redis中取数据，处理
        processLogsFromRedis(JedisFactory.getSingleJedis(), "source", s -> {
            System.out.println(s);
        });

    }

    // 从Redis中取数据，处理
    public static void processLogsFromRedis(Jedis conn, String id, Callback callback)
            throws Exception {
        while (true) {
            List<ChatMessagesModel> fdata = GroupDemo.fetchPendingMessages(id);
            for (ChatMessagesModel messages : fdata) {
                for (MessageModel message : messages.messages) {
                    String logFile = message.getMessage();

                    if (":done".equals(logFile)) {
                        return;
                    }
                    if (logFile == null || logFile.length() == 0) {
                        continue;
                    }
                    InputStream in = new RedisInputStream(conn, messages.chatId + logFile);
                    if (logFile.endsWith(".gz")){
                        in = new GZIPInputStream(in);
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    try{
                        String line = null;
                        while ((line = reader.readLine()) != null){
                            callback.callback(line);
                        }
                        callback.callback(null);
                    }finally{
                        reader.close();
                    }

                    conn.incr(messages.chatId + logFile + ":done");
                }
            }

            if (fdata.size() == 0){
                Thread.sleep(100);
            }

        }
    }
}
