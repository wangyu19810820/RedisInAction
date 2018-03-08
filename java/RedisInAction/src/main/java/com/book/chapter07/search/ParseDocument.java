package com.book.chapter07.search;

import com.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static com.book.chapter07.search.Constants.*;

// 将文档内的有效字符（token，word）取出来，做反向索引
// 1.剔除过短的字符，2.剔除常用词，3建立反向索引：有效字符的集合包含文档id
public class ParseDocument {


    // 正则表达式找出符合要求的单词
    // 剔除长度小于等于2的单词，剔除无意义常用单词
    // 返回有效单词的集合
    public static Set<String> tokenize(String content) {
        Set<String> words = new HashSet<String>();
        Matcher matcher = WORDS_RE.matcher(content);
        while (matcher.find()){
            String word = matcher.group().trim();
            if (word.length() > 2 && !STOP_WORDS.contains(word)){
                words.add(word);
            }
        }
        return words;
    }

    // 为文档建立反向索引
    // 有效词集合包含文档id,idx:word1包含docid,idx:word2包含docid
    public static int indexDocument(Jedis conn, String docid, String content) throws Exception {
        Set<String> words = tokenize(content);
        Transaction trans = conn.multi();
        for (String word : words) {
            trans.sadd("idx:" + word, docid);
        }
        List<Object> results = trans.exec();
        trans.close();
        return results.size();
    }

    public static void main(String[] args) throws Exception {
//        tokenize("a abc ab");
        indexDocument(JedisFactory.getSingleJedis(), "doc1", "a abc ab efg efg abc");
    }
}
