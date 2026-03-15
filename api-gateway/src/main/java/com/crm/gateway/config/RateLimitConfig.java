package com.crm.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    /**
     * Resolve rate-limit bucket by authenticated user ID.
     * Falls back to remote IP for unauthenticated (public) routes.
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just(userId);
            }
            return Mono.just(
                exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "anonymous"
            );
        };
    }

    /**
     * Default limiter: 20 req/s burst, 10 req/s replenish.
     * Per-route overrides are defined in application.yml.
     */
    @Bean
    public RedisRateLimiter defaultRateLimiter() {
        return new RedisRateLimiter(10, 20, 1);
    }
}
