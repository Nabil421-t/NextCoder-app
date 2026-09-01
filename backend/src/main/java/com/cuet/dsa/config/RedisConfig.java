package com.cuet.dsa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    /**
     * Unchanged — used for plain string values: Lua script keys/args,
     * rate-limit counters, exam-start gate. Everything wired to this
     * template's original bean name keeps working as-is.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Separate bean, separate name — for manual caching of actual Java
     * objects (DTOs, entities) outside of @Cacheable, e.g. if you ever
     * need to hand-roll a cache-aside read instead of using the
     * annotation. Values are stored as readable JSON in redis-cli.
     *
     * Inject this one explicitly by name where you need it:
     *   @Qualifier("objectRedisTemplate") RedisTemplate<String, Object> template
     */
    @Bean
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        GenericJacksonJsonRedisSerializer jsonSerializer =
                GenericJacksonJsonRedisSerializer.builder().build();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    // Loads start_exam.lua from src/main/resources/scripts/.
    // Returns List - index 0 is "1" (first time) or "0" (already started),
    // index 1 is the deadline epoch-ms as a string. See ExamSessionService
    // for how this is consumed.
    @Bean
    public DefaultRedisScript<List> startExamScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("scripts/start_exam.lua"));
        script.setResultType(List.class);
        return script;
    }
}