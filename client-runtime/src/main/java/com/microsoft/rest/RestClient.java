/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.interceptors.BaseUrlHandler;
import com.microsoft.rest.interceptors.CustomHeadersInterceptor;
import com.microsoft.rest.interceptors.LoggingInterceptor;
import com.microsoft.rest.interceptors.RequestIdHeaderInterceptor;
import com.microsoft.rest.interceptors.UserAgentInterceptor;
import com.microsoft.rest.protocol.Environment;
import com.microsoft.rest.protocol.ResponseBuilder;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.retry.RetryHandler;
import com.microsoft.rest.retry.RetryStrategy;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.Proxy;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * An instance of this class stores the client information for making REST calls.
 */
public final class RestClient {
    /** The {@link okhttp3.OkHttpClient} object. */
    private final OkHttpClient httpClient;
    /** The {@link retrofit2.Retrofit} object. */
    private final Retrofit retrofit;
    /** The credentials to authenticate. */
    private final ServiceClientCredentials credentials;
    /** The interceptor to handle custom headers. */
    private final CustomHeadersInterceptor customHeadersInterceptor;
    /** The adapter for a serializer. */
    private final SerializerAdapter<?> serializerAdapter;
    /** The builder factory for response builders. */
    private final ResponseBuilder.Factory responseBuilderFactory;
    /** The logging interceptor to use. */
    private final LoggingInterceptor loggingInterceptor;

    private RestClient(OkHttpClient httpClient,
                       Retrofit retrofit,
                       ServiceClientCredentials credentials,
                       CustomHeadersInterceptor customHeadersInterceptor,
                       LoggingInterceptor loggingInterceptor,
                       SerializerAdapter<?> serializerAdapter,
                       ResponseBuilder.Factory responseBuilderFactory) {
        this.httpClient = httpClient;
        this.retrofit = retrofit;
        this.credentials = credentials;
        this.customHeadersInterceptor = customHeadersInterceptor;
        this.serializerAdapter = serializerAdapter;
        this.responseBuilderFactory = responseBuilderFactory;
        this.loggingInterceptor = loggingInterceptor;
    }

    /**
     * @return the headers interceptor.
     */
    public CustomHeadersInterceptor headers() {
        return customHeadersInterceptor;
    }

    /**
     * @return the current serializer adapter.
     */
    public SerializerAdapter<?> serializerAdapter() {
        return serializerAdapter;
    }

    /**
     * @return the current respnose builder factory.
     */
    public ResponseBuilder.Factory responseBuilderFactory() {
        return responseBuilderFactory;
    }

    /**
     * @return the {@link OkHttpClient} instance
     */
    public OkHttpClient httpClient() {
        return httpClient;
    }

    /**
     * @return the {@link Retrofit} instance
     */
    public Retrofit retrofit() {
        return retrofit;
    }

    /**
     * @return the credentials attached to this REST client
     */
    public ServiceClientCredentials credentials() {
        return this.credentials;
    }

    /**
     * @return the current HTTP traffic logging level
     */
    public LogLevel logLevel() {
        return loggingInterceptor.logLevel();
    }

    /**
     * Set the current HTTP traffic logging level.
     * @param logLevel the logging level enum
     * @return the RestClient itself
     */
    public RestClient withLogLevel(LogLevel logLevel) {
        this.loggingInterceptor.withLogLevel(logLevel);
        return this;
    }

    /**
     * Create a new builder for a new Rest Client with the same configurations on this one.
     * @return a RestClient builder
     */
    public RestClient.Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * The builder class for building a REST client.
     */
    public static class Builder {
        /** The dynamic base URL with variables wrapped in "{" and "}". */
        private String baseUrl;
        /** The builder to build an {@link OkHttpClient}. */
        private OkHttpClient.Builder httpClientBuilder;
        /** The builder to build a {@link Retrofit}. */
        private Retrofit.Builder retrofitBuilder;
        /** The credentials to authenticate. */
        private ServiceClientCredentials credentials;
        /** The interceptor to handle custom headers. */
        private CustomHeadersInterceptor customHeadersInterceptor;
        /** The value for 'User-Agent' header. */
        private String userAgent;
        /** The adapter for serializations and deserializations. */
        private SerializerAdapter<?> serializerAdapter;
        /** The builder factory for response builders. */
        private ResponseBuilder.Factory responseBuilderFactory;
        /** The logging interceptor to use. */
        private LoggingInterceptor loggingInterceptor;
        /** The strategy used for retry failed requests. */
        private RetryStrategy retryStrategy;

