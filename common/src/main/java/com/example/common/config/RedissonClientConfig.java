package com.example.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 分布式锁配置
 *
 * @author peppy
 */
@EnableConfigurationProperties(RedisProperties.class)
@Configuration
public class RedissonClientConfig {

    private static final String ADDRESS = "redis://%s:%s";
    private static final String NODE_ADDRESS = "redis://%s";

    @Resource
    private RedisProperties redisProperties;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        RedisProperties.Cluster cluster = redisProperties.getCluster();
        RedisProperties.Sentinel sentinel = redisProperties.getSentinel();

        Config config = new Config();
        if (Objects.nonNull(cluster) && !CollectionUtils.isEmpty(cluster.getNodes())) {
            // 集群模式
            config.useClusterServers()
                    .addNodeAddress(nodeAddress(cluster.getNodes()))
                    .setPassword(redisProperties.getPassword());
        } else if (Objects.nonNull(sentinel) && !CollectionUtils.isEmpty(sentinel.getNodes())) {
            // 哨兵模式
            config.useSentinelServers()
                    .setMasterName(sentinel.getMaster())
                    .addSentinelAddress(nodeAddress(sentinel.getNodes()))
                    .setPassword(redisProperties.getPassword());
        } else {
            // 单机模式
            config.useSingleServer()
                    .setAddress(String.format(ADDRESS, redisProperties.getHost(), redisProperties.getPort()))
                    .setPassword(redisProperties.getPassword());

        }
        return Redisson.create(config);
    }

    private String[] nodeAddress(List<String> nodeAddress) {
        return nodeAddress.stream().map(node -> String.format(NODE_ADDRESS, node)).toArray(String[]::new);
    }

}
