package com.netease.comment.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;

@Slf4j
public class RedisUtils {


	private static JedisPool jedisPool = null;
	private static boolean isClosed = false;

	/**
	 * 初始化Redis连接池
	 */
	static {
		try {
			JedisPoolConfig config = new JedisPoolConfig();
			Resource resource = new ClassPathResource("application.yml");
			Properties p = PropertiesLoaderUtils.loadProperties(resource);
			String host =  p.getProperty("host");
			int port = Integer.parseInt(p.getProperty("port"));
			int timeout = Integer.parseInt(p.getProperty("timeout"));
			String password=p.getProperty("password");

			config.setMaxIdle(Integer.parseInt(p.getProperty("maxIdle")));
			config.setMinIdle(Integer.parseInt(p.getProperty("minIdle")));
			config.setMaxTotal(Integer.parseInt(p.getProperty("maxTotal")));
			config.setMaxWaitMillis(Integer.parseInt(p.getProperty("maxWait")));
			config.setBlockWhenExhausted(Boolean.parseBoolean(p.getProperty("blockWhenExhausted")));

			log.info(String.format("connect to ncr: {%s:%s}", host, port));
			jedisPool = new JedisPool(config, host, port, timeout, password);
		} catch (Exception e) {
			isClosed = true;
			log.error("redis initiation error ：", e);
		}
	}

	/**
	 * 获取Jedis实例
	 * 
	 * @return
	 */
	public synchronized static Jedis getJedis() {
		try {
			if (isClosed)
				return null;
			if (jedisPool != null) {
				Jedis resource = jedisPool.getResource();
				return resource;
			} else {
				return null;
			}
		} catch (Exception e) {
			log.error("get redis error ：", e);
			return null;
		}
	}

	/**
	 * 释放jedis资源
	 * 
	 * @param jedis
	 */
	@SuppressWarnings("deprecation")
	public static void returnResource(final Jedis jedis) {
		if (jedis != null) {
			jedisPool.returnResource(jedis);
		}
	}

