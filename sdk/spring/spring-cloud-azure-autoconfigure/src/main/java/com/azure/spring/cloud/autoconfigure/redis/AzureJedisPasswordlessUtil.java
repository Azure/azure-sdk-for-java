// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.redis;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

final class AzureJedisPasswordlessUtil {

    private static final int AZURE_REDIS_PORT = 6380;
    private static final boolean COMMONS_POOL2_AVAILABLE = ClassUtils.isPresent("org.apache.commons.pool2.ObjectPool",
        AzureJedisPasswordlessAutoConfiguration.class.getClassLoader());

    private AzureJedisPasswordlessUtil() {
    }

    static JedisClientConfiguration getJedisClientConfiguration(RedisProperties redisProperties) {

        JedisClientConfiguration.JedisClientConfigurationBuilder builder = applyProperties(redisProperties, JedisClientConfiguration.builder());

        RedisProperties.Pool pool = redisProperties.getJedis().getPool();

        if (isPoolEnabled(pool)) {
            applyPooling(pool, builder);
        }

        if (StringUtils.hasText(redisProperties.getUrl())) {
            customizeConfigurationFromUrl(redisProperties, builder);
        }
        return builder.build();
    }

    private static JedisClientConfiguration.JedisClientConfigurationBuilder applyProperties(RedisProperties properties, JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(properties.isSsl()).whenTrue().toCall(builder::useSsl);
        map.from(properties.getTimeout()).to(builder::readTimeout);
        map.from(properties.getConnectTimeout()).to(builder::connectTimeout);
        map.from(properties.getClientName()).whenHasText().to(builder::clientName);
        return builder;
    }

    private static void customizeConfigurationFromUrl(RedisProperties redisProperties, JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        ConnectionInfo connectionInfo = parseUrl(redisProperties.getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }
    }

    private static boolean isPoolEnabled(RedisProperties.Pool pool) {
        if (pool == null) {
            return false;
        }
        Boolean enabled = true;
        Method method = ReflectionUtils.findMethod(RedisProperties.Pool.class, "getEnabled");
        if (method != null) {
            try {
                enabled = (Boolean) method.invoke(pool);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return (enabled != null) ? enabled : COMMONS_POOL2_AVAILABLE;
    }

    private static void applyPooling(RedisProperties.Pool pool,
                                     JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        builder.usePooling().poolConfig(jedisPoolConfig(pool));
    }

    private static JedisPoolConfig jedisPoolConfig(RedisProperties.Pool pool) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());

        if (pool.getTimeBetweenEvictionRuns() != null) {
            Method method = ReflectionUtils.findMethod(RedisProperties.Pool.class, "setTimeBetweenEvictionRuns");
            if (method != null) {
                try {
                    method.invoke(pool, pool.getTimeBetweenEvictionRuns());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                config.setTimeBetweenEvictionRunsMillis(pool.getTimeBetweenEvictionRuns().toMillis());
            }
        }

        if (pool.getMaxWait() != null) {
            Method method = ReflectionUtils.findMethod(RedisProperties.Pool.class, "setMaxWait");
            if (method != null) {
                try {
                    method.invoke(pool, pool.getMaxWait());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            } else {
                config.setMaxWaitMillis(pool.getMaxWait().toMillis());
            }
        }
        return config;
    }

    static RedisStandaloneConfiguration getStandaloneConfig(RedisProperties redisProperties) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        if (StringUtils.hasText(redisProperties.getUrl())) {
            ConnectionInfo connectionInfo = parseUrl(redisProperties.getUrl());
            config.setHostName(connectionInfo.getHostName());
            config.setPort(connectionInfo.getPort());
            config.setUsername(connectionInfo.getUsername());
            config.setPassword(RedisPassword.of(connectionInfo.getPassword()));
        } else {
            config.setHostName(redisProperties.getHost());
            config.setPort(redisProperties.getPort());
            config.setUsername(redisProperties.getUsername());
            config.setPassword(RedisPassword.of(redisProperties.getPassword()));
        }
        config.setDatabase(redisProperties.getDatabase());
        if (config.getPort() == 0) {
            config.setPort(AZURE_REDIS_PORT);
        }
        return config;
    }

    static ConnectionInfo parseUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"redis".equals(scheme) && !"rediss".equals(scheme)) {
                throw new RedisUrlSyntaxException(url);
            }
            boolean useSsl = ("rediss".equals(scheme));
            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String candidate = uri.getUserInfo();
                int index = candidate.indexOf(':');
                if (index >= 0) {
                    username = candidate.substring(0, index);
                    password = candidate.substring(index + 1);
                } else {
                    password = candidate;
                }
            }
            return new ConnectionInfo(uri, useSsl, username, password);
        } catch (URISyntaxException ex) {
            throw new RedisUrlSyntaxException(url, ex);
        }
    }

    static class ConnectionInfo {

        private final URI uri;

        private final boolean useSsl;

        private final String username;

        private final String password;

        ConnectionInfo(URI uri, boolean useSsl, String username, String password) {
            this.uri = uri;
            this.useSsl = useSsl;
            this.username = username;
            this.password = password;
        }

        boolean isUseSsl() {
            return this.useSsl;
        }

        String getHostName() {
            return this.uri.getHost();
        }

        int getPort() {
            return this.uri.getPort();
        }

        String getUsername() {
            return this.username;
        }

        String getPassword() {
            return this.password;
        }

    }
}
