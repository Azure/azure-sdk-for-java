// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.redis;

import redis.clients.jedis.HostAndPortMapper;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.Protocol;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocketFactory;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * JedisClientConfig implementation used for Azure Redis.
 */
public final class AzureJedisClientConfig implements JedisClientConfig {

    private final int connectionTimeoutMillis;
    private final int socketTimeoutMillis;
    private final int blockingSocketTimeoutMillis;

    private final String user;
    private volatile String password;
    private final int database;
    private final String clientName;

    private final boolean ssl;
    private final SSLSocketFactory sslSocketFactory;
    private final SSLParameters sslParameters;
    private final HostnameVerifier hostnameVerifier;

    private final HostAndPortMapper hostAndPortMapper;

    private Supplier<String> credentialSupplier;

    private AzureJedisClientConfig(int connectionTimeoutMillis, int soTimeoutMillis,
                                   int blockingSocketTimeoutMillis, String user, String password, int database, String clientName,
                                   boolean ssl, SSLSocketFactory sslSocketFactory, SSLParameters sslParameters,
                                   HostnameVerifier hostnameVerifier, HostAndPortMapper hostAndPortMapper, Supplier<String> credentialSupplier) {
        this.connectionTimeoutMillis = connectionTimeoutMillis;
        this.socketTimeoutMillis = soTimeoutMillis;
        this.blockingSocketTimeoutMillis = blockingSocketTimeoutMillis;
        this.user = user;
        this.password = password;
        this.database = database;
        this.clientName = clientName;
        this.ssl = ssl;
        this.sslSocketFactory = sslSocketFactory;
        this.sslParameters = sslParameters;
        this.hostnameVerifier = hostnameVerifier;
        this.hostAndPortMapper = hostAndPortMapper;
        this.credentialSupplier = credentialSupplier;
    }

    @Override
    public int getConnectionTimeoutMillis() {
        return connectionTimeoutMillis;
    }

    @Override
    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    @Override
    public int getBlockingSocketTimeoutMillis() {
        return blockingSocketTimeoutMillis;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String getPassword() {
        if (this.password != null) {
            return this.password;
        }
        if (this.credentialSupplier != null) {
            return this.credentialSupplier.get();
        }
        return this.password;
    }

    @Override
    public synchronized void updatePassword(String password) {
        if (!Objects.equals(this.password, password)) {
            this.password = password;
        }
    }

    @Override
    public int getDatabase() {
        return database;
    }

    @Override
    public String getClientName() {
        return clientName;
    }

    @Override
    public boolean isSsl() {
        return ssl;
    }

    @Override
    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    @Override
    public SSLParameters getSslParameters() {
        return sslParameters;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    @Override
    public HostAndPortMapper getHostAndPortMapper() {
        return hostAndPortMapper;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private int connectionTimeoutMillis = Protocol.DEFAULT_TIMEOUT;
        private int socketTimeoutMillis = Protocol.DEFAULT_TIMEOUT;
        private int blockingSocketTimeoutMillis = 0;

        private String user = null;
        private String password = null;

        private Supplier<String> credentialSupplier = null;
        private int database = Protocol.DEFAULT_DATABASE;
        private String clientName = null;

        private boolean ssl = false;
        private SSLSocketFactory sslSocketFactory = null;
        private SSLParameters sslParameters = null;
        private HostnameVerifier hostnameVerifier = null;

        private HostAndPortMapper hostAndPortMapper = null;

        private Builder() {
        }

        public AzureJedisClientConfig build() {
            return new AzureJedisClientConfig(connectionTimeoutMillis, socketTimeoutMillis,
                blockingSocketTimeoutMillis, user, password, database, clientName, ssl, sslSocketFactory,
                sslParameters, hostnameVerifier, hostAndPortMapper, credentialSupplier);
        }

        public Builder connectionTimeoutMillis(int connectionTimeoutMillis) {
            this.connectionTimeoutMillis = connectionTimeoutMillis;
            return this;
        }

        public Builder socketTimeoutMillis(int socketTimeoutMillis) {
            this.socketTimeoutMillis = socketTimeoutMillis;
            return this;
        }

        public Builder blockingSocketTimeoutMillis(int blockingSocketTimeoutMillis) {
            this.blockingSocketTimeoutMillis = blockingSocketTimeoutMillis;
            return this;
        }

        public Builder user(String user) {
            this.user = user;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder credentialSupplier(Supplier<String> credentialSupplier) {
            this.credentialSupplier = credentialSupplier;
            return this;
        }

        public Builder database(int database) {
            this.database = database;
            return this;
        }

        public Builder clientName(String clientName) {
            this.clientName = clientName;
            return this;
        }

        public Builder ssl(boolean ssl) {
            this.ssl = ssl;
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }

        public Builder sslParameters(SSLParameters sslParameters) {
            this.sslParameters = sslParameters;
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder hostAndPortMapper(HostAndPortMapper hostAndPortMapper) {
            this.hostAndPortMapper = hostAndPortMapper;
            return this;
        }
    }

}
