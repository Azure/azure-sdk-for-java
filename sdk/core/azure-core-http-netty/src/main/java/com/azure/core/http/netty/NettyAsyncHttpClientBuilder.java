// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.implementation.ChallengeHolder;
import com.azure.core.http.netty.implementation.HttpProxyHandler;
import com.azure.core.util.AuthorizationChallengeHandler;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.resolver.NoopAddressResolverGroup;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.AddressUtils;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_READ_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT;

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
    private static final long DEFAULT_CONNECT_TIMEOUT = getDefaultTimeout(PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT,
        10000);
    private static final long DEFAULT_WRITE_TIMEOUT = getDefaultTimeout(PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT, 60000);
    private static final long DEFAULT_RESPONSE_TIMEOUT = getDefaultTimeout(PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT,
        60000);
    private static final long DEFAULT_READ_TIMEOUT = getDefaultTimeout(PROPERTY_AZURE_REQUEST_READ_TIMEOUT, 60000);

    private final HttpClient baseHttpClient;
    private ProxyOptions proxyOptions;
    private ConnectionProvider connectionProvider;
    private boolean enableWiretap;
    private int port = 80;
    private EventLoopGroup eventLoopGroup;
    private Configuration configuration;
    private boolean disableBufferCopy;
    private Duration connectTimeout;
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
     * Creates a new builder instance, where a builder is capable of generating multiple instances of {@link HttpClient}
     * based on the provided Reactor Netty HttpClient.
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
            nettyHttpClient = HttpClient.create(this.connectionProvider).resolver(DefaultAddressResolverGroup.INSTANCE);
        } else {
            nettyHttpClient = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);
        }

        nettyHttpClient = nettyHttpClient
            .port(port)
            .wiretap(enableWiretap)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) getTimeoutMillis(connectTimeout,
                DEFAULT_CONNECT_TIMEOUT));

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        ProxyOptions buildProxyOptions = (proxyOptions == null && buildConfiguration != Configuration.NONE)
            ? ProxyOptions.fromConfiguration(buildConfiguration, true)
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

        if (eventLoopGroup != null) {
            nettyHttpClient = nettyHttpClient.runOn(eventLoopGroup);
        }

        // Proxy configurations are present, set up a proxy in Netty.
        if (buildProxyOptions != null) {
            // Determine if custom handling will be used, otherwise use Netty's built-in handlers.
            if (handler != null) {
                /*
                 * Configure the request Channel to be initialized with a ProxyHandler. The ProxyHandler is the
                 * first operation in the pipeline as it needs to handle sending a CONNECT request to the proxy
                 * before any request data is sent.
                 *
                 * And in addition to adding the ProxyHandler update the Bootstrap resolver for proxy support.
                 */
                Pattern nonProxyHostsPattern = CoreUtils.isNullOrEmpty(buildProxyOptions.getNonProxyHosts())
                    ? null
                    : Pattern.compile(buildProxyOptions.getNonProxyHosts(), Pattern.CASE_INSENSITIVE);

                nettyHttpClient = nettyHttpClient.doOnChannelInit((connectionObserver, channel, socketAddress) -> {
                    if (shouldApplyProxy(socketAddress, nonProxyHostsPattern)) {
                        channel.pipeline()
                            .addFirst(NettyPipeline.ProxyHandler, new HttpProxyHandler(
                                AddressUtils.replaceWithResolved(buildProxyOptions.getAddress()),
                                handler, proxyChallengeHolder));
                    }
                });

                AddressResolverGroup<?> resolver = nettyHttpClient.configuration().resolver();
                if (resolver == null) {
                    nettyHttpClient.resolver(NoopAddressResolverGroup.INSTANCE);
                }
            } else {
                nettyHttpClient = nettyHttpClient.proxy(proxy ->
                    proxy.type(toReactorNettyProxyType(buildProxyOptions.getType(), logger))
                        .address(buildProxyOptions.getAddress())
                        .username(buildProxyOptions.getUsername())
                        .password(ignored -> buildProxyOptions.getPassword())
                        .nonProxyHosts(buildProxyOptions.getNonProxyHosts()));
            }
        }

        return new NettyAsyncHttpClient(nettyHttpClient, disableBufferCopy,
            getTimeoutMillis(readTimeout, DEFAULT_READ_TIMEOUT), getTimeoutMillis(writeTimeout, DEFAULT_WRITE_TIMEOUT),
            getTimeoutMillis(responseTimeout, DEFAULT_RESPONSE_TIMEOUT));
    }

    /**
     * Sets the connection provider.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * {@codesnippet com.azure.core.http.netty.NettyAsyncHttpClientBuilder.connectionProvider#ConnectionProvider}
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
     * Sets the connection timeout for a request to be sent.
     * <p>
     * The connection timeout begins once the request attempts to connect to the remote host and finishes once the
     * connection is resolved.
     * <p>
     * If {@code connectTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_CONNECT_TIMEOUT} or a
     * 10-second timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout will be
     * applied. When applying the timeout the greatest of one millisecond and the value of {@code connectTimeout} will
     * be used.
     * <p>
     * By default the connection timeout is 10 seconds.
     *
     * @param connectTimeout Connect timeout duration.
     * @return The updated {@link NettyAsyncHttpClientBuilder} object.
     */
    public NettyAsyncHttpClientBuilder connectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     * Sets the writing timeout for a request to be sent.
     * <p>
     * The writing timeout does not apply to the entire request but to the request being sent over the wire. For example
     * a request body which emits {@code 10} {@code 8KB} buffers will trigger {@code 10} write operations, the last
     * write tracker will update when each operation completes and the outbound buffer will be periodically checked to
     * determine if it is still draining.
     * <p>
     * If {@code writeTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_WRITE_TIMEOUT} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no write timeout will be
     * applied. When applying the timeout the greatest of one millisecond and the value of {@code writeTimeout} will be
     * used.
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
     * If {@code responseTimeout} is null either {@link Configuration#PROPERTY_AZURE_REQUEST_RESPONSE_TIMEOUT} or a
     * 60-second timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout will be
     * applied to the response. When applying the timeout the greatest of one millisecond and the value of {@code
     * responseTimeout} will be used.
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
     * If {@code readTimeout} is null or {@link Configuration#PROPERTY_AZURE_REQUEST_READ_TIMEOUT} or a 60-second
     * timeout will be used, if it is a {@link Duration} less than or equal to zero then no timeout period will be
     * applied to response read. When applying the timeout the greatest of one millisecond and the value of {@code
     * readTimeout} will be used.
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

    private static boolean shouldApplyProxy(SocketAddress socketAddress, Pattern nonProxyHostsPattern) {
        if (nonProxyHostsPattern == null) {
            return true;
        }

        if (!(socketAddress instanceof InetSocketAddress)) {
            return true;
        }

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;

        return !nonProxyHostsPattern.matcher(inetSocketAddress.getHostString()).matches();
    }

    private static long getDefaultTimeout(String property, long defaultTimeoutMillis) {
        String envTimeout = Configuration.getGlobalConfiguration().get(property);
        if (CoreUtils.isNullOrEmpty(envTimeout)) {
            return defaultTimeoutMillis;
        }

        try {
            return Long.parseLong(envTimeout);
        } catch (NumberFormatException ex) {
            return defaultTimeoutMillis;
        }
    }

    /*
     * Returns the timeout in milliseconds to use based on the passed Duration and default timeout.
     *
     * If the timeout is {@code null} the default timeout will be used. If the timeout is less than or equal to zero
     * no timeout will be used. If the timeout is less than one millisecond a timeout of one millisecond will be used.
     */
    static long getTimeoutMillis(Duration configuredTimeout, long defaultTimeout) {
        // Timeout is null, use the default timeout.
        if (configuredTimeout == null) {
            return defaultTimeout;
        }

        // Timeout is less than or equal to zero, return no timeout.
        if (configuredTimeout.isZero() || configuredTimeout.isNegative()) {
            return 0;
        }

        // Return the maximum of the timeout period and the minimum allowed timeout period.
        return Math.max(configuredTimeout.toMillis(), MINIMUM_TIMEOUT);
    }
}
