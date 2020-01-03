// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.logging.ClientLogger;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;
import reactor.netty.tcp.TcpClient;

import java.util.Objects;

/**
 * Builder class responsible for creating instances of {@link NettyAsyncHttpClient}.
 *
 * <p><strong>Building a new HttpClient instance</strong></p>
 *
 * {@codesnippet com.azure.core.http.netty.instantiation-simple}
 *
 * @see NettyAsyncHttpClient
 * @see HttpClient
 */
public class NettyAsyncHttpClientBuilder {
    // Reactor Netty uses this string to uniquely identify the ProxyHandler being used in the pipeline.
    private static final String PROXY_HANDLER_IDENTIFIER = "reactor.left.proxyHandler";

    private static final String INVALID_PROXY_MESSAGE = "Unknown Proxy type '%s' in use. Not configuring Netty proxy.";
    private static final String INVALID_INITIALIZER_STATE_MESSAGE = "connectionProvider cannot be set on an " +
        "existing reactor netty HttpClient.";

    private final ClientLogger logger = new ClientLogger(NettyAsyncHttpClientBuilder.class);

    private final HttpClient baseHttpClient;
    private ProxyOptions proxyOptions;
    private ConnectionProvider connectionProvider;
    private boolean enableWiretap;
    private int port = 80;
    private NioEventLoopGroup nioEventLoopGroup;

    /**
     * Creates a new builder instance, where a builder is capable of generating multiple instances of
     * {@link NettyAsyncHttpClient}.
     */
    public NettyAsyncHttpClientBuilder() {
        this.baseHttpClient = null;
    }

    /**
     * Creates a new builder instance, where a builder is capable of generating multiple instances of
     * {@link NettyAsyncHttpClient} based on the provided reactor netty HttpClient.
     *
     * {@codesnippet com.azure.core.http.netty.from-existing-http-client}
     *
     * @param nettyHttpClient base reactor netty HttpClient
     */
    public NettyAsyncHttpClientBuilder(HttpClient nettyHttpClient) {
        this.baseHttpClient = Objects.requireNonNull(nettyHttpClient, "'nettyHttpClient' cannot be null.");
    }

    /**
     * Creates a new Netty-backed {@link com.azure.core.http.HttpClient} instance on every call, using the
     * configuration set in the builder at the time of the build method call.
     *
     * @return A new Netty-backed {@link com.azure.core.http.HttpClient} instance.
     * @throws IllegalStateException If the builder is configured to use an unknown proxy type.
     */
    public com.azure.core.http.HttpClient build() {
        if (this.baseHttpClient != null) {
            return new NettyAsyncHttpClient(baseHttpClient);
        }

        HttpClient nettyHttpClient = (this.connectionProvider != null)
            ? HttpClient.create(this.connectionProvider)
            : HttpClient.create();

        nettyHttpClient = nettyHttpClient
            .port(port)
            .wiretap(enableWiretap);

        if (proxyOptions != null) {
            ProxyProvider.Proxy proxy = mapProxyType(proxyOptions.getType(), logger);

            // HTTP proxy has special handling if it is using authentication to support digest authentication.
            if (proxy == ProxyProvider.Proxy.HTTP && proxyOptions.getUsername() != null) {
                ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator(proxyOptions.getUsername(),
                    proxyOptions.getPassword());

                nettyHttpClient = nettyHttpClient.doOnRequest(proxyAuthenticator::doOnRequestHandler)
                    .doOnRequestError(proxyAuthenticator::doOnRequestErrorHandler)
                    .doOnResponse(proxyAuthenticator::doOnResponseHandler)
                    .tcpConfiguration(tcpClient -> tcpClient.proxy(typeSpec -> typeSpec.type(ProxyProvider.Proxy.HTTP)
                        .address(proxyOptions.getAddress())));
            } else {
                nettyHttpClient = nettyHttpClient.tcpConfiguration(tcpClient ->
                    tcpClient.proxy(typeSpec -> typeSpec.type(proxy)
                        .address(proxyOptions.getAddress())
                        .username(proxyOptions.getUsername())
                        .password(username -> proxyOptions.getPassword())));
            }
        }

        return new NettyAsyncHttpClient(nettyHttpClient);
    }

