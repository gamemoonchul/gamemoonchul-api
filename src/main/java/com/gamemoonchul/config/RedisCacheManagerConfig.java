package com.gamemoonchul.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamemoonchul.common.constants.RedisKeyConstant;
import com.gamemoonchul.domain.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@RequiredArgsConstructor
public class RedisCacheManagerConfig {
    private final ObjectMapper objectMapper;

    @Primary
    @Bean
    public CacheManager postCacheManager(RedisConnectionFactory cf) {
        RedisSerializer<Post> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructType(Post.class));

        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer)) // Value Serializer 변경
            .entryTtl(Duration.ofMinutes(30L)); // 캐시 수명 30분

        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(cf).cacheDefaults(redisCacheConfiguration).build();
    }

    @Bean
    public CacheManager commentCountCacheManager(RedisConnectionFactory cf) {
        RedisSerializer<Integer> valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructType(Integer.class));
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer)) // Value Serializer 변경
            .entryTtl(Duration.ofMinutes(30L)); // 캐시 수명 30분

        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(cf).cacheDefaults(redisCacheConfiguration).build();
    }
}
