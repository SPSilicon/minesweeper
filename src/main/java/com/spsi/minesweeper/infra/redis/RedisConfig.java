package com.spsi.minesweeper.infra.redis;


import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import com.redis.om.spring.annotations.EnableRedisDocumentRepositories;

@Configuration
@EnableRedisDocumentRepositories(basePackages = "com.spsi.minesweeper.*")
public class RedisConfig {

    //@Bean
    LettuceConnectionFactory lettuceConnectionFactory() {
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
    }

}
