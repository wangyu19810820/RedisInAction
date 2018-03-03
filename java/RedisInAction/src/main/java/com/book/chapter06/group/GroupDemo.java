package com.book.chapter06.group;

import com.JedisFactory;
import com.book.chapter06.lock.SimpleLockUtil;
import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import java.util.*;

// 用两类有序集合实现群组功能
// 一个是聊天室有序集合，成员是聊天室用户id，分值是用户接收到该聊天室消息的最大id
// 一个是用户接收消息有序集合，成员是聊天室id，分值是用户接收到该聊天室消息的最大id
// 一个是消息有序集合，成员是消息内容，分值是消息id
public class GroupDemo {

    static Jedis jedis = JedisFactory.getSingleJedis();

    public static void main(String[] args) throws Exception {
//        long chatId = jedis.incr("chat:");
//        Set<String> set = new HashSet<>();
//        set.add("2");
//        set.add("3");
//        createChat("1", set, "aa", String.valueOf(chatId));

//        long chatId1 = jedis.incr("chat:");
//        createChat("1", set, "aa", String.valueOf(chatId1));
//        sendMessage("2", "1", "bb");

        fetchPendingMessages("1");
    }

    // 将发送者加入到接收者集合中
    // 开启事务，创建聊天室有序集合，创建发送者接收者已读消息有序集合，提交事务
    // 发送消息
    public static String createChat(String sender, Set<String> recipients, String message, String chatID) {
        recipients.add(sender);
        Transaction trans = jedis.multi();
        for (String recipient : recipients) {
            trans.zadd("chat:" + chatID, 0, recipient);
            trans.zadd("seen:" + recipient, 0, chatID);
        }
        trans.exec();
        return sendMessage(chatID, sender, message);
    }

    // 获取锁，获取自增msgID，往有序集合msgs:chatId中插入消息
    public static String sendMessage(String chatId, String sender, String message) {
        String lock = SimpleLockUtil.acquire_lock("chat:" + chatId, 1000);
        if (lock == null) {
            throw new RuntimeException("无法获取锁");
        }
        try {
            long msgID = jedis.incr("ids:" + chatId);
            Date ts = new Date();
            MessageModel messageModel = new MessageModel();
            messageModel.setId(String.valueOf(msgID));
            messageModel.setMessage(message);
            messageModel.setTs(ts);
            messageModel.setSender(sender);
            Gson gson = new Gson();
            String str = gson.toJson(messageModel);
            jedis.zadd("msgs:" + chatId, msgID, str);
        } finally {
            SimpleLockUtil.release_lock("chat:" + chatId, lock);
        }

        return chatId;
    }

    // 遍历该用户拥有的聊天室，将消息封装到List<ChatMessagesModel>中
    // 收集
    // 返回新接收的消息List<ChatMessagesModel>
    public static List<ChatMessagesModel> fetchPendingMessages(String recipient) throws Exception {
        // 从seen:userID获取该用户最后接收消息情况（chatID，最后接收msgID）集合
        Set<Tuple> seenSet = jedis.zrangeWithScores("seen:" + recipient, 0, -1);
        List<Tuple> seenList = new ArrayList<Tuple>(seenSet);

        // 从msgs:chatID获取所有未读消息
        List<Response<Set<String>>> msgResList = new ArrayList<>();
        Transaction trans = jedis.multi();
        for (Tuple tuple : seenList) {
            String chatId = tuple.getElement();
            int lastRevMsgId = (int)tuple.getScore();
            Response<Set<String>> res = trans.zrangeByScore("msgs:" + chatId, String.valueOf(lastRevMsgId + 1), "inf");
            msgResList.add(res);
        }
        trans.exec();
        trans.close();

        // 将该用户的msg封装到对象中
        // 修改状态（seen:userID中的分值，chat:chatID中的分值，msg:chatID中所有已读消息）
        List<ChatMessagesModel> chatMessagesModelList = new ArrayList<>();
        List<Object[]> seenUpdates = new ArrayList<Object[]>();
        List<Object[]> msgRemoves = new ArrayList<Object[]>();
        for (int i = 0; i < seenList.size(); i++) {
            // 从seenList中获取chatID
            Tuple tuple = seenList.get(i);
            String chatID = tuple.getElement();
            int maxSeenMsgID = (int)(double)jedis.zscore("seen:" + recipient, chatID);

            Response<Set<String>> res1 = msgResList.get(i);
            Set<String> msgSet = res1.get();
            List<MessageModel> messageModelList = new ArrayList<>();
            for (String msg : msgSet) {
                Gson gson = new Gson();
                MessageModel messageModel = gson.fromJson(msg, MessageModel.class);
                messageModelList.add(messageModel);
                int msgID = Integer.parseInt(messageModel.getId());
                if (msgID > maxSeenMsgID) {
                    maxSeenMsgID = msgID;
                }
            }

            chatMessagesModelList.add(new ChatMessagesModel(chatID, messageModelList));

            // 修改seen:userID中,chatID对应的分值。修改chat:chatID中，userID对应分值
            // 删除msg:chatID中分值小于chat:chatID最小值的消息
            jedis.zadd("seen:" + recipient, maxSeenMsgID, chatID);
            jedis.zadd("chat:" + chatID, maxSeenMsgID, recipient);
            Set<Tuple> set = jedis.zrangeWithScores("chat:" + chatID, 0, 0);
            if (set.size() > 0) {
                double score = set.iterator().next().getScore();
                jedis.zremrangeByScore("msgs:" + chatID, 0, score);
            }
        }

        return chatMessagesModelList;
    }
}
