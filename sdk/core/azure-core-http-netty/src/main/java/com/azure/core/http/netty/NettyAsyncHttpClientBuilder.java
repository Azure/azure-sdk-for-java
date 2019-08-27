// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.nio.NioEventLoopGroup;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;

/**
 *
 */
public class NettyAsyncHttpClientBuilder {
    private final ClientLogger logger = new ClientLogger(NettyAsyncHttpClientBuilder.class);

    private ProxyOptions proxyOptions;
    private boolean enableWiretap;
    private int port = 80;
    private NioEventLoopGroup nioEventLoopGroup;

    /**
     *
     */
    public NettyAsyncHttpClientBuilder() {
    }

    /**
     *
     * @return A new NettyAsyncHttpClient instance
     * @throws IllegalStateException If proxy type is unknown.
     */
    public NettyAsyncHttpClient build() {
        HttpClient nettyHttpClient = HttpClient.create()
            .port(port)
            .wiretap(enableWiretap)
            .tcpConfiguration(tcpConfig -> {
                if (nioEventLoopGroup != null) {
                    tcpConfig = tcpConfig.runOn(nioEventLoopGroup);
                }

                if (proxyOptions == null) {
                    return tcpConfig;
                }

                ProxyProvider.Proxy nettyProxy;
                switch (proxyOptions.type()) {
                    case HTTP: nettyProxy = ProxyProvider.Proxy.HTTP; break;
                    case SOCKS4: nettyProxy = ProxyProvider.Proxy.SOCKS4; break;
                    case SOCKS5: nettyProxy = ProxyProvider.Proxy.SOCKS5; break;
                    default:
                        throw logger.logExceptionAsWarning(new IllegalStateException("Unknown Proxy type '" + proxyOptions.type() + "' in use. Not configuring Netty proxy."));
                }

                return tcpConfig.proxy(ts -> ts.type(nettyProxy).address(proxyOptions.address()));
            });
        return new NettyAsyncHttpClient(nettyHttpClient);
    }

    /**
     * Sets the {@link ProxyOptions proxy options} that the client will use.
     *
     * @param proxyOptions The proxy configuration to use.
     * @return the updated NettyAsyncHttpClientBuilder object
     */
    public NettyAsyncHttpClientBuilder proxy(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Apply or remove a wire logger configuration.
     *
     * @param enableWiretap Flag indicating wiretap status
     * @return the updated NettyAsyncHttpClientBuilder object
     */
    public NettyAsyncHttpClientBuilder wiretap(boolean enableWiretap) {
        this.enableWiretap = enableWiretap;
        return this;
    }

    /**
     * Sets the port which this client should connect.
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
     * @param nioEventLoopGroup The {@link NioEventLoopGroup} that will run IO loops.
     * @return the updated NettyAsyncHttpClientBuilder object
     */
    public NettyAsyncHttpClientBuilder nioEventLoopGroup(NioEventLoopGroup nioEventLoopGroup) {
        this.nioEventLoopGroup = nioEventLoopGroup;
        return this;
    }
}
