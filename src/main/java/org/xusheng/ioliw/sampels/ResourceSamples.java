package org.xusheng.ioliw.sampels;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class ResourceSamples {

    public static void main(String[] args) {
        JedisPoolWrapper jedisPoolWrapper = new JedisPoolWrapper(null);
        jedisPoolWrapper.run(jedis -> jedis.get("hello.world"));
    }
}

interface RedisOperation<T> {
    T run(Jedis jedis);
}

class JedisPoolWrapper {
    private final JedisPool pool;
    public JedisPoolWrapper(JedisPool pool) {
        this.pool = pool;
    }
    public <T> T run(RedisOperation<T> operation) {
        Jedis resource = pool.getResource();
        try {
            return operation.run(resource);
        } finally {
            pool.returnResource(resource);
        }
    }
}