	public static String getString(String key) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return null;
		}
		if(null == key) {
			return null;
		}
		try {
			String value = jedis.get(key);
			// logger.debug("read redis: key = {}, value = {}", key, value);
			return value;
		} catch (Exception e) {
			log.error("read redis error: key={}", key, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	public static List<String> mgetString(List<String> keys) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return null;
		}
		if (null == keys || keys.size() <=0) {
			return null;
		}
		try {
			List<String> values = jedis.mget(keys.toArray(new String[keys.size()]));
			// logger.debug("read redis: keys = {}, values = {}", keys, values);
			return values;
		} catch (Exception e) {
			log.error("read redis error: keys = {}", keys, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	public static String setStringEx(String key, String value, int secconds) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return null;
		}
		if (null == key || null == value || secconds <= 0) {
			return null;
		}
		try {
			// logger.debug("write redis: key={}, value={}, seconds={}", key, value, secconds);
			return jedis.setex(key, secconds, value);
		} catch (Exception e) {
			log.error("write redis error: key={}, value={}, seconds={}", key, value, secconds, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	public static boolean delString (String key) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return false;
		}
		if (null == key) {
			return true;
		}
		try {
			Long result = jedis.del(key);
			// logger.debug("delete redis: key={}, result={}", key, result);
			return true;
		} catch (Exception e) {
			log.error("fail to delete redis: key={}", key, e);
			return false;
		} finally {
			returnResource(jedis);
		}
	}

	public static void sadd (String key, int ttl, String... members) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return;
		}
		if (null == key || ttl <= 0) {
			return;
		}
		try {
			Long num = jedis.sadd(key, members);
			if (num > 0) {
				jedis.expire(key, ttl);
				// logger.debug("write {} elements in redis set: key={}", num, key);
			}
		} catch (Exception e) {
			log.error("write redis set error: key={}, ttl={}, members={}", key, ttl, members, e);
		} finally {
			returnResource(jedis);
		}
	}

	public static Set<String> smembers (String key) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return null;
		}
		if (null == key) {
			return null;
		}
		try {
			Set<String> result = jedis.smembers(key);
			// logger.debug("read redis: key={}, set result={}", key, result);
			return result;
		} catch (Exception e) {
			log.error("read redis set error: key={}", key, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	public static void lpush (String key, int ttl, String... elements) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return;
		}
		if (null == key || ttl <= 0 || null == elements || elements.length <= 0) {
			return;
		}
		try {
			jedis.lpush(key, elements);
			jedis.expire(key, ttl);
			// logger.debug("write redis list: key={}, ttl={}, elements={}", key, ttl, elements);
			long len = jedis.llen(key);
			// 如果list长度超过500，保留最近的400条记录
			if (len >= 500) {
				// logger.debug("delete redis 100 elements from list, key={}", key);
				jedis.ltrim(key, 0, 399);
			}
		} catch (Exception e) {
			log.error("write redis list error: key={}, ttl={}, elements={}", key, ttl, elements, e);
		} finally {
			returnResource(jedis);
		}
	}

	public static List<String> lrange (String key, int start, int end) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return null;
		}
		if (null == key) {
			return null;
		}
		try {
			List<String > result = jedis.lrange(key, start, end);
			// logger.debug("read redis list: key={}, list={}", key, result);
			return result;
		} catch (Exception e) {
			log.error("read redis list error: key={}, start={}, end={}", key, start, end, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	public static void lrem (String key, long count, List<String> elements) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return;
		}
		if (null == key || null == elements || elements.isEmpty()) {
			return;
		}
		try {
			for (String element : elements) {
                if (null != element) {
                    jedis.lrem(key, count, element);
                }
            }
			// logger.debug("delete redis list: key={}, count={}, elements={}", key, count, elements);
		} catch (Exception e) {
			log.error("delete redis list error: key={}, count={}, elements={}", key, count, elements, e);
		} finally {
			returnResource(jedis);
		}
	}

	public static void hmset (String key, int ttl, Map<String, String> map) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return;
		}
		if (null == key || ttl <=0 || null == map || map.isEmpty()) {
			return;
		}
		try {
			jedis.hmset(key, map);
			// 用户浏览了头图或置顶文章，重置超时时间
			jedis.expire(key, ttl);
			// logger.debug("write redis hash: key={}, ttl={}, map={}", key, ttl, map);
			long len = jedis.hlen(key);
			// 如果field数量超过500，删除100个
			if (len >= 500) {
				Set<String> fields = jedis.hkeys(key);
				LinkedList<String> toBeDeleted = new LinkedList<>();
				int count = 0;
				for (String field : fields) {
					if (count >= 100) {
						break;
					}
					if (null != field) {
						toBeDeleted.add(field);
						count++;
					}
				}
				if (toBeDeleted.size() > 0) {
					jedis.hdel(key, toBeDeleted.toArray(new String[toBeDeleted.size()]));
					// logger.debug("delete fields={} from hash, key={}, length={}", toBeDeleted, key, jedis.hlen(key));
				}
            }
		} catch (Exception e) {
			log.error("write redis hash error: key={}, ttl={}, map={}", key, ttl, map, e);
		} finally {
			returnResource(jedis);
		}
	}

	public static List<String> hvals (String key) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return null;
		}
		if (null == key) {
			return null;
		}
		try {
			List<String> values = jedis.hvals(key);
			// logger.debug("read redis hash: key={}, values={}", key, values);
			return values;
		} catch (Exception e) {
			log.error("read redis hash error: key={}", key, e);
			return null;
		} finally {
			returnResource(jedis);
		}
	}

	public static void hdel (String key, String... fields) {
		Jedis jedis = getJedis();
		if (isClosed || jedis == null) {
			return;
		}
		if (null == key || null == fields || fields.length <= 0) {
			return;
		}
		try {
			jedis.hdel(key, fields);
			// logger.debug("delete redis hash fields, key={}, fields={}", key, fields);
		} catch (Exception e) {
			log.error("delete redis hash error: key={}, fields={}", key, fields, e);
		} finally {
			returnResource(jedis);
		}

	}

}