// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.nio.NioEventLoopGroup;
import java.nio.ByteBuffer;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;

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
    private final ClientLogger logger = new ClientLogger(NettyAsyncHttpClientBuilder.class);

    private final HttpClient baseHttpClient;
    private ProxyOptions proxyOptions;
    private ConnectionProvider connectionProvider;
    private boolean enableWiretap;
    private int port = 80;
    private NioEventLoopGroup nioEventLoopGroup;
    private boolean disableBufferCopy;

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
        HttpClient nettyHttpClient;
        if (this.connectionProvider != null) {
            if (this.baseHttpClient != null) {
                throw logger.logExceptionAsError(new IllegalStateException("connectionProvider cannot be set on an "
                    + "existing reactor netty HttpClient."));
            }
            nettyHttpClient = HttpClient.create(this.connectionProvider);
        } else {
            nettyHttpClient = this.baseHttpClient == null ? HttpClient.create() : this.baseHttpClient;
        }
        nettyHttpClient = nettyHttpClient
            .port(port)
            .wiretap(enableWiretap)
            .tcpConfiguration(tcpConfig -> {
                if (nioEventLoopGroup != null) {
                    tcpConfig = tcpConfig.runOn(nioEventLoopGroup);
                }

                if (proxyOptions != null) {
                    ProxyProvider.Proxy nettyProxy;
                    switch (proxyOptions.getType()) {
                        case HTTP:
                            nettyProxy = ProxyProvider.Proxy.HTTP;
                            break;
                        case SOCKS4:
                            nettyProxy = ProxyProvider.Proxy.SOCKS4;
                            break;
                        case SOCKS5:
                            nettyProxy = ProxyProvider.Proxy.SOCKS5;
                            break;
                        default:
                            throw logger.logExceptionAsError(new IllegalStateException(
                                String.format("Unknown Proxy type '%s' in use. Not configuring Netty proxy.",
                                    proxyOptions.getType())));
                    }
                    if (proxyOptions.getUsername() != null) {
                        // Netty supports only Basic proxy authentication and we default to it.
                        return tcpConfig.proxy(ts -> ts.type(nettyProxy)
                                .address(proxyOptions.getAddress())
                                .username(proxyOptions.getUsername())
                                .password(userName -> proxyOptions.getPassword())
                                .build());
                    } else {
                        return tcpConfig.proxy(ts -> ts.type(nettyProxy).address(proxyOptions.getAddress()));
                    }
                }
                return tcpConfig;
            });
        return new NettyAsyncHttpClient(nettyHttpClient, disableBufferCopy);
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

    /**
     * Disables deep copy of response {@link ByteBuffer} into a heap location that is managed by this client as
     * opposed to the underlying netty library which may use direct buffer pool.
     * <br>
     * <b>
     * Caution: Disabling this is not recommended as it can lead to data corruption if the downstream consumers
     * of the response do not handle the byte buffers before netty releases them.
     * </b>
     * If copy is disabled, underlying Netty layer can potentially reclaim byte array backed by the {@code ByteBuffer}
     * upon the return of {@code onNext()}. So, users should ensure they process the {@link ByteBuffer} immediately
     * and then return.
     *
     *  {@codesnippet com.azure.core.http.netty.disabled-buffer-copy}
     *
     * @param disableBufferCopy If set to {@code true}, the client built from this builder will not deep-copy
     * response {@link ByteBuffer ByteBuffers}.
     * @return The updated {@link NettyAsyncHttpClientBuilder} object.
     */
    public NettyAsyncHttpClientBuilder disableBufferCopy(boolean disableBufferCopy) {
        this.disableBufferCopy = disableBufferCopy;
        return this;
    }
}
