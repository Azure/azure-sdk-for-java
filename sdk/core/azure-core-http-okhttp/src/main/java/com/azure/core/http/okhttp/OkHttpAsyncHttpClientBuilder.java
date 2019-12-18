// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.AuthorizationChallengeHandler;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.Challenge;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Builder to configure and build an implementation of {@link HttpClient} for OkHttp.
 */
public class OkHttpAsyncHttpClientBuilder {
    private final ClientLogger logger = new ClientLogger(OkHttpAsyncHttpClientBuilder.class);

    private final okhttp3.OkHttpClient okHttpClient;

    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(120);
    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(60);

    private List<Interceptor> networkInterceptors = new ArrayList<>();
    private Duration readTimeout;
    private Duration connectionTimeout;
    private ConnectionPool connectionPool;
    private Dispatcher dispatcher;
    private ProxyOptions proxyOptions;

    /**
     * Creates OkHttpAsyncHttpClientBuilder.
     */
    public OkHttpAsyncHttpClientBuilder() {
        this.okHttpClient = null;
    }

    /**
     * Creates OkHttpAsyncHttpClientBuilder from the builder of an existing OkHttpClient.
     *
     * @param okHttpClient the httpclient
     */
    public OkHttpAsyncHttpClientBuilder(OkHttpClient okHttpClient) {
        this.okHttpClient = Objects.requireNonNull(okHttpClient, "'okHttpClient' cannot be null.");
    }

    /**
     * Add a network layer interceptor to Http request pipeline.
     *
     * @param networkInterceptor the interceptor to add
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder addNetworkInterceptor(Interceptor networkInterceptor) {
        Objects.requireNonNull(networkInterceptor, "'networkInterceptor' cannot be null.");
        this.networkInterceptors.add(networkInterceptor);
        return this;
    }

    /**
     * Add network layer interceptors to Http request pipeline.
     *
     * This replaces all previously-set interceptors.
     *
     * @param networkInterceptors the interceptors to add
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder networkInterceptors(List<Interceptor> networkInterceptors) {
        this.networkInterceptors = Objects.requireNonNull(networkInterceptors, "'networkInterceptors' cannot be null.");
        return this;
    }

    /**
     * Sets the read timeout.
     *
     * The default read timeout is 120 seconds.
     *
     * @param readTimeout the read timeout
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder readTimeout(Duration readTimeout) {
        // setReadTimeout can be null
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     * Sets the connection timeout.
     *
     * The default connection timeout is 60 seconds.
     *
     * @param connectionTimeout the connection timeout
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder connectionTimeout(Duration connectionTimeout) {
        // setConnectionTimeout can be null
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    /**
     * Sets the Http connection pool.
     *
     * @param connectionPool the OkHttp connection pool to use
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder connectionPool(ConnectionPool connectionPool) {
        // Null ConnectionPool is not allowed
        this.connectionPool = Objects.requireNonNull(connectionPool, "'connectionPool' cannot be null.");
        return this;
    }

    /**
     * Sets the dispatcher that also composes the thread pool for executing HTTP requests.
     *
     * @param dispatcher the dispatcher to use
     * @return the updated OkHttpAsyncHttpClientBuilder object
     */
    public OkHttpAsyncHttpClientBuilder dispatcher(Dispatcher dispatcher) {
        // Null Dispatcher is not allowed
        this.dispatcher = Objects.requireNonNull(dispatcher, "'dispatcher' cannot be null.");
        return this;
    }

