// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.implementation.AzureNettyHttpClientContext;
import com.azure.core.http.netty.implementation.AzureSdkHandler;
import com.azure.core.http.netty.implementation.ChallengeHolder;
import com.azure.core.http.netty.implementation.HttpProxyHandler;
import com.azure.core.http.netty.implementation.NettyUtility;
import com.azure.core.util.AuthorizationChallengeHandler;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LoggingHandler;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.resolver.NoopAddressResolverGroup;
import reactor.netty.Connection;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClientRequest;
import reactor.netty.http.client.HttpResponseDecoderSpec;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.transport.AddressUtils;
import reactor.netty.transport.ProxyProvider;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.azure.core.implementation.util.HttpUtils.getDefaultConnectTimeout;
import static com.azure.core.implementation.util.HttpUtils.getDefaultReadTimeout;
import static com.azure.core.implementation.util.HttpUtils.getDefaultResponseTimeout;
import static com.azure.core.implementation.util.HttpUtils.getDefaultWriteTimeout;
import static com.azure.core.implementation.util.HttpUtils.getTimeout;

/**
 * <p>
 * Builder class responsible for creating instances of {@link com.azure.core.http.HttpClient} backed by Reactor Netty.
 * The client built from this builder can support sending requests synchronously and asynchronously.
 * Use {@link com.azure.core.http.HttpClient#sendSync(HttpRequest, Context)} to send the provided request
 * synchronously with contextual information.
 * </p>
 *
 * <p>
 * <strong>Building a new HttpClient instance</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.netty.instantiation-simple -->
 * <pre>
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;
 *     .port&#40;8080&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.netty.instantiation-simple -->
 *
 * <p>
 * <strong>Building a new HttpClient instance using http proxy.</strong>
 * </p>
 *
 * <p>
 * Configuring the Netty client with a proxy is relevant when your application needs to communicate with Azure
 * services through a proxy server.
 * </p>
 *
 * <!-- src_embed com.azure.core.http.netty.instantiation-simple -->
 * <pre>
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;
 *     .port&#40;8080&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.netty.instantiation-simple -->
 *
 * <p>
 * <strong>Building a new HttpClient instance with HTTP/2 Support.</strong>
 * </p>
 *
 * <!-- src_embed com.azure.core.http.netty.instantiation-simple -->
 * <pre>
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;
 *     .port&#40;8080&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.netty.instantiation-simple -->
 *
 * <p>
 * It is also possible to create a Netty HttpClient that only supports HTTP/2.
 * </p>
 *
 * <!-- src_embed readme-sample-useHttp2OnlyWithConfiguredNettyClient -->
 * <pre>
 * &#47;&#47; Constructs an HttpClient that only supports HTTP&#47;2.
 * HttpClient client = new NettyAsyncHttpClientBuilder&#40;reactor.netty.http.client.HttpClient.create&#40;&#41;
 *     .protocol&#40;HttpProtocol.H2&#41;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end readme-sample-useHttp2OnlyWithConfiguredNettyClient -->
 *
 * @see HttpClient
 * @see NettyAsyncHttpClient
 */
public class NettyAsyncHttpClientBuilder {
    // NettyAsyncHttpClientBuilder may be instantiated many times, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(NettyAsyncHttpClientBuilder.class);

    static {
        NettyUtility.validateNettyVersions();
    }

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
     * <!-- src_embed com.azure.core.http.netty.from-existing-http-client -->
     * <pre>
     * &#47;&#47; Creates a reactor-netty client with netty logging enabled.
     * reactor.netty.http.client.HttpClient baseHttpClient = reactor.netty.http.client.HttpClient.create&#40;&#41;
     *     .wiretap&#40;TcpClient.class.getName&#40;&#41;, LogLevel.INFO&#41;;
     * &#47;&#47; Create an HttpClient based on above reactor-netty client and configure EventLoop count.
     * HttpClient client = new NettyAsyncHttpClientBuilder&#40;baseHttpClient&#41;
     *     .eventLoopGroup&#40;new NioEventLoopGroup&#40;5&#41;&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.netty.from-existing-http-client -->
     *
     * @param nettyHttpClient base reactor netty HttpClient
     */
    public NettyAsyncHttpClientBuilder(HttpClient nettyHttpClient) {
        this.baseHttpClient = Objects.requireNonNull(nettyHttpClient, "'nettyHttpClient' cannot be null.");
    }

