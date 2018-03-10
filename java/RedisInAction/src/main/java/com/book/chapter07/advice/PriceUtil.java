package com.book.chapter07.advice;

import java.util.HashMap;
import java.util.Map;

// 统一计费方式工具类，可将值缓存起来
public class PriceUtil {

    public static Map<PriceType,Double> AVERAGE_PER_1K = new HashMap<PriceType,Double>();

    // 统一计费方式
    // 按千次浏览量计费，原值返回
    // 按点击数，按动作计费，都可以通过比例转换成按千次浏览量计费（比例为：千次浏览点击率，千次浏览动作率）
    public static double toEcpm(PriceType type, double views, double avg, double value) {
        switch(type){
            case CPC:
            case CPA:
                return 1000. * value * avg / views;
            case CPM:
                return value;
        }
        return value;
    }
}