    /**
     * Sets the proxy.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder.proxy#ProxyOptions}
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated {@link OkHttpAsyncHttpClientBuilder} object
     */
    public OkHttpAsyncHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        // proxyOptions can be null
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Build a HttpClient with current configurations.
     *
     * @return a {@link HttpClient}.
     */
    public HttpClient build() {
        OkHttpClient.Builder httpClientBuilder = this.okHttpClient == null
            ? new OkHttpClient.Builder()
            : this.okHttpClient.newBuilder();
        //
        for (Interceptor interceptor : this.networkInterceptors) {
            httpClientBuilder = httpClientBuilder.addNetworkInterceptor(interceptor);
        }
        if (this.readTimeout != null) {
            httpClientBuilder = httpClientBuilder.readTimeout(this.readTimeout);
        } else {
            httpClientBuilder = httpClientBuilder.readTimeout(DEFAULT_READ_TIMEOUT);
        }
        if (this.connectionTimeout != null) {
            httpClientBuilder = httpClientBuilder.connectTimeout(this.connectionTimeout);
        } else {
            httpClientBuilder = httpClientBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT);
        }
        if (this.connectionPool != null) {
            httpClientBuilder = httpClientBuilder.connectionPool(connectionPool);
        }
        if (this.dispatcher != null) {
            httpClientBuilder = httpClientBuilder.dispatcher(dispatcher);
        }
        if (proxyOptions != null) {
            Proxy.Type proxyType;
            switch (proxyOptions.getType()) {
                case HTTP:
                    proxyType = Proxy.Type.HTTP;
                    break;
                case SOCKS4:
                case SOCKS5:
                    // JDK Proxy.Type.SOCKS identifies SOCKS V4 and V5 proxy.
                    proxyType = Proxy.Type.SOCKS;
                    break;
                default:
                    throw logger.logExceptionAsError(new IllegalStateException(
                        String.format("Unknown Proxy type '%s' in use. Not configuring OkHttp proxy.",
                            proxyOptions.getType())));
            }
            Proxy proxy = new Proxy(proxyType, this.proxyOptions.getAddress());
            httpClientBuilder = httpClientBuilder.proxy(proxy);
            if (proxyOptions.getUsername() != null) {
                AuthorizationChallengeHandler challengeHandler =
                    new AuthorizationChallengeHandler(proxyOptions.getUsername(), proxyOptions.getPassword());

                httpClientBuilder = httpClientBuilder.proxyAuthenticator((route, response) -> {
                    List<Challenge> basicChallenges = response.challenges().stream()
                        .filter(challenge -> "Basic".equalsIgnoreCase(challenge.scheme()))
                        .collect(Collectors.toList());

                    List<Challenge> digestChallenges = response.challenges().stream()
                        .filter(challenge -> "Digest".equalsIgnoreCase(challenge.scheme()))
                        .collect(Collectors.toList());

                    String authorizationHeader = null;
                    if (digestChallenges.size() > 0) {
                        List<HttpHeaders> challenges = digestChallenges.stream().map(Challenge::authParams)
                            .map(HttpHeaders::new).collect(Collectors.toList());
                        Supplier<byte[]> bodySupplier = () -> {
                            RequestBody requestBody = response.request().body();
                            if (requestBody == null) {
                                return new byte[0];
                            }

                            Buffer bodyBuffer = new Buffer();
                            try {
                                requestBody.writeTo(bodyBuffer);
                            } catch (IOException e) {
                                throw logger.logExceptionAsWarning(new UncheckedIOException(e));
                            }

                            return bodyBuffer.readByteArray();
                        };

                        authorizationHeader = challengeHandler.handleDigest(response.request().method(),
                            response.request().url().toString(), challenges, bodySupplier);
                    } else if (basicChallenges.size() > 0) {
                        authorizationHeader = challengeHandler.handleBasic();
                    }

                    /*
                     * If Digest proxy was attempted but it wasn't able to be computed and the server sent a Basic
                     * challenge as well apply the basic authorization header.
                     */
                    if (authorizationHeader == null && basicChallenges.size() > 0) {
                        authorizationHeader = challengeHandler.handleBasic();
                    }

                    Request.Builder requestBuilder = response.request().newBuilder();

                    if (authorizationHeader != null) {
                        requestBuilder.header("Proxy-Authorization", authorizationHeader);
                    }

                    return requestBuilder.build();
                });
            }
        }
        return new OkHttpAsyncHttpClient(httpClientBuilder.build());
    }
}
