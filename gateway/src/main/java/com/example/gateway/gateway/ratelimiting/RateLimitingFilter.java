package com.example.gateway.gateway.ratelimiting;

import com.example.common.security.SecurityUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;
import io.github.jhipster.config.JHipsterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * zuul 过滤器, 用于限制每个客户端的 HTTP 调用次数
 *
 * @author peppy
 */
public class RateLimitingFilter extends ZuulFilter {

    private final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    public static final String GATEWAY_RATE_LIMITING_CACHE_NAME = "gateway-rate-limiting";

    private final JHipsterProperties jHipsterProperties;

    private javax.cache.Cache<String, GridBucketState> cache;

    private ProxyManager<String> buckets;

    public RateLimitingFilter(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;

        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();

        CompleteConfiguration<String, GridBucketState> config = new MutableConfiguration<String, GridBucketState>()
            .setTypes(String.class, GridBucketState.class);

        this.cache = cacheManager.createCache(GATEWAY_RATE_LIMITING_CACHE_NAME, config);
        this.buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(cache);
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    @Override
    public boolean shouldFilter() {
        // specific APIs can be filtered out using
        // if (RequestContext.getCurrentContext().getRequest().getRequestURI().startsWith("/api")) { ... }
        return true;
    }

    @Override
    public Object run() {
        String bucketId = getId(RequestContext.getCurrentContext().getRequest());
        Bucket bucket = buckets.getProxy(bucketId, getConfigSupplier());
        if (bucket.tryConsume(1)) {
            // the limit is not exceeded
            log.debug("API rate limit OK for {}", bucketId);
        } else {
            // limit is exceeded
            log.info("API rate limit exceeded for {}", bucketId);
            apiLimitExceeded();
        }
        return null;
    }

    private Supplier<BucketConfiguration> getConfigSupplier() {
        return () -> {
            JHipsterProperties.Gateway.RateLimiting rateLimitingProperties =
                jHipsterProperties.getGateway().getRateLimiting();

            return Bucket4j.configurationBuilder()
                .addLimit(Bandwidth.simple(rateLimitingProperties.getLimit(),
                    Duration.ofSeconds(rateLimitingProperties.getDurationInSeconds())))
                .build();
        };
    }

    /**
     * 超过 API 限制时创建 Zuul 响应错误
     */
    private void apiLimitExceeded() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setResponseStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody("API rate limit exceeded");
            ctx.setSendZuulResponse(false);
        }
    }

    /**
     * 标识限制的 ID：用户登录名或用户 IP 地址
     */
    private String getId(HttpServletRequest httpServletRequest) {
        return SecurityUtils.getCurrentUserLogin().orElse(httpServletRequest.getRemoteAddr());
    }

}
