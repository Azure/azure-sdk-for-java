// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.implementation.ChallengeHolder;
import com.azure.core.http.netty.implementation.DeferredHttpProxyProvider;
import com.azure.core.http.netty.implementation.ReadTimeoutHandler;
import com.azure.core.http.netty.implementation.ResponseTimeoutHandler;
import com.azure.core.http.netty.implementation.WriteTimeoutHandler;
import com.azure.core.util.AuthorizationChallengeHandler;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import reactor.netty.Connection;
import reactor.netty.NettyPipeline;
import reactor.netty.channel.BootstrapHandlers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Builder class responsible for creating instances of {@link com.azure.core.http.HttpClient} backed by Reactor Netty.
 *
 * <p><strong>Building a new HttpClient instance</strong></p>
 *
 * {@codesnippet com.azure.core.http.netty.instantiation-simple}
 *
 * @see HttpClient
 */
public class NettyAsyncHttpClientBuilder {
    private final ClientLogger logger = new ClientLogger(NettyAsyncHttpClientBuilder.class);

    private static final long MINIMUM_TIMEOUT = TimeUnit.MILLISECONDS.toMillis(1);
    private static final long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(60);

    private final HttpClient baseHttpClient;
    private ProxyOptions proxyOptions;
    private ConnectionProvider connectionProvider;
    private boolean enableWiretap;
    private int port = 80;
    private EventLoopGroup eventLoopGroup;
    private Configuration configuration;
    private boolean disableBufferCopy;
    private Duration writeTimeout;
    private Duration responseTimeout;
    private Duration readTimeout;

    /**
     * Creates a new builder instance, where a builder is capable of generating multiple instances of {@link
     * com.azure.core.http.HttpClient} backed by Reactor Netty.
     */
    public NettyAsyncHttpClientBuilder() {
        this.baseHttpClient = null;
    }

    /**
     * Creates a new builder instance, where a builder is capable of generating multiple instances of {@link
     * HttpClient} based on the provided Reactor Netty HttpClient.
     *
     * {@codesnippet com.azure.core.http.netty.from-existing-http-client}
     *
     * @param nettyHttpClient base reactor netty HttpClient
     */
    public NettyAsyncHttpClientBuilder(HttpClient nettyHttpClient) {
        this.baseHttpClient = Objects.requireNonNull(nettyHttpClient, "'nettyHttpClient' cannot be null.");
    }

    /**
     * Creates a new Netty-backed {@link com.azure.core.http.HttpClient} instance on every call, using the configuration
     * set in the builder at the time of the build method call.
     *
     * @return A new Netty-backed {@link com.azure.core.http.HttpClient} instance.
     * @throws IllegalStateException If the builder is configured to use an unknown proxy type.
     */
    public com.azure.core.http.HttpClient build() {
        HttpClient nettyHttpClient;
        if (this.baseHttpClient != null) {
            nettyHttpClient = baseHttpClient;
        } else if (this.connectionProvider != null) {
            nettyHttpClient = HttpClient.create(this.connectionProvider);
        } else {
            nettyHttpClient = HttpClient.create();
        }

        nettyHttpClient = nettyHttpClient
            .port(port)
            .wiretap(enableWiretap)
            .doOnRequest((request, connection) -> addWriteTimeoutHandler(connection, getTimeoutMillis(writeTimeout)))
            .doAfterRequest((request, connection) ->
                addResponseTimeoutHandler(connection, getTimeoutMillis(responseTimeout)))
            .doOnResponse((response, connection) -> addReadTimeoutHandler(connection, getTimeoutMillis(readTimeout)))
            .doAfterResponseSuccess((response, connection) -> removeReadTimeoutHandler(connection));

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ProxyOptions buildProxyOptions = (proxyOptions == null && buildConfiguration != Configuration.NONE)
            ? ProxyOptions.fromConfiguration(buildConfiguration)
            : proxyOptions;

        /*
         * Only configure the custom authorization challenge handler and challenge holder when using an authenticated
         * HTTP proxy. All other proxying such as SOCKS4, SOCKS5, and anonymous HTTP will use Netty's built-in handlers.
         */
        boolean useCustomProxyHandler = shouldUseCustomProxyHandler(buildProxyOptions);
        AuthorizationChallengeHandler handler = useCustomProxyHandler
            ? new AuthorizationChallengeHandler(buildProxyOptions.getUsername(), buildProxyOptions.getPassword())
            : null;
        AtomicReference<ChallengeHolder> proxyChallengeHolder = useCustomProxyHandler ? new AtomicReference<>() : null;

        nettyHttpClient = nettyHttpClient.tcpConfiguration(tcpClient -> {
            if (eventLoopGroup != null) {
                tcpClient = tcpClient.runOn(eventLoopGroup);
            }

            // Proxy configurations are present, setup a proxy in Netty.
            if (buildProxyOptions != null) {
                // Determine if custom handling will be used, otherwise use Netty's built-in handlers.
                if (handler != null) {
                    /*
                     * Configure the request Channel to be initialized with a ProxyHandler. The ProxyHandler is the
                     * first operation in the pipeline as it needs to handle sending a CONNECT request to the proxy
                     * before any request data is sent.
                     */
                    tcpClient = tcpClient.bootstrap(bootstrap -> BootstrapHandlers.updateConfiguration(bootstrap,
                        NettyPipeline.ProxyHandler, new DeferredHttpProxyProvider(handler, proxyChallengeHolder,
                            buildProxyOptions)));
                } else {
                    tcpClient = tcpClient.proxy(proxy ->
                        proxy.type(toReactorNettyProxyType(buildProxyOptions.getType(), logger))
                            .address(buildProxyOptions.getAddress())
                            .username(buildProxyOptions.getUsername())
                            .password(ignored -> buildProxyOptions.getPassword())
                            .nonProxyHosts(buildProxyOptions.getNonProxyHosts()));
                }
            }

            return tcpClient;
        });

        return new NettyAsyncHttpClient(nettyHttpClient, disableBufferCopy);
    }