    /**
     * Sets the connection provider.
     *
     * @param connectionProvider the connection provider
     * @return the updated {@link NettyAsyncHttpClientBuilder} object
     */
    public NettyAsyncHttpClientBuilder connectionProvider(ConnectionProvider connectionProvider) {
        // Enables overriding the default reactor-netty connection/channel pool.
        this.connectionProvider = connectionProvider;
        return this;
    }
    /**
     * Sets the {@link ProxyOptions proxy options} that the client will use.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.http.netty.NettyAsyncHttpClientBuilder#proxy}
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated NettyAsyncHttpClientBuilder object
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
     * @return the updated NettyAsyncHttpClientBuilder object
     */
    public NettyAsyncHttpClientBuilder wiretap(boolean enableWiretap) {
        this.enableWiretap = enableWiretap;
        return this;
    }

    /**
     * Sets the port which this client should connect, which by default will be set to port 80.
     *
     * @param port The port to connect to.
     * @return the updated NettyAsyncHttpClientBuilder object
     */
    public NettyAsyncHttpClientBuilder port(int port) {
        this.port = port;
        return this;
    }

    /**
     * Sets the NIO event loop group that will be used to run IO loops.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.http.netty.NettyAsyncHttpClientBuilder#nioEventLoopGroup}
     *
     * @param nioEventLoopGroup The {@link NioEventLoopGroup} that will run IO loops.
     * @return the updated NettyAsyncHttpClientBuilder object
     */
    public NettyAsyncHttpClientBuilder nioEventLoopGroup(NioEventLoopGroup nioEventLoopGroup) {
        this.nioEventLoopGroup = nioEventLoopGroup;
        return this;
    }

    /*
     * Maps the ProxyOptions.Type to the corresponding ProxyProvider.Proxy type. If the type is unknown this will throw
     * an IllegalStateException.
     */
    private static ProxyProvider.Proxy mapProxyType(ProxyOptions.Type type, ClientLogger logger) {
        switch (type) {
            case HTTP:
                return ProxyProvider.Proxy.HTTP;
            case SOCKS4:
                return ProxyProvider.Proxy.SOCKS4;
            case SOCKS5:
                return ProxyProvider.Proxy.SOCKS5;
            default:
                throw logger.logExceptionAsError(new IllegalStateException(String.format(INVALID_PROXY_MESSAGE, type)));
        }
    }

    private TcpClient setupTcpConfiguration(TcpClient tcpClient) {
        if (proxyOptions == null) {
            return tcpClient;
        }

        return tcpClient.proxy(typeSpec -> typeSpec.type(ProxyProvider.Proxy.HTTP)
            .address(proxyOptions.getAddress())
            .httpHeaders(headers -> headers.add(HttpHeaderNames.PROXY_AUTHORIZATION, "Basic wrong")));
            //.bootstrap(this::setupBootstrap);
    }

    private Bootstrap setupBootstrap(Bootstrap bootstrap) {
        if (nioEventLoopGroup != null) {
            bootstrap = bootstrap.group(nioEventLoopGroup);
        }

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline pipeline = ch.pipeline();
                pipeline.addLast("encoder", new HttpResponseEncoder())
                    .addLast("decoder", new HttpRequestDecoder())
                    .addLast("aggregator", new HttpObjectAggregator(1048576))
                    .addLast("ssl", SslContextBuilder.forClient().build().newHandler(ch.alloc()))
                    .addLast("connect", new HttpClientCodec());

                ProxyHandler proxyHandler = getProxyHandler(proxyOptions, logger);
                if (proxyHandler != null) {
                    pipeline.addLast("proxy", proxyHandler);
                }
            }
        });

//        if (proxyOptions != null) {
//            ProxyHandler proxyHandler = getProxyHandler(proxyOptions, logger);
//
//            if (proxyHandler == null) {
//
//            }
//
//            bootstrap = BootstrapHandlers.updateConfiguration(bootstrap, PROXY_HANDLER_IDENTIFIER, (b) ->
//                (connectionObserver, channel) ->
//                    channel.pipeline().addFirst(PROXY_HANDLER_IDENTIFIER, proxyHandler));
//        }

        return bootstrap;
    }

    /*
     * Creates a proxy handler based on the passed ProxyOptions.
     */
    private static ProxyHandler getProxyHandler(ProxyOptions proxyOptions, ClientLogger logger) {
        if (proxyOptions == null) {
            return null;
        }

        switch (proxyOptions.getType()) {
            case HTTP:
                return new ProxyAuthenticationHandler(proxyOptions);
            case SOCKS4:
                return new Socks4ProxyHandler(proxyOptions.getAddress(), proxyOptions.getUsername());
            case SOCKS5:
                return new Socks5ProxyHandler(proxyOptions.getAddress(), proxyOptions.getUsername(),
                    proxyOptions.getPassword());
            default:
                throw logger.logExceptionAsError(new IllegalStateException(
                    String.format(INVALID_PROXY_MESSAGE, proxyOptions.getType())));
        }
    }
}
