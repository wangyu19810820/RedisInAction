package com.book.chapter05.ip;

import com.JedisFactory;
import com.google.gson.Gson;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

// a.csv内的数据，ip转换后的分值段对应城市代码，存储到redis中的有序集合，存两个数据，A城市最高分，A城市最低分
// b.csv内的数据，城市代码对应城市描述，存储到redis中的hash中
// 程序内容，将ip转换成分值，zrevrangeByScore查询有序集合中的分值的段，找到匹配的城市
// 再从hash中找到城市描述
public class IPMapper {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) {
//        int score = ipToScore("121.225.47.73");
//        System.out.println(score);

//        importIpsToRedis(new File("E:\\gitproj\\RedisInAction\\java\\RedisInAction\\src\\main\\resources\\a.csv"));
//        importCitiesToRedis(new File("E:\\gitproj\\RedisInAction\\java\\RedisInAction\\src\\main\\resources\\b.csv"));

        String cityDesc = getCityByIp("121.225.47.73");
        String[] cityDescArr = new Gson().fromJson(cityDesc, String[].class);
        System.out.println(Arrays.toString(cityDescArr));
    }

    public static String getCityByIp(String ip) {
        int score = ipToScore(ip);
        score = 28561562;   // 数据不全，手动指定一个值
        Set<String> results = jedis.zrevrangeByScore("ip2cityid:", score, 0, 0, 1);
        if (results.size() == 0) {
            return null;
        }
        String city = results.iterator().next();
        city = "261_0"; // 数据不全，手动指定一个值
        String cityId = city.split("_")[0];
        String cityDesc = jedis.hget("cityid2city:", cityId);
        return cityDesc;
    }

    public static int ipToScore(String ip) {
        int score = 0;
        for (String seg : ip.split("\\.")) {
            score = score * 256 + Integer.parseInt(seg);
        }
        return score;
    }

    public static void importIpsToRedis(File file) {
        FileReader reader = null;
        try{
            reader = new FileReader(file);
            CSVParser parser = new CSVParser(reader, CSVFormat.newFormat(','));
            List<CSVRecord> data = parser.getRecords();
            for (CSVRecord record:data){
                long num = record.getRecordNumber();
                if (num<3){
                    continue;
                }
                String value1 = record.get(0).replaceAll("\"","");
                int ivalue1 = Integer.parseInt(value1);
                String value2 = record.get(1).replaceAll("\"","");
                int ivalue2 = Integer.parseInt(value2);
                String area1 = record.get(2).replaceAll("\"","")+"_0";
                String area2 = record.get(2).replaceAll("\"","")+"_1";
                System.out.println(value1);
                jedis.zadd("ip2cityid", ivalue1, area1);
                jedis.zadd("ip2cityid", ivalue2, area2);
//                jedis.zadd("ip2cityid:",Long.valueOf(record.get(0).replaceAll("\"","")),record.get(2).replaceAll("\"","")+"_0");
//                jedis.zadd("ip2cityid:",Long.valueOf(record.get(1).replaceAll("\"","")),record.get(2).replaceAll("\"","")+"_1");
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }finally{
            try{
                reader.close();
            }catch(Exception e){
                // ignore
            }
        }
    }

    public static void importCitiesToRedis(File file) {
        Gson gson = new Gson();
        try(Reader reader = new FileReader(file) ){
            CSVParser parser = new CSVParser(reader,CSVFormat.newFormat(','));
            List<CSVRecord> data = parser.getRecords();
            for (CSVRecord record:data){
                long num = record.getRecordNumber();
                if (num<3){
                    continue;
                }
                String cityId = record.get(0).replaceAll("\"","");
                String country = record.get(1).replaceAll("\"","");
                String region = record.get(2).replaceAll("\"","");
                String city = record.get(3).replaceAll("\"","");
                String postalCode = record.get(4).replaceAll("\"","");
                String latitude = record.get(5).replaceAll("\"","");
                String longitude = record.get(6).replaceAll("\"","");
                String metroCode = record.get(7).replaceAll("\"","");
                String areaCode = record.get(8).replaceAll("\"","");
                jedis.hset("cityid2city:",cityId,gson.toJson(new String[]{country, region, city, postalCode, latitude, longitude, metroCode, areaCode}));
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