        /**
         * Creates an instance of the builder with a base URL to the service.
         */
        public Builder() {
            this(new OkHttpClient.Builder(), new Retrofit.Builder());
        }

        private Builder(final RestClient restClient) {
            this(restClient.httpClient.newBuilder(), new Retrofit.Builder());
            this.httpClientBuilder.interceptors().clear();
            this.httpClientBuilder.networkInterceptors().clear();
            this.baseUrl = restClient.retrofit.baseUrl().toString();
            this.responseBuilderFactory = restClient.responseBuilderFactory;
            this.serializerAdapter = restClient.serializerAdapter;
            if (restClient.credentials != null) {
                this.credentials = restClient.credentials;
            }
            if (restClient.retrofit.callbackExecutor() != null) {
                this.withCallbackExecutor(restClient.retrofit.callbackExecutor());
            }
            for (Interceptor interceptor : restClient.httpClient.interceptors()) {
                if (interceptor instanceof UserAgentInterceptor) {
                    this.userAgent = ((UserAgentInterceptor) interceptor).userAgent();
                } else if (interceptor instanceof RetryHandler) {
                    this.retryStrategy = ((RetryHandler) interceptor).strategy();
                } else if (interceptor instanceof RequestIdHeaderInterceptor
                    || interceptor instanceof BaseUrlHandler) {
                } else if (interceptor instanceof CustomHeadersInterceptor) {
                    this.customHeadersInterceptor = new CustomHeadersInterceptor();
                    this.customHeadersInterceptor.addHeaderMultimap(((CustomHeadersInterceptor) interceptor).headers());
                } else {
                    this.withInterceptor(interceptor);
                }
            }
            for (Interceptor interceptor : restClient.httpClient.networkInterceptors()) {
                if (interceptor instanceof LoggingInterceptor) {
                    LoggingInterceptor old = (LoggingInterceptor) interceptor;
                    this.loggingInterceptor = new LoggingInterceptor(old.logLevel());
                } else {
                    this.withNetworkInterceptor(interceptor);
                }
            }
        }

