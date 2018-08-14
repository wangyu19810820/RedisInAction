package com;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.BinaryJedisCluster;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.SortingParams;
import redis.clients.util.SafeEncoder;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class JedisClientCluster implements JedisClient {

	private JedisCluster jedisCluster;
	private BinaryJedisCluster binJedisCuster;

	public BinaryJedisCluster getBinJedisCuster() {
		return binJedisCuster;
	}
	public void setBinJedisCuster(BinaryJedisCluster binJedisCuster) {
		this.binJedisCuster = binJedisCuster;
	}
	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}
	public void setJedisCluster(JedisCluster jedisCluster) {
		this.jedisCluster = jedisCluster;
	}

	/**
	 * 设置一个字符串类型的值,如果记录存在则覆盖原有value
	 *
	 * @param key
	 *            值对应的键
	 * @param value
	 *            值
	 * @return 状态码, 成功则返回OK
	 */
    @Override
    public String set(String key, String value) {
        return jedisCluster.set(key, value);
    }
    @Override
    public String set(String key, String value, String nxxx, String expx, long time) {
        return jedisCluster.set(key, value, nxxx, expx, time);
    }
    
    @Override
    public String setObj(String key, Object value) {
        return binJedisCuster.set(keyToBytes(key), valueToBytes(value));
    }
	
//	@Override
//	public String setex(String key,int xx, String value) {
////		jedisCluster.setex(key, seconds, value);
////		binJedisCuster.set(keyToBytes(key), valueToBytes(value));
//		binJedisCuster.set(keyToBytes(key), valueToBytes(value));
//		return jedisCluster.set(key, value);
//	}

	/**
	 * 从redis中根据key取值
	 *
	 * @param key
	 *            要取得值对应的key
	 * @return 取到的value值
	 */
    @Override
    public String get(String key) {
        return jedisCluster.get(key);
    }
    
    @Override
    public Object getObj(String key) {
        byte[] bb=binJedisCuster.get(keyToBytes(key));
        return valueFromBytes(bb);
    }

	/**
	 * 判断某个键值对是否存在
	 *
	 * @param key
	 *            根据键判断
	 * @return 判断结果
	 */
	@Override
	public Boolean exists(String key) {
		return jedisCluster.exists(key);
	}

	/**
	 * 设置键值对的过期时间
	 *
	 * @param key
	 *            要设置过期时间的k键值对的键
	 * @param seconds
	 *            过期时间
	 * @return 影响的记录数
	 */
	@Override
	public Long expire(String key, int seconds) {
		return jedisCluster.expire(key, seconds);
	}

	/**
	 * 查看键值对的剩余时间
	 *
	 * @param key
	 *            要查看的键值对的键
	 * @return 剩余时间
	 */
	@Override
	public Long ttl(String key) {
		return jedisCluster.ttl(key);
	}

	/**
	 * 添加一个对应关系
	 *
	 * @param key
	 *            存储的键
	 * @param field
	 *            存储的名字
	 * @param value
	 *            存储的值
	 * @return 状态码, 1成功, 0失败, 如果field已存在将更新, 返回0
	 */
	@Override
	public Long hset(String key, String field, String value) {
		return jedisCluster.hset(key, field, value);
	}

	/**
	 * 返回hash中指定存储的值
	 *
	 * @param key
	 *            查找的存储的键
	 * @param field
	 *            查找的存储的名字
	 * @return 指定存储的值
	 */
    @Override
    public String hget(String key, String field) {
        return jedisCluster.hget(key, field);
    }
    
    

	/**
	 * 从hash中删除指定的存储
	 *
	 * @param key
	 *            存储的键
	 * @param field
	 *            存储的名字
	 * @return 状态码, 1成功, 0失败
	 */
	@Override
	public Long hdel(String key, String... field) {
		return jedisCluster.hdel(key, field);
	}

	/**
	 * 检测hash中指定的存储是否存在
	 *
	 * @param key
	 *            存储的键
	 * @param field
	 *            存储的额名字
	 * @return 状态码, 1代表成功, 0代表失败
	 */
	@Override
	public Boolean hexists(String key, String field) {
		return jedisCluster.hexists(key, field);
	}

	/**
	 * 以map的形式返回hash存储的名字和值
	 *
	 * @param key
	 *            存储的键
	 * @return 根据key查找到的存储的名字和值
	 */
	@Override
	public Map<String, String> hgetAll(String key) {
		return jedisCluster.hgetAll(key);
	}

	/**
	 * 获取hash中value的集合
	 *
	 * @param key
	 *            hash中存储的键
	 * @return 指定键的所有value的集合
	 */
	@Override
	public List<String> hvals(String key) {
		return jedisCluster.hvals(key);
	}

	/**
	 * 根据存储的键删除存储
	 *
	 * @param key
	 *            存储的键
	 * @return 状态码, 1成功, 0失败
	 */
	@Override
	public Long del(String key) {
		return jedisCluster.del(key);
	}

	/**
	 * rpush(key, value) 从list集合中插入数据
	 * 
	 */
	@Override
	public Long rpush(String key, String value) {

		return jedisCluster.rpush(key, value);
	}

	/**
	 * 
	 * 从list集合中读取数据
	 * 
	 */
	@Override
	public List lrange(String key, int num, int end) {

		return jedisCluster.lrange(key, num, end);
	}

	/**
	 * Redis Lpop 命令用于list移除并返回列表的第一个元素。
	 */
	@Override
	public String Lpop(String key) {

		return jedisCluster.lpop(key);
	}

	/**
	 * Redis Llen 命令用于list返回列表的长度。 如果列表 key 不存在，则 key 被解释为一个空列表，返回 0 。 如果 key
	 * 不是列表类型，返回一个错误。
	 */
	@Override
	public Long Llen(String key) {

		return jedisCluster.llen(key);
	}

	/**
	 * Redis Rpop 命令用于list移除并返回列表的最后一个元素。
	 */
	@Override
	public String Rpop(String key) {
		return jedisCluster.rpop(key);
	}
    @Override
    public Object RpopObject(String key) {
        byte[] bb=binJedisCuster.rpop(keyToBytes(key));
        return valueFromBytes(bb);
    }
    
	/**
	 * Redis Ltrim 对一个列表list进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。 下标 0
	 * 表示列表的第一个元素，以 1 表示列表的第二个元素，以此类推。 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素
	 */
	@Override
	public String Ltrim(String key, Long start, Long end) {
		return jedisCluster.ltrim(key, start, end);
	}

	/**
	 * Redis Lset 通过索引来设置list里元素的值。 当索引参数超出范围，或对一个空列表进行 LSET 时，返回一个错误。
	 */
	@Override
	public String lset(String key, Long index, String value) {
		return jedisCluster.lset(key, index, value);
	}
	/**
	 * Redis Linsert 命令用于在list列表的元素前或者后插入元素。 当指定元素不存在于列表中时，不执行任何操作。 
	 * 当列表不存在时，被视为空列表，不执行任何操作。 如果 key 不是列表类型，返回一个错误。
	 * where的值前台传过来必须是大写
	 */
	@Override
	public Long Linsert(String key, LIST_POSITION where, String pivot, String value) {
		return jedisCluster.linsert(key, where, pivot, value);
	}

    /**
     * Redis Lpush 命令将一个或多个值插入到list列表头部。 如果 key 不存在，一个空列表会被创建并执行 LPUSH 操作。 当 key
     * 存在但不是列表类型时，返回一个错误。
     */
    @Override
    public Long lpush(String key, String valve) {
        // binJedisCuster.lpush(keyToBytes(key), valueToBytes(value));
        // jedisCluster.lpush(keyToBytes(key), valueToBytes(value));
        return jedisCluster.lpush(key, valve);
    }

    @Override
    public Long lpush(String key, Object value) {
        // jedisCluster.lpush(keyToBytes(key), valueToBytes(value));
        return binJedisCuster.lpush(keyToBytes(key), valueToBytes(value));
    }
	/**
	 * Redis Blpop 命令移出并获取列表的第一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
	 */
	@Override
	public List<String> blpop(int timeout,String key) {
		
		return jedisCluster.blpop(timeout, key);
	}
	/**
	 * Redis Rpushx 命令用于将一个或多个值插入到已存在的list列表尾部(最右边)。如果列表不存在，操作无效。
	 */
	@Override
	public Long rpushx(String key, String value) {
		
		return jedisCluster.rpushx(key, value);
	}
	/**
	 * Redis Sadd 命令将一个或多个成员元素加入到set集合中，已经存在于集合的成员元素将被忽略。
	 */
	@Override
	public Long sadd(String key, String member) {
		return jedisCluster.sadd(key, member);
	}
	/**
	 * Redis Scard 命令返回set集合中元素的数量。
	 */

	@Override
	public Long scard(String key) {
		// TODO Auto-generated method stub
		return jedisCluster.scard(key);
	}
	/**
	 * Redis Sismember 命令判断成员元素是否是集合的成员。
	 * @param key
	 * @param value
	 * @return
	 */
	@Override
	public boolean sismember(String key, String value) {
		return jedisCluster.sismember(key, value);
	}
	/**
	 * Redis Sscan 命令用于迭代集合键中的元素。
	 */
	@Override
	public ScanResult<String> sscan(String key, String num) {
		return jedisCluster.sscan(key, num);
	}
	/**
	 * Redis Smembers 命令返回集合中的所有的成员。 不存在的集合 key 被视为空集合。
	 */
	@Override
	public Set<String> smembers(String key) {
		return jedisCluster.smembers(key);
	}

    // ---------

    public byte[] keyToBytes(String key) {
        return SafeEncoder.encode(key);
    }

    private byte[] keyToBytes(Object key) {
        return SafeEncoder.encode(key.toString());
    }

    private byte[] valueToBytes(Object object) {
        return SerializeUtils.serialize(object);
    }

    protected Object valueFromBytes(byte[] bytes) {
        if (bytes != null)
            return SerializeUtils.deSerialize(bytes);
        return null;
    }
    
    @Override
    public List<String> sort(String listKey, String ...sortStr) {
        SortingParams sortingParameters = new SortingParams();
//      sortingParameters.get("user:*->name");
//      sortingParameters.get("user:*->add");
////      System.out.println(jedis.);
        for(int i=0; i<sortStr.length; i++)
            sortingParameters.get(sortStr[i]);
        return jedisCluster.sort(listKey,sortingParameters);
    }
}
