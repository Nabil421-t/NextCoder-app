package com.cuet.dsa.config;

import com.cuet.dsa.security.RateLimitingFilter;
import com.cuet.dsa.service.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ✅ recommended setup for your UserController
@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final RateLimiterService  rateLimiterService ;

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitingFilter> reg
                = new FilterRegistrationBean<>();

        reg.setFilter(new RateLimitingFilter(rateLimiterService ));
        reg.addUrlPatterns("/api/*");
        reg.setOrder(1);
        return reg;
    }
}
