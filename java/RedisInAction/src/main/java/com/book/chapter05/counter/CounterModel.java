package com.book.chapter05.counter;

public class CounterModel {

    private String time;
    private int count;

    public CounterModel() {
    }

    public CounterModel(String time, int count) {
        this.time = time;
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CounterModel{" +
                "time='" + time + '\'' +
                ", count=" + count +
                '}';
    }
}