    /**
     * Creates a new Netty-backed {@link com.azure.core.http.HttpClient} instance on every call, using the configuration
     * set in the builder at the time of the build method call. Please be aware that client built from this builder can
     * support synchronously and asynchronously call of sending request. Use
     * {@link com.azure.core.http.HttpClient#sendSync(HttpRequest, Context)} to send the provided request synchronously
     * with contextual information.
     *
     * @return A new Netty-backed {@link com.azure.core.http.HttpClient} instance.
     * @throws IllegalStateException If the builder is configured to use an unknown proxy type.
     */
    public com.azure.core.http.HttpClient build() {
        HttpClient nettyHttpClient;

        // Used to track if the builder set the DefaultAddressResolverGroup. If it did, when proxying it allows the
        // no-op address resolver to be set.
        boolean addressResolverWasSetByBuilder = false;
        if (this.baseHttpClient != null) {
            nettyHttpClient = baseHttpClient;
        } else if (this.connectionProvider != null) {
            nettyHttpClient = HttpClient.create(this.connectionProvider).resolver(DefaultAddressResolverGroup.INSTANCE);
            addressResolverWasSetByBuilder = true;
        } else {
            nettyHttpClient = HttpClient.create().resolver(DefaultAddressResolverGroup.INSTANCE);
            addressResolverWasSetByBuilder = true;
        }

        long writeTimeout = getTimeout(this.writeTimeout, getDefaultWriteTimeout()).toMillis();
        long responseTimeout = getTimeout(this.responseTimeout, getDefaultResponseTimeout()).toMillis();
        long readTimeout = getTimeout(this.readTimeout, getDefaultReadTimeout()).toMillis();

        // Get the initial HttpResponseDecoderSpec and update it.
        // .httpResponseDecoder passes a new HttpResponseDecoderSpec and any existing configuration should be updated
        // instead of overwritten.
        HttpResponseDecoderSpec initialSpec = nettyHttpClient.configuration().decoder();
        nettyHttpClient = nettyHttpClient.port(port)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                (int) getTimeout(connectTimeout, getDefaultConnectTimeout()).toMillis())
            // TODO (alzimmer): What does validating HTTP response headers get us?
            .httpResponseDecoder(httpResponseDecoderSpec -> initialSpec.validateHeaders(false))
            .doOnRequest(
                (request, connection) -> addHandler(request, connection, writeTimeout, responseTimeout, readTimeout))
            .doAfterResponseSuccess((ignored, connection) -> removeHandler(connection));

        LoggingHandler loggingHandler = nettyHttpClient.configuration().loggingHandler();
        if (loggingHandler == null) {
            // Only enable wiretap if the LoggingHandler is null. If the LoggingHandler isn't null this means that a
            // base client was passed with logging enabled. 'wiretap(boolean)' is a basic API that doesn't allow for
            // in-depth settings to be done on how logging works, so setting it always can replace a customized logger
            // with a basic one that isn't as useful in troubleshooting scenarios.
            nettyHttpClient.wiretap(enableWiretap);
        }

        Configuration buildConfiguration
            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;

        ProxyOptions buildProxyOptions
            = proxyOptions == null ? ProxyOptions.fromConfiguration(buildConfiguration, true) : proxyOptions;

        /*
         * Only configure the custom authorization challenge handler and challenge holder when using an authenticated
         * HTTP proxy. All other proxying such as SOCKS4, SOCKS5, and anonymous HTTP will use Netty's built-in handlers.
         */
        boolean useCustomProxyHandler = shouldUseCustomProxyHandler(buildProxyOptions);
        AuthorizationChallengeHandler handler = useCustomProxyHandler
            ? new AuthorizationChallengeHandler(buildProxyOptions.getUsername(), buildProxyOptions.getPassword())
            : null;
        AtomicReference<ChallengeHolder> proxyChallengeHolder = useCustomProxyHandler ? new AtomicReference<>() : null;

        boolean addProxyHandler = false;

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
                 */
                addProxyHandler = true;
                Pattern nonProxyHostsPattern = CoreUtils.isNullOrEmpty(buildProxyOptions.getNonProxyHosts())
                    ? null
                    : Pattern.compile(buildProxyOptions.getNonProxyHosts(), Pattern.CASE_INSENSITIVE);

