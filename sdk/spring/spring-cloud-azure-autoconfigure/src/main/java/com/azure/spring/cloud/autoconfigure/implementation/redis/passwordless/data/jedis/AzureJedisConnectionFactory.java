// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.redis.passwordless.data.jedis;

import com.azure.spring.cloud.service.implementation.redis.AzureJedisClientConfig;
import com.azure.spring.cloud.service.implementation.redis.AzureJedisPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.util.Pool;

import java.util.function.Supplier;

/**
 * Connection factory creating Jedis based connections to Azure Redis.
 *
 */
public class AzureJedisConnectionFactory implements InitializingBean, DisposableBean, RedisConnectionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureJedisConnectionFactory.class);

    private final RedisStandaloneConfiguration standaloneConfig;

    private final Supplier<String> credentialSupplier;
    private final JedisClientConfiguration clientConfiguration;
    private JedisClientConfig jedisClientConfig;
    private @Nullable Pool<Jedis> pool;
    private boolean initialized;
    private boolean destroyed;

    private boolean convertPipelineAndTxResults = true;

    /**
     * Constructs a new AzureJedisConnectionFactory instance using the given RedisStandaloneConfiguration, JedisClientConfiguration and CredentialSupplier.
     *
     * @param standaloneConfig must not be {@literal null}.
     * @param clientConfiguration must not be {@literal null}.
     * @param credentialSupplier must not be {@literal null}.
     */
    public AzureJedisConnectionFactory(RedisStandaloneConfiguration standaloneConfig, JedisClientConfiguration clientConfiguration, Supplier<String> credentialSupplier) {
        this.standaloneConfig = standaloneConfig;
        this.clientConfiguration = clientConfiguration;
        this.credentialSupplier = credentialSupplier;
    }

    @Override
    public void afterPropertiesSet() {
        this.jedisClientConfig = createClientConfig(this.standaloneConfig, this.clientConfiguration);

        if (getUsePool()) {
            this.pool = createRedisPool();
        }
        this.initialized = true;
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return this.convertPipelineAndTxResults;
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public void destroy() {
        if (getUsePool() && pool != null) {

            try {
                pool.destroy();
            } catch (Exception ex) {
                LOGGER.warn("Cannot properly close Jedis pool", ex);
            }
            pool = null;
        }

        this.destroyed = true;
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        throw new UnsupportedOperationException("This operation is not supported");
    }

    @Override
    public RedisConnection getConnection() {
        assertInitialized();

        Jedis jedis = fetchJedisConnector();

        JedisClientConfig sentinelConfig = this.jedisClientConfig;

        JedisConnection connection = (getUsePool() ? new AzureJedisConnection(jedis, pool, this.jedisClientConfig, sentinelConfig)
            : new AzureJedisConnection(jedis, null, this.jedisClientConfig, sentinelConfig));
        connection.setConvertPipelineAndTxResults(convertPipelineAndTxResults);
        return postProcessConnection(connection);
    }

    /**
     * Specifies if pipelined results should be converted to the expected data type. If false, results of
     * {@link JedisConnection#closePipeline()} and {@link JedisConnection#exec()} will be of the type returned by the
     * Jedis driver.
     *
     * @param convertPipelineAndTxResults Whether or not to convert pipeline and tx results.
     */
    public void setConvertPipelineAndTxResults(boolean convertPipelineAndTxResults) {
        this.convertPipelineAndTxResults = convertPipelineAndTxResults;
    }

    /**
     * Indicates the use of a connection pool.
     * <p>
     * Applies only to single node Redis.
     *
     * @return the use of connection pooling.
     */
    public boolean getUsePool() {
        return clientConfiguration.isUsePooling();
    }

    private JedisConnection postProcessConnection(JedisConnection connection) {
        return connection;
    }

    @SuppressWarnings("unchecked")
    private Pool<Jedis> createRedisPool() {
        return new AzureJedisPool(this.clientConfiguration.getPoolConfig().get(),
            new HostAndPort(this.standaloneConfig.getHostName(), this.standaloneConfig.getPort()),
            this.jedisClientConfig);
    }

    /**
     * Returns a Jedis instance to be used as a Redis connection. The instance can be newly created or retrieved from a
     * pool.
     *
     * @return Jedis instance ready for wrapping into a {@link RedisConnection}.
     * @throws RedisConnectionFailureException when can't fetch a jedis instance.
     */
    protected Jedis fetchJedisConnector() {
        try {

            if (getUsePool() && pool != null) {
                LOGGER.info("Get connection from pool.");
                return pool.getResource();
            }

            Jedis jedis = createJedis();
            // force initialization (see Jedis issue #82)
            jedis.connect();

            return jedis;
        } catch (Exception ex) {
            throw new RedisConnectionFailureException("Cannot get Jedis connection", ex);
        }
    }

    private Jedis createJedis() {
        return new Jedis(new HostAndPort(this.standaloneConfig.getHostName(), this.standaloneConfig.getPort()), this.jedisClientConfig);
    }

    private JedisClientConfig createClientConfig(RedisStandaloneConfiguration standaloneConfig,
                                                 JedisClientConfiguration clientConfig) {

        String username = standaloneConfig.getUsername();
        RedisPassword password = standaloneConfig.getPassword();

        AzureJedisClientConfig.Builder builder = AzureJedisClientConfig.builder();

        clientConfig.getClientName().ifPresent(builder::clientName);
        builder.connectionTimeoutMillis(Math.toIntExact(clientConfig.getConnectTimeout().toMillis()));
        builder.socketTimeoutMillis(Math.toIntExact(clientConfig.getReadTimeout().toMillis()));

        builder.database(standaloneConfig.getDatabase());

        if (!ObjectUtils.isEmpty(username)) {
            builder.user(username);
        }

        password.toOptional().map(String::new).ifPresent(builder::password);

        builder.credentialSupplier(credentialSupplier);

        if (clientConfig.isUseSsl()) {

            builder.ssl(true);

            clientConfig.getSslSocketFactory().ifPresent(builder::sslSocketFactory);
            clientConfig.getHostnameVerifier().ifPresent(builder::hostnameVerifier);
            clientConfig.getSslParameters().ifPresent(builder::sslParameters);
        }

        return builder.build();
    }

    private void assertInitialized() {
        Assert.state(this.initialized, "JedisConnectionFactory was not initialized through afterPropertiesSet()");
        Assert.state(!this.destroyed, "JedisConnectionFactory was destroyed and cannot be used anymore");
    }

    void setPool(@Nullable Pool<Jedis> pool) {
        this.pool = pool;
    }
}