        /**
         * Creates an instance of the builder with a base URL and 2 custom builders.
         *
         * @param httpClientBuilder the builder to build an {@link OkHttpClient}.
         * @param retrofitBuilder the builder to build a {@link Retrofit}.
         */
        public Builder(OkHttpClient.Builder httpClientBuilder, Retrofit.Builder retrofitBuilder) {
            if (httpClientBuilder == null) {
                throw new IllegalArgumentException("httpClientBuilder == null");
            }
            if (retrofitBuilder == null) {
                throw new IllegalArgumentException("retrofitBuilder == null");
            }
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            customHeadersInterceptor = new CustomHeadersInterceptor();
            // Set up OkHttp client
            this.httpClientBuilder = httpClientBuilder
                    .cookieJar(new JavaNetCookieJar(cookieManager))
                    .readTimeout(60, TimeUnit.SECONDS);
            this.retrofitBuilder = retrofitBuilder;
            this.loggingInterceptor = new LoggingInterceptor(LogLevel.NONE);
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
         * Sets the response builder factory.
         *
         * @param responseBuilderFactory the response builder factory
         * @return the builder itself for chaining
         */
        public Builder withResponseBuilderFactory(ResponseBuilder.Factory responseBuilderFactory) {
            this.responseBuilderFactory = responseBuilderFactory;
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
            credentials.applyCredentialsFilter(httpClientBuilder);

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
            this.loggingInterceptor.withLogLevel(logLevel);
            return this;
        }

        /**
         * Add an interceptor the Http client pipeline.
         *
         * @param interceptor the interceptor to add.
         * @return the builder itself for chaining.
         */
        public Builder withInterceptor(Interceptor interceptor) {
            if (interceptor == null) {
                throw new NullPointerException("interceptor == null");
            }
            httpClientBuilder.addInterceptor(interceptor);
            return this;
        }

        /**
         * Add an interceptor the network layer of Http client pipeline.
         *
         * @param networkInterceptor the interceptor to add.
         * @return the builder itself for chaining.
         */
        public Builder withNetworkInterceptor(Interceptor networkInterceptor) {
            if (networkInterceptor == null) {
                throw new NullPointerException("networkInterceptor == null");
            }
            httpClientBuilder.addNetworkInterceptor(networkInterceptor);
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
            httpClientBuilder.readTimeout(timeout, unit);
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
            httpClientBuilder.connectTimeout(timeout, unit);
            return this;
        }

        /**
         * Set the maximum idle connections for the HTTP client. Default is 5.
         *
         * @param maxIdleConnections the maximum idle connections
         * @return the builder itself for chaining
         */
        public Builder withMaxIdleConnections(int maxIdleConnections) {
            httpClientBuilder.connectionPool(new ConnectionPool(maxIdleConnections, 5, TimeUnit.MINUTES));
            return this;
        }

        /**
         * Sets the executor for async callbacks to run on.
         *
         * @param executor the executor to execute the callbacks.
         * @return the builder itself for chaining
         */
        public Builder withCallbackExecutor(Executor executor) {
            retrofitBuilder.callbackExecutor(executor);
            return this;
        }

        /**
         * Sets the proxy for the HTTP client.
         *
         * @param proxy the proxy to use
         * @return the builder itself for chaining
         */
        public Builder withProxy(Proxy proxy) {
            httpClientBuilder.proxy(proxy);
            return this;
        }

        /**
         * Sets the proxy authenticator for the HTTP client.
         *
         * @param proxyAuthenticator the proxy authenticator to use
         * @return the builder itself for chaining
         */
        public Builder withProxyAuthenticator(Authenticator proxyAuthenticator) {
            httpClientBuilder.proxyAuthenticator(proxyAuthenticator);
            return this;
        }

        /**
         * Adds a retry strategy to the client.
         * @param strategy the retry strategy to add
         * @return the builder itself for chaining
         */
        public Builder withRetryStrategy(RetryStrategy strategy) {
            this.retryStrategy = retryStrategy;
            return this;
        }

        /**
         * Build a RestClient with all the current configurations.
         *
         * @return a {@link RestClient}.
         */
        public RestClient build() {
            UserAgentInterceptor userAgentInterceptor = new UserAgentInterceptor();
            if (userAgent != null) {
                userAgentInterceptor.withUserAgent(userAgent);
            }
            if (baseUrl == null) {
                throw new IllegalArgumentException("Please set base URL.");
            }
            if (responseBuilderFactory == null) {
                throw new IllegalArgumentException("Please set response builder factory.");
            }
            if (serializerAdapter == null) {
                throw new IllegalArgumentException("Please set serializer adapter.");
            }
            RetryHandler retryHandler;
            if (retryStrategy == null) {
                retryHandler = new RetryHandler();
            } else {
                retryHandler = new RetryHandler(retryStrategy);
            }
            OkHttpClient httpClient = httpClientBuilder
                    .addInterceptor(userAgentInterceptor)
                    .addInterceptor(new RequestIdHeaderInterceptor())
                    .addInterceptor(new BaseUrlHandler())
                    .addInterceptor(customHeadersInterceptor)
                    .addInterceptor(retryHandler)
                    .addNetworkInterceptor(loggingInterceptor)
                    .build();
            return new RestClient(httpClient,
                    retrofitBuilder
                            .baseUrl(baseUrl)
                            .client(httpClient)
                            .addConverterFactory(serializerAdapter.converterFactory())
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .build(),
                    credentials,
                    customHeadersInterceptor,
                    loggingInterceptor,
                    serializerAdapter,
                    responseBuilderFactory);
        }
    }
}
