package com.watashi.msghandler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class OtherApplicationTests {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
        HashOperations hashOperations = redisTemplate.opsForHash();
        List<String> list = new ArrayList();
        list.add("444");
        list.add("555");
        System.out.println("list = " + list.toArray());
//        hashOperations.delete("aaa", list.toArray());
        redisTemplate.executePipelined(new RedisCallback<Object>() {

            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
//                Integer key = 880012;
//                redisConnection.hashCommands().hDel("\"order_key\"".getBytes(), key.toString().getBytes());
//                redisConnection.hashCommands().hDel("aaa".getBytes(), ))
                return null;
            }
        });
    }

}
