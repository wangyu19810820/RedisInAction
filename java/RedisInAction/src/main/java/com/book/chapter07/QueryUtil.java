package com.book.chapter07;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.book.chapter07.Constants.*;

// 将查询表达式转换成Query对象
public class QueryUtil {

    public static void main(String[] args) {
        // abc表示要搜索的词，+efg表示它是abc的同义词，-xyz表示搜索需要排除的词，a b c因为太短不被录入
        Query query = parse("abc +efg -xyz opq a b c");
        System.out.println(query);
    }

    public static Query parse(String queryString) {
        Query query = new Query();
        Set<String> current = new HashSet<String>();
        Matcher matcher = QUERY_RE.matcher(queryString.toLowerCase());
        while (matcher.find()){
            String word = matcher.group().trim();
            char prefix = word.charAt(0);
            if (prefix == '+' || prefix == '-') {
                word = word.substring(1);
            }

            if (word.length() < 2 || STOP_WORDS.contains(word)) {
                continue;
            }

            if (prefix == '-') {
                query.unwanted.add(word);
                continue;
            }

            if (!current.isEmpty() && prefix != '+') {
                query.all.add(new ArrayList<String>(current));
                current.clear();
            }
            current.add(word);
        }

        if (!current.isEmpty()){
            query.all.add(new ArrayList<String>(current));
        }
        return query;
    }

}
