package com.book.chapter07.advice;

import com.book.chapter07.search.AggregationUtil;
import com.book.chapter07.search.ParseDocument;
import org.javatuples.Pair;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.ZParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TargetAds {

    public Pair<Long,String> targetAds(
            Jedis conn, String[] locations, String content)
    {
        Transaction trans = conn.multi();

        String matchedAds = matchLocation(trans, locations);

        String baseEcpm = AggregationUtil.zintersect(
                trans, 30, new ZParams().weights(0, 1), matchedAds, "ad:value:");

        Pair<Set<String>,String> result = finishScoring(
                trans, matchedAds, baseEcpm, content);

        trans.incr("ads:served:");
        trans.zrevrange("idx:" + result.getValue1(), 0, 0);

        List<Object> response = trans.exec();
        long targetId = (Long)response.get(response.size() - 2);
        Set<String> targetedAds = (Set<String>)response.get(response.size() - 1);

        if (targetedAds.size() == 0){
            return new Pair<Long,String>(null, null);
        }

        String adId = targetedAds.iterator().next();
        recordTargetingResult(conn, targetId, adId, result.getValue0());

        return new Pair<Long,String>(targetId, adId);
    }

    // 从反向索引中获取匹配地区的广告id集合
    public String matchLocation(Transaction trans, String[] locations) {
        // 地区反向索引是req:location，转换后获取adID的集合
        String[] required = new String[locations.length];
        for(int i = 0; i < locations.length; i++){
            required[i] = "req:" + locations[i];
        }
        return AggregationUtil.union(trans, 300, required);
    }

    public void recordTargetingResult(
            Jedis conn, long targetId, String adId, Set<String> words)
    {
        Set<String> terms = conn.smembers("terms:" + adId);
        String type = conn.hget("type:", adId);

        Transaction trans = conn.multi();
        terms.addAll(words);
        if (terms.size() > 0) {
            String matchedKey = "terms:matched:" + targetId;
            for (String term : terms) {
                trans.sadd(matchedKey, term);
            }
            trans.expire(matchedKey, 900);
        }

        trans.incr("type:" + type + ":views:");
        for (String term : terms) {
            trans.zincrby("views:" + adId, 1, term);
        }
        trans.zincrby("views:" + adId, 1, "");

        List<Object> response = trans.exec();
        double views = (Double)response.get(response.size() - 1);
        if ((views % 100) == 0){
            updateCpms(conn, adId);
        }
    }

    public Pair<Set<String>,String> finishScoring(
            Transaction trans, String matched, String base, String content) {
        Map<String,Integer> bonusEcpm = new HashMap<String,Integer>();
        Set<String> words = ParseDocument.tokenize(content);
        for (String word : words){
            String wordBonus = AggregationUtil.zintersect(
                    trans, 30, new ZParams().weights(0, 1), matched, word);
            bonusEcpm.put(wordBonus, 1);
        }

        if (bonusEcpm.size() > 0){

            String[] keys = new String[bonusEcpm.size()];
            int[] weights = new int[bonusEcpm.size()];
            int index = 0;
            for (Map.Entry<String,Integer> bonus : bonusEcpm.entrySet()){
                keys[index] = bonus.getKey();
                weights[index] = bonus.getValue();
                index++;
            }

            ZParams minParams = new ZParams().aggregate(ZParams.Aggregate.MIN).weights(weights);
            String minimum = AggregationUtil.zunion(trans, 30, minParams, keys);

            ZParams maxParams = new ZParams().aggregate(ZParams.Aggregate.MAX).weights(weights);
            String maximum = AggregationUtil.zunion(trans, 30, maxParams, keys);

            String result = AggregationUtil.zunion(
                    trans, 30, new ZParams().weights(2, 1, 1), base, minimum, maximum);
            return new Pair<Set<String>,String>(words, result);
        }
        return new Pair<Set<String>,String>(words, base);
    }

    public void updateCpms(Jedis conn, String adId) {
        Transaction trans = conn.multi();
        trans.hget("type:", adId);
        trans.zscore("ad:base_value:", adId);
        trans.smembers("terms:" + adId);
        List<Object> response = trans.exec();
        String type = (String)response.get(0);
        Double baseValue = (Double)response.get(1);
        Set<String> words = (Set<String>)response.get(2);

        String which = "clicks";
        PriceType ecpm = Enum.valueOf(PriceType.class, type.toUpperCase());
        if (PriceType.CPA.equals(ecpm)) {
            which = "actions";
        }

        trans = conn.multi();
        trans.get("type:" + type + ":views:");
        trans.get("type:" + type + ':' + which);
        response = trans.exec();
        String typeViews = (String)response.get(0);
        String typeClicks = (String)response.get(1);

        PriceUtil.AVERAGE_PER_1K.put(ecpm,
                1000. *
                        Integer.valueOf(typeClicks != null ? typeClicks : "1") /
                        Integer.valueOf(typeViews != null ? typeViews : "1"));

        if (PriceType.CPM.equals(ecpm)) {
            return;
        }

        String viewKey = "views:" + adId;
        String clickKey = which + ':' + adId;

        trans = conn.multi();
        trans.zscore(viewKey, "");
        trans.zscore(clickKey, "");
        response = trans.exec();
        Double adViews = (Double)response.get(0);
        Double adClicks = (Double)response.get(1);

        double adEcpm = 0;
        if (adClicks == null || adClicks < 1){
            Double score = conn.zscore("idx:ad:value:", adId);
            adEcpm = score != null ? score.doubleValue() : 0;
        }else{
            adEcpm = PriceUtil.toEcpm(
                    ecpm,
                    adViews != null ? adViews.doubleValue() : 1,
                    adClicks != null ? adClicks.doubleValue() : 0,
                    baseValue);
            conn.zadd("idx:ad:value:", adEcpm, adId);
        }
        for (String word : words) {
            trans = conn.multi();
            trans.zscore(viewKey, word);
            trans.zscore(clickKey, word);
            response = trans.exec();
            Double views = (Double)response.get(0);
            Double clicks = (Double)response.get(1);

            if (clicks == null || clicks < 1){
                continue;
            }

            double wordEcpm = PriceUtil.toEcpm(
                    ecpm,
                    views != null ? views.doubleValue() : 1,
                    clicks != null ? clicks.doubleValue() : 0,
                    baseValue);
            double bonus = wordEcpm - adEcpm;
            conn.zadd("idx:" + word, bonus, adId);
        }
    }

}
