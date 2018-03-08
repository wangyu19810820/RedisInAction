package com.book.chapter07.search;

import java.util.ArrayList;
import java.util.List;

// 将字符串转换成数字，方便匹配最相近内容
public class StringToScoreUtil {

    public static void main(String[] args) {
        long l1 = stringToScore("aaabbb", true);
        long l2 = stringToScore("aaabbc", true);
        System.out.println(l1);
        System.out.println(l2);
    }

    public static long stringToScore(String string) {
        return stringToScore(string, false);
    }

    public static long stringToScore(String string, boolean ignoreCase) {
        if (ignoreCase){
            string = string.toLowerCase();
        }

        List<Integer> pieces = new ArrayList<Integer>();
        for (int i = 0; i < Math.min(string.length(), 6); i++) {
            pieces.add((int)string.charAt(i));
        }
        while (pieces.size() < 6){
            pieces.add(-1);
        }

        long score = 0;
        for (int piece : pieces) {
            score = score * 257 + piece + 1;
        }

        return score * 2 + (string.length() > 6 ? 1 : 0);
    }
}
