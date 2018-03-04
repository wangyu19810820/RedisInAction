package com.book.chapter07;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Constants {


    public static final Pattern QUERY_RE = Pattern.compile("[+-]?[a-z']{2,}");

    // 匹配长度大于等于2字符的单词
    public static final Pattern WORDS_RE = Pattern.compile("[a-z']{2,}");

    // 无实际意义的常用英语单词
    public static final Set<String> STOP_WORDS = new HashSet<String>();
    static {
        for (String word :
                ("able about across after all almost also am among " +
                        "an and any are as at be because been but by can " +
                        "cannot could dear did do does either else ever " +
                        "every for from get got had has have he her hers " +
                        "him his how however if in into is it its just " +
                        "least let like likely may me might most must my " +
                        "neither no nor not of off often on only or other " +
                        "our own rather said say says she should since so " +
                        "some than that the their them then there these " +
                        "they this tis to too twas us wants was we were " +
                        "what when where which while who whom why will " +
                        "with would yet you your").split(" "))
        {
            STOP_WORDS.add(word);
        }
    }

}
