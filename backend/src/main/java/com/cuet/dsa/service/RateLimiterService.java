package com.cuet.dsa.service;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    // Storage for buckets associated with unique IP addresses
    private final Map<String,Bucket>buckets=new ConcurrentHashMap<>();
    public Bucket resolvedBucket(String ip){
        return buckets.computeIfAbsent(ip,k->createBucket());
    }
    private Bucket createBucket(){
        Bandwidth limit=Bandwidth.builder()
                .capacity(100)
                .refillGreedy(5, Duration.ofSeconds(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
    public boolean tryConsume(String ip){
        return resolvedBucket(ip).tryConsume(1);
    }

}


//package com.cuetdsa.ratelimiter.config;
//
//import com.cuetdsa.ratelimiter.model.RateLimitResult;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.Collections;
//import java.util.List;
//
//@Service
//public class RateLimiterService {
//
//    private final RedisTemplate<String, Object> redisTemplate;
//    private final DefaultRedisScript<List> tokenBucketScript;
//
//    public RateLimiterService(RedisTemplate<String, Object> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//        this.tokenBucketScript = loadScript();
//    }
//
//    public RateLimitResult isAllowed(String bucketKey) {
//        return isAllowed(bucketKey, 100, 10.0);
//    }
//
//    public RateLimitResult isAllowed(String bucketKey, long capacity, double refillRate) {
//        long now = System.currentTimeMillis();
//        List<String> keys = Collections.singletonList(bucketKey);
//
//        @SuppressWarnings("unchecked")
//        List<Long> result = redisTemplate.execute(
//                tokenBucketScript,
//                keys,
//                String.valueOf(capacity),
//                String.valueOf(refillRate),
//                String.valueOf(now)
//        );
//
//        // Result structural verification
//        boolean allowed = result != null && !result.isEmpty() && result.get(0) == 1L;
//        long remaining = (result != null && result.size() > 1) ? result.get(1) : 0L;
//
//        return new RateLimitResult(allowed, remaining);
//    }
//
//    private DefaultRedisScript<List> loadScript() {
//        try {
//            ClassPathResource resource = new ClassPathResource("token_bucket.lua");
//            try (InputStream is = resource.getInputStream()) {
//                String script = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//                DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
//                redisScript.setScriptText(script);
//                redisScript.setResultType(List.class);
//                return redisScript;
//            }
//        } catch (IOException e) {
//            throw new RuntimeException("Could not load token_bucket.lua", e);
//        }
//    }
//}