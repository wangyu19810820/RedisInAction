package com.book.chapter06.mapreduce;

import com.JedisFactory;
import com.book.chapter06.group.GroupDemo;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.util.*;

// 读取临时文件内的内容，存入redis，等待其它线程将数据取走
// 通过limit控制redis内最大暂存数据大小，暂存数据如果达到最大值，就等待一段时间
// 消息拉取（替代订阅/发布）的处理方式和“群组聊天”（GroupDemo）相同
public class CopyLogsThread extends Thread {

    private Jedis conn;
    private File path;
    private String channel;
    private int count;
    private long limit;

    public CopyLogsThread(File path, String channel, int count, long limit) {
        this.conn = JedisFactory.getSingleJedis();
        this.path = path;
        this.channel = channel;
        this.count = count;
        this.limit = limit;
    }

    public void run() {
        Deque<File> waiting = new ArrayDeque<File>();
        long bytesInRedis = 0;  // 实际暂存数据大小

        // 将接收者放入一个“群组”中
        Set<String> recipients= new HashSet<String>();
        for (int i = 0; i < count; i++){
            recipients.add(String.valueOf(i));
        }
        GroupDemo.createChat("source", recipients, "", channel);

        File[] logFiles = path.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String name){
                return name.startsWith("temp_redis");
            }
        });
        Arrays.sort(logFiles);
        for (File logFile : logFiles){
            long fsize = logFile.length();
            while ((bytesInRedis + fsize) > limit){
                long cleaned = clean(waiting, count);
                if (cleaned != 0){
                    bytesInRedis -= cleaned;
                }else{
                    try{
                        sleep(250);
                    }catch(InterruptedException ie){
                        Thread.interrupted();
                    }
                }
            }

            BufferedInputStream in = null;
            try{
                in = new BufferedInputStream(new FileInputStream(logFile));
                int read = 0;
                byte[] buffer = new byte[8192];
                while ((read = in.read(buffer, 0, buffer.length)) != -1){
                    if (buffer.length != read){
                        byte[] bytes = new byte[read];
                        System.arraycopy(buffer, 0, bytes, 0, read);
                        conn.append((channel + logFile).getBytes(), bytes);
                    }else{
                        conn.append((channel + logFile).getBytes(), buffer);
                    }
                }
            }catch(IOException ioe){
                ioe.printStackTrace();
                throw new RuntimeException(ioe);
            }finally{
                try{
                    in.close();
                }catch(Exception ignore){
                }
            }

            GroupDemo.sendMessage(channel, "source", logFile.toString());

            bytesInRedis += fsize;
            waiting.addLast(logFile);
        }

        GroupDemo.sendMessage(channel, "source", ":done");

        while (waiting.size() > 0){
            long cleaned = clean(waiting, count);
            if (cleaned != 0){
                bytesInRedis -= cleaned;
            }else{
                try{
                    sleep(250);
                }catch(InterruptedException ie){
                    Thread.interrupted();
                }
            }
        }

    }

    private long clean(Deque<File> waiting, int count) {
        if (waiting.size() == 0){
            return 0;
        }
        File w0 = waiting.getFirst();
        if (String.valueOf(count).equals(conn.get(channel + w0 + ":done"))){
            conn.del(channel + w0, channel + w0 + ":done");
            return waiting.removeFirst().length();
        }
        return 0;
    }
}