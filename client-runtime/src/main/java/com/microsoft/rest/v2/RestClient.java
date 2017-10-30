/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.v2.credentials.ServiceClientCredentials;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.NettyClient;
import com.microsoft.rest.v2.policy.AddCookiesPolicy;
import com.microsoft.rest.v2.policy.CredentialsPolicy;
import com.microsoft.rest.v2.policy.LoggingPolicy;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RetryPolicy;
import com.microsoft.rest.v2.policy.UserAgentPolicy;
import com.microsoft.rest.v2.protocol.Environment;
import com.microsoft.rest.v2.protocol.SerializerAdapter;
import com.microsoft.rest.v2.serializer.JacksonAdapter;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An instance of this class stores configuration for setting up specific service clients.
 */
public final class RestClient {
    private final HttpClient.Factory httpClientFactory;
    private final HttpClient httpClient;
    private final Proxy proxy;
    private final String baseURL;
    private final String userAgent;
    private final long readTimeoutMillis;
    private final long connectionTimeoutMillis;
    private final SerializerAdapter<?> serializerAdapter;
    private final ServiceClientCredentials credentials;
    private final LogLevel logLevel;

    private final List<RequestPolicy.Factory> customPolicyFactories;

    private RestClient(RestClient.Builder builder) {
        this.proxy = builder.proxy;
        this.baseURL = builder.baseUrl;
        this.userAgent = builder.userAgent;
        this.readTimeoutMillis = builder.readTimeoutMillis;
        this.connectionTimeoutMillis = builder.connectionTimeoutMillis;
        this.serializerAdapter = builder.serializerAdapter;
        this.credentials = builder.credentials;
        this.logLevel = builder.logLevel;
        this.customPolicyFactories = builder.customPolicyFactories;

        this.httpClientFactory = builder.httpClientFactory;

        List<RequestPolicy.Factory> policyFactories = new ArrayList<>();
        policyFactories.add(new UserAgentPolicy.Factory(userAgent));
        policyFactories.add(new RetryPolicy.Factory());
        policyFactories.add(new AddCookiesPolicy.Factory());
        if (credentials != null) {
            policyFactories.add(new CredentialsPolicy.Factory(credentials));
        }
        policyFactories.addAll(customPolicyFactories);
        policyFactories.add(new LoggingPolicy.Factory(logLevel));

        HttpClient.Configuration configuration = new HttpClient.Configuration(policyFactories, proxy);
        this.httpClient = httpClientFactory.create(configuration);
    }

    /**
     * @return the user-defined request policy factories.
     */
    public List<RequestPolicy.Factory> customPolicyFactories() {
        return customPolicyFactories;
    }

    /**
     * @return the current serializer adapter.
     */
    public SerializerAdapter<?> serializerAdapter() {
        return serializerAdapter;
    }

    /**
     * @return the {@link HttpClient} instance
     */
    public HttpClient httpClient() {
        return httpClient;
    }

    /**
     * @return the {@link Proxy} to use
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * @return the base URL to make requests to.
     */
    public String baseURL() {
        return baseURL;
    }

    /**
     * @return the connection timeout for HTTP connections in milliseconds.
     */
    public long connectionTimeoutMillis() {
        return connectionTimeoutMillis;
    }

    /**
     * @return the credentials attached to this REST client
     */
    public ServiceClientCredentials credentials() {
        return credentials;
    }

    /**
     * @return the current HTTP traffic logging level
     */
    public LogLevel logLevel() {
        return logLevel;
    }

    /**
     * Create a new builder for a new Rest Client with the same configurations on this one.
     * @return a RestClient builder
     */
    RestClient.Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * @return The user agent string to send in HTTP requests.
     */
    public String userAgent() {
        return userAgent;
    }

    /**
     * @return a new initialized instance of the default SerializerAdapter type.
     */
    public static SerializerAdapter<?> createDefaultSerializer() {
        return new JacksonAdapter();
    }

    /**
     * The builder class for building a REST client.
     */
    public static class Builder {
        private final long defaultReadTimeoutMillis = 10000;
        private final long defaultConnectionTimeoutMillis = 10000;

        private HttpClient.Factory httpClientFactory;
        private Proxy proxy;
        /** The dynamic base URL with variables wrapped in "{" and "}". */
        private String baseUrl;
        /** The credentials to authenticate. */
        private ServiceClientCredentials credentials;

        private List<RequestPolicy.Factory> customPolicyFactories = new ArrayList<>();

        /** The value for 'User-Agent' header. */
        private String userAgent;
        private long readTimeoutMillis = defaultReadTimeoutMillis;
        private long connectionTimeoutMillis = defaultConnectionTimeoutMillis;
        /** The adapter for serializations and deserializations. */
        private SerializerAdapter<?> serializerAdapter;
        /** The logging level to use. */
        private LogLevel logLevel = LogLevel.NONE;

        private Builder(final RestClient restClient) {
            this.httpClientFactory = restClient.httpClientFactory;
            this.proxy = restClient.proxy;
            this.baseUrl = restClient.baseURL;
            this.userAgent = restClient.userAgent;
            this.connectionTimeoutMillis = restClient.connectionTimeoutMillis;
            this.readTimeoutMillis = restClient.readTimeoutMillis;
            this.serializerAdapter = restClient.serializerAdapter;
            this.credentials = restClient.credentials;
            this.customPolicyFactories = new ArrayList<>(restClient.customPolicyFactories);
            this.logLevel = restClient.logLevel;
        }

        /**
         * Creates an instance of the builder.
         */
        public Builder() {
            this.httpClientFactory = new NettyClient.Factory();
        }

        /**
         * Sets the httpClientFactory.
         * @param httpClientFactory the httpClientFactory to use.
         * @return the builder itself for chaining.
         */
        public Builder withHttpClientFactory(HttpClient.Factory httpClientFactory) {
            this.httpClientFactory = httpClientFactory;
            return this;
        }

        /**
         * Sets the proxy.
         * @param proxy the proxy to use.
         * @return the builder itself for chaining.
         */
        public Builder withProxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        /**
         * Sets the dynamic base URL.
         *
         * @param baseUrl the base URL to use.
         * @return the builder itself for chaining.
         */
        public Builder withBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Sets the base URL with the default from the Environment.
         *
         * @param environment the environment to use
         * @param endpoint the environment endpoint the application is accessing
         * @return the builder itself for chaining
         */
        public Builder withBaseUrl(Environment environment, Environment.Endpoint endpoint) {
            this.baseUrl = environment.url(endpoint);
            return this;
        }

        /**
         * Sets the serialization adapter.
         *
         * @param serializerAdapter the adapter to a serializer
         * @return the builder itself for chaining
         */
        public Builder withSerializerAdapter(SerializerAdapter<?> serializerAdapter) {
            this.serializerAdapter = serializerAdapter;
            return this;
        }

        /**
         * Sets the credentials.
         *
         * @param credentials the credentials object.
         * @return the builder itself for chaining.
         */
        public Builder withCredentials(ServiceClientCredentials credentials) {
            if (credentials == null) {
                throw new NullPointerException("credentials == null");
            }
            this.credentials = credentials;
            return this;
        }

        /**
         * Sets the user agent header.
         *
         * @param userAgent the user agent header.
         * @return the builder itself for chaining.
         */
        public Builder withUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        /**
         * Sets the HTTP log level.
         *
         * @param logLevel the {@link LogLevel} enum.
         * @return the builder itself for chaining.
         */
        public Builder withLogLevel(LogLevel logLevel) {
            if (logLevel == null) {
                throw new NullPointerException("logLevel == null");
            }
            this.logLevel = logLevel;
            return this;
        }

        /**
         * Set the read timeout on the HTTP client. Default is 10 seconds.
         *
         * @param timeout the timeout numeric value
         * @param unit the time unit for the numeric value
         * @return the builder itself for chaining
         */
        public Builder withReadTimeout(long timeout, TimeUnit unit) {
            readTimeoutMillis = unit.toMillis(timeout);
            return this;
        }

        /**
         * Set the connection timeout on the HTTP client. Default is 10 seconds.
         *
         * @param timeout the timeout numeric value
         * @param unit the time unit for the numeric value
         * @return the builder itself for chaining
         */
        public Builder withConnectionTimeout(long timeout, TimeUnit unit) {
            connectionTimeoutMillis = unit.toMillis(timeout);
            return this;
        }

        /**
         * Set the maximum idle connections for the HTTP client. Default is 5.
         *
         * @param maxIdleConnections the maximum idle connections
         * @return the builder itself for chaining
         */
        public Builder withMaxIdleConnections(int maxIdleConnections) {
            // FIXME -- maybe by deleting this method?
            // Seems like a configuration on a concrete HTTP client
            throw new RuntimeException();
//            httpClientBuilder.connectionPool(new ConnectionPool(maxIdleConnections, 5, TimeUnit.MINUTES));
        }

        /**
         * Adds a custom RequestPolicyFactory to the request pipeline.
         * @param factory The Factory producing a custom user-defined RequestPolicy.
         * @return The builder.
         */
        public Builder addCustomPolicy(RequestPolicy.Factory factory) {
            customPolicyFactories.add(factory);
            return this;
        }

        /**
         * Build a RestClient with all the current configurations.
         *
         * @return a {@link RestClient}.
         */
        public RestClient build() {
            if (baseUrl == null) {
                throw new IllegalArgumentException("Please set base URL.");
            }
            if (serializerAdapter == null) {
                throw new IllegalArgumentException("Please set serializer adapter.");
            }

            return new RestClient(this);
        }
    }
}