                nettyHttpClient = nettyHttpClient.doOnChannelInit((connectionObserver, channel, socketAddress) -> {
                    if (shouldApplyProxy(socketAddress, nonProxyHostsPattern)) {
                        channel.pipeline()
                            .addFirst(NettyPipeline.ProxyHandler,
                                new HttpProxyHandler(AddressUtils.replaceWithResolved(buildProxyOptions.getAddress()),
                                    handler, proxyChallengeHolder));
                    }
                });
            } else {
                nettyHttpClient
                    = nettyHttpClient.proxy(proxy -> proxy.type(toReactorNettyProxyType(buildProxyOptions.getType()))
                        .address(buildProxyOptions.getAddress())
                        .username(buildProxyOptions.getUsername())
                        .password(ignored -> buildProxyOptions.getPassword())
                        .nonProxyHosts(buildProxyOptions.getNonProxyHosts()));
            }

            AddressResolverGroup<?> resolver = nettyHttpClient.configuration().resolver();
            if (resolver == null || addressResolverWasSetByBuilder) {
                // This mimics behaviors seen when Reactor Netty proxying is used.
                nettyHttpClient = nettyHttpClient.resolver(NoopAddressResolverGroup.INSTANCE);
            }
        }

        return new NettyAsyncHttpClient(nettyHttpClient, disableBufferCopy, addProxyHandler);
    }

    /**
     * Sets the connection provider.
     *
     * <p><strong>Code Sample</strong></p>
     *
     * <!-- src_embed com.azure.core.http.netty.NettyAsyncHttpClientBuilder.connectionProvider#ConnectionProvider -->
     * <pre>
     * &#47;&#47; The following creates a connection provider which will have each connection use the base name
     * &#47;&#47; 'myHttpConnection', has a limit of 500 concurrent connections in the connection pool, has no limit on the
     * &#47;&#47; number of connection requests that can be pending when all connections are in use, and removes a connection
     * &#47;&#47; from the pool if the connection isn't used for 60 seconds.
     * ConnectionProvider connectionProvider = ConnectionProvider.builder&#40;&quot;myHttpConnection&quot;&#41;
     *     .maxConnections&#40;500&#41;
     *     .pendingAcquireMaxCount&#40;-1&#41;
     *     .maxIdleTime&#40;Duration.ofSeconds&#40;60&#41;&#41;
     *     .build&#40;&#41;;
     *
     * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;
     *     .connectionProvider&#40;connectionProvider&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.netty.NettyAsyncHttpClientBuilder.connectionProvider#ConnectionProvider -->
     *
     * @param connectionProvider the connection provider
     * @return the updated {@link NettyAsyncHttpClientBuilder} object.
     */
    public NettyAsyncHttpClientBuilder connectionProvider(ConnectionProvider connectionProvider) {
        // Enables overriding the default reactor-netty connection/channel pool.
        if (connectionProvider != null) {
            LOGGER.verbose("Setting ConnectionProvider for the Reactor Netty HttpClient. Please be aware of the "
                + "differences in runtime behavior when creating a default Reactor Netty HttpClient vs an HttpClient"
                + "with a specified ConnectionProvider. For more details see "
                + "https://aka.ms/azsdk/java/docs/configure-httpclient.");
        }

        this.connectionProvider = connectionProvider;
        return this;
    }

    NettyAsyncHttpClientBuilder connectionProviderInternal(ConnectionProvider connectionProvider) {
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
     * @deprecated If logging should be enabled as the Reactor Netty level, construct the builder using
     * {@link #NettyAsyncHttpClientBuilder(HttpClient)} where the passed Reactor Netty HttpClient has logging
     * configured.
     */
    @Deprecated
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
     * <!-- src_embed com.azure.core.http.netty.NettyAsyncHttpClientBuilder#eventLoopGroup -->
     * <pre>
     * int threadCount = 5;
     * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;
     *     .eventLoopGroup&#40;new NioEventLoopGroup&#40;threadCount&#41;&#41;
     *     .build&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.netty.NettyAsyncHttpClientBuilder#eventLoopGroup -->
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
     * <!-- src_embed com.azure.core.http.netty.disabled-buffer-copy -->
     * <pre>
     * HttpClient client = new NettyAsyncHttpClientBuilder&#40;&#41;
     *     .port&#40;8080&#41;
     *     .disableBufferCopy&#40;true&#41;
     *     .build&#40;&#41;;
     *
     * client.send&#40;httpRequest&#41;
     *     .flatMapMany&#40;response -&gt; response.getBody&#40;&#41;&#41;
     *     .map&#40;byteBuffer -&gt; completeProcessingByteBuffer&#40;byteBuffer&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.core.http.netty.disabled-buffer-copy -->
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
     * By default, the connection timeout is 10 seconds.
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

    private static ProxyProvider.Proxy toReactorNettyProxyType(ProxyOptions.Type azureProxyType) {
        switch (azureProxyType) {
            case HTTP:
                return ProxyProvider.Proxy.HTTP;

            case SOCKS4:
                return ProxyProvider.Proxy.SOCKS4;

            case SOCKS5:
                return ProxyProvider.Proxy.SOCKS5;

            default:
                throw LOGGER
                    .logExceptionAsError(new IllegalArgumentException("Unknown 'ProxyOptions.Type' enum value"));
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

    /*
     * Request has started, add the Azure SDK ChannelAdapter.
     */
    private static void addHandler(HttpClientRequest request, Connection connection, long writeTimeout,
        long responseTimeout, long readTimeout) {
        AzureNettyHttpClientContext attr
            = request.currentContextView().getOrDefault(AzureNettyHttpClientContext.KEY, null);

        connection.addHandlerLast(AzureSdkHandler.HANDLER_NAME,
            new AzureSdkHandler(attr, writeTimeout, responseTimeout, readTimeout));
    }

    /*
     * Response has completed, remove the Azure SDK ChannelHandler.
     */
    private static void removeHandler(Connection connection) {
        connection.removeHandler(AzureSdkHandler.HANDLER_NAME);
    }
}