    /**
     * Sets the connection provider.
     *
     * @param connectionProvider the connection provider
     * @return the updated {@link NettyAsyncHttpClientBuilder} object.
     */
    public NettyAsyncHttpClientBuilder connectionProvider(ConnectionProvider connectionProvider) {
        // Enables overriding the default reactor-netty connection/channel pool.
        this.connectionProvider = connectionProvider;
        return this;
    }

    /**
     * Sets the {@link ProxyOptions proxy options} that the client will use.
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated NettyAsyncHttpClientBuilder object.
     */
    public NettyAsyncHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        // proxyOptions can be null
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Enables the Netty wiretap feature.
     *
     * @param enableWiretap Flag indicating wiretap status
     * @return the updated NettyAsyncHttpClientBuilder object.
     */
    public NettyAsyncHttpClientBuilder wiretap(boolean enableWiretap) {
        this.enableWiretap = enableWiretap;
        return this;
    }

    /**
     * Sets the port which this client should connect, which by default will be set to port 80.
     *
     * @param port The port to connect to.
     * @return the updated NettyAsyncHttpClientBuilder object.
     */
    public NettyAsyncHttpClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the NIO event loop group that will be used to run IO loops.
     *
     * @param nioEventLoopGroup The {@link NioEventLoopGroup} that will run IO loops.
     * @return the updated NettyAsyncHttpClientBuilder object.
     * @deprecated deprecated in favor of {@link #eventLoopGroup(EventLoopGroup)}.
     */
    @Deprecated
    public NettyAsyncHttpClientBuilder nioEventLoopGroup(NioEventLoopGroup nioEventLoopGroup) {
        this.eventLoopGroup = nioEventLoopGroup;
        return this;
    }

    /**
     * Sets the IO event loop group that will be used to run IO loops.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.http.netty.NettyAsyncHttpClientBuilder#eventLoopGroup}
     *
     * @param eventLoopGroup The {@link EventLoopGroup} that will run IO loops.
     * @return the updated NettyAsyncHttpClientBuilder object.
     */
    public NettyAsyncHttpClientBuilder eventLoopGroup(EventLoopGroup eventLoopGroup) {
        this.eventLoopGroup = eventLoopGroup;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     * <p>
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}, use {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to
     * @return The updated NettyAsyncHttpClientBuilder object.
     */
    public NettyAsyncHttpClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Disables deep copy of response {@link ByteBuffer} into a heap location that is managed by this client as opposed
     * to the underlying netty library which may use direct buffer pool.
     * <br>
     * <b>
     * Caution: Disabling this is not recommended as it can lead to data corruption if the downstream consumers of the
     * response do not handle the byte buffers before netty releases them.
     * </b>
     * If copy is disabled, underlying Netty layer can potentially reclaim byte array backed by the {@code ByteBuffer}
     * upon the return of {@code onNext()}. So, users should ensure they process the {@link ByteBuffer} immediately and
     * then return.
     *
     * {@codesnippet com.azure.core.http.netty.disabled-buffer-copy}
     *
     * @param disableBufferCopy If set to {@code true}, the client built from this builder will not deep-copy response
     * {@link ByteBuffer ByteBuffers}.
     * @return The updated {@link NettyAsyncHttpClientBuilder} object.
     */
    public NettyAsyncHttpClientBuilder disableBufferCopy(boolean disableBufferCopy) {
        this.disableBufferCopy = disableBufferCopy;
        return this;
    }

    /**
     * Sets the write timeout for a request to be sent.
     * <p>
     * The write timeout does not apply to the entire request but to the request being sent over the wire. For example a
     * request body which emits {@code 10} {@code 8KB} buffers will trigger {@code 10} write operations, the last write
     * tracker will update when each operation completes and the outbound buffer will be periodically checked to
     * determine if it is still draining.
     * <p>
     * If {@code writeTimeout} is {@code null} is {@code null} a 60 second timeout will be used, if it is a {@link
     * Duration} less than or equal to zero then no write timeout will be applied. When applying the timeout the greater
     * of one millisecond and the value of {@code writeTimeout} will be used.
     *
     * @param writeTimeout Write operation timeout duration.
     * @return The updated {@link NettyAsyncHttpClientBuilder} object.
     */
    public NettyAsyncHttpClientBuilder writeTimeout(Duration writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    /**
     * Sets the response timeout duration used when waiting for a server to reply.
     * <p>
     * The response timeout begins once the request write completes and finishes once the first response read is
     * triggered when the server response is received.
     * <p>
     * If {@code responseTimeout} is {@code null} a 60 second timeout will be used, if it is a {@link Duration} less
     * than or equal to zero then no timeout will be applied to the response. When applying the timeout the greater of
     * one millisecond and the value of {@code responseTimeout} will be used.
     *
     * @param responseTimeout Response timeout duration.
     * @return The updated {@link NettyAsyncHttpClientBuilder} object.
     */
    public NettyAsyncHttpClientBuilder responseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }

    /**
     * Sets the read timeout duration used when reading the server response.
     * <p>
     * The read timeout begins once the first response read is triggered after the server response is received. This
     * timeout triggers periodically but won't fire its operation if another read operation has completed between when
     * the timeout is triggered and completes.
     * <p>
     * If {@code readTimeout} is {@code null} a 60 second timeout will be used, if it is a {@link Duration} less than or
     * equal to zero then no timeout period will be applied to response read. When applying the timeout the greater of
     * one millisecond and the value of {@code readTimeout} will be used.
     *
     * @param readTimeout Read timeout duration.
     * @return The updated {@link NettyAsyncHttpClientBuilder} object.
     */
    public NettyAsyncHttpClientBuilder readTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    private static boolean shouldUseCustomProxyHandler(ProxyOptions options) {
        return options != null && options.getUsername() != null && options.getType() == ProxyOptions.Type.HTTP;
    }

    private static ProxyProvider.Proxy toReactorNettyProxyType(ProxyOptions.Type azureProxyType, ClientLogger logger) {
        switch (azureProxyType) {
            case HTTP:
                return ProxyProvider.Proxy.HTTP;
            case SOCKS4:
                return ProxyProvider.Proxy.SOCKS4;
            case SOCKS5:
                return ProxyProvider.Proxy.SOCKS5;
            default:
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Unknown 'ProxyOptions.Type' enum value"));
        }
    }

    /*
     * Adds the write timeout handler once the request is ready to begin sending.
     */
    private static void addWriteTimeoutHandler(Connection connection, long timeoutMillis) {
        connection.addHandlerLast(WriteTimeoutHandler.HANDLER_NAME, new WriteTimeoutHandler(timeoutMillis));
    }

    /*
     * First removes the write timeout handler from the connection as the request has finished sending, then adds the
     * response timeout handler.
     */
    private static void addResponseTimeoutHandler(Connection connection, long timeoutMillis) {
        connection.removeHandler(WriteTimeoutHandler.HANDLER_NAME)
            .addHandlerLast(ResponseTimeoutHandler.HANDLER_NAME, new ResponseTimeoutHandler(timeoutMillis));
    }

    /*
     * First removes the response timeout handler from the connection as the response has been received, then adds the
     * read timeout handler.
     */
    private static void addReadTimeoutHandler(Connection connection, long timeoutMillis) {
        connection.removeHandler(ResponseTimeoutHandler.HANDLER_NAME)
            .addHandlerLast(ReadTimeoutHandler.HANDLER_NAME, new ReadTimeoutHandler(timeoutMillis));
    }

    /*
     * Removes the read timeout handler as the complete response has been received.
     */
    private static void removeReadTimeoutHandler(Connection connection) {
        connection.removeHandler(ReadTimeoutHandler.HANDLER_NAME);
    }

    /*
     * Returns the timeout in milliseconds to use based on the passed {@link Duration}.
     * <p>
     * If the timeout is {@code null} a default of 60 seconds will be used. If the timeout is less than or equal to zero
     * no timeout will be used. If the timeout is less than one millisecond a timeout of one millisecond will be used.
     *
     * @param timeout The {@link Duration} to convert to timeout in milliseconds.
     * @return The timeout period in milliseconds, zero if no timeout.
     */
    static long getTimeoutMillis(Duration timeout) {
        // Timeout is null, use the 60 second default.
        if (timeout == null) {
            return TimeUnit.SECONDS.toMillis(60);
        }

        // Timeout is less than or equal to zero, return no timeout.
        if (timeout.isZero() || timeout.isNegative()) {
            return 0;
        }

        // Return the maximum of the timeout period and the minimum allowed timeout period.
        return Math.max(timeout.toMillis(), MINIMUM_TIMEOUT);
    }
}
