package com.book.chapter07.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Query {
    // 查询包括哪些需要搜索的单词，单个List<String>包含了一组同义词
    public final List<List<String>> all = new ArrayList<List<String>>();

    // 查询包括哪些不需要搜索的单词
    public final Set<String> unwanted = new HashSet<String>();

    @Override
    public String toString() {
        return "Query{" +
                "all=" + all +
                ", unwanted=" + unwanted +
                '}';
    }
}
