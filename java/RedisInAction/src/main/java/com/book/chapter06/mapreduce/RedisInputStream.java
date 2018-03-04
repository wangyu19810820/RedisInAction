package com.book.chapter06.mapreduce;

import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.io.InputStream;

public class RedisInputStream extends InputStream {

    private Jedis conn;
    private String key;
    private int pos;

    public RedisInputStream(Jedis conn, String key){
        this.conn = conn;
        this.key = key;
    }

    @Override
    public int available()
            throws IOException
    {
        long len = conn.strlen(key);
        return (int)(len - pos);
    }

    @Override
    public int read()
            throws IOException
    {
        byte[] block = conn.substr(key.getBytes(), pos, pos);
        if (block == null || block.length == 0){
            return -1;
        }
        pos++;
        return (int)(block[0] & 0xff);
    }

    @Override
    public int read(byte[] buf, int off, int len)
            throws IOException
    {
        byte[] block = conn.substr(key.getBytes(), pos, pos + (len - off - 1));
        if (block == null || block.length == 0){
            return -1;
        }
        System.arraycopy(block, 0, buf, off, block.length);
        pos += block.length;
        return block.length;
    }

    @Override
    public void close() {
        // no-op
    }

}
