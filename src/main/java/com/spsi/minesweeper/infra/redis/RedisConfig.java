package com.spsi.minesweeper.infra.redis;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

@Configuration
@EnableRedisDocumentRepositories(basePackages = "com.spsi.minesweeper.*")
public class RedisConfig {

    @Bean
    JedisConnectionFactory redisConnectionFactory() {
        return new JedisConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
    }

}
