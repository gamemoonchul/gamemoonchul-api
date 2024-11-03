package com.gamemoonchul.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemoonchul.domain.entity.Post;
import com.gamemoonchul.infrastructure.web.dto.response.PostMainPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public RedisTemplate<String, Integer> stringIntegerRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer 설정
        template.setKeySerializer(new StringRedisSerializer());

        // Value Serializer 설정
        template.setValueSerializer(new GenericToStringSerializer<>(Integer.class));

        return template;
    }

    /**
     * Redis Cacheable에서 사용함
     *
     * @param connectionFactory
     * @return redisTemplate
     */
    @Bean
    public RedisTemplate<String, Post> postRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Post> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key Serializer 설정
        template.setKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<Post> serializer =
            new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructType(Post.class));
        template.setValueSerializer(serializer);

        template.setValueSerializer(serializer);
        template.setValueSerializer(serializer);

        return template;
    }
}
