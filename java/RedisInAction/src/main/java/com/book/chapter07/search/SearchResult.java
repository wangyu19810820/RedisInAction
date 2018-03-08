package com.book.chapter07.search;

import java.util.List;

public class SearchResult {

    public final String id;
    public final long total;
    public final List<String> results;

    public SearchResult(String id, long total, List<String> results) {
        this.id = id;
        this.total = total;
        this.results = results;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "id='" + id + '\'' +
                ", total=" + total +
                ", results=" + results +
                '}';
    }
}
