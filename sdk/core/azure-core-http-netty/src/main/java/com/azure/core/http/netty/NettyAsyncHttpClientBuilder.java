// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.netty.channel.nio.NioEventLoopGroup;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.tcp.ProxyProvider;
import reactor.netty.tcp.TcpClient;

import java.net.InetSocketAddress;
import java.net.URI;
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
    private Configuration configuration;

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

        Configuration buildConfiguration = (configuration == null)
            ? Configuration.getGlobalConfiguration()
            : configuration;

        nettyHttpClient = nettyHttpClient
            .port(port)
            .wiretap(enableWiretap)
            .tcpConfiguration(tcpConfig -> {
                if (nioEventLoopGroup != null) {
                    tcpConfig = tcpConfig.runOn(nioEventLoopGroup);
                }

                return setupProxy(tcpConfig, proxyOptions, logger);
            });
        return new NettyAsyncHttpClient(nettyHttpClient);
    }

    /*
     * Configures a proxy to the passed 'TcpClient' if the passed 'ProxyOptions' isn't null or a configuration is
     * loadable from the environment, otherwise no proxy will be applied.
     */
    private static TcpClient setupProxy(TcpClient tcpClient, ProxyOptions proxyOptions, ClientLogger logger) {
        // ProxyOptions is set, use it.
        if (proxyOptions != null) {
            return applyProxy(tcpClient, mapProxyType(proxyOptions.getType(), logger), proxyOptions.getAddress(),
                proxyOptions.getUsername(), proxyOptions.getPassword(), null);
        }

        for (NettyProxyConfiguration proxyConfiguration : NettyProxyConfiguration.PROXY_CONFIGURATIONS_LOAD_ORDER) {
            // Proxy configuration doesn't meet the pre-requisites to be used.
            if (!proxyConfiguration.canProxyConfigurationBeApplied()) {
                continue;
            }

            String host = proxyConfiguration.getHost();

            // No host listed for this proxy choice, check the next one.
            if (CoreUtils.isNullOrEmpty(host)) {
                continue;
            }

            return applyProxy(tcpClient, proxyConfiguration.getType(),
                new InetSocketAddress(host, proxyConfiguration.getPort()), proxyConfiguration.getUsername(),
                proxyConfiguration.getPassword(), proxyConfiguration.getNonProxyHosts());
        }

        return tcpClient;
    }

    /*
     * Applies the proxy configuration to the 'TcpClient'.
     */
    private static TcpClient applyProxy(TcpClient tcpClient, ProxyProvider.Proxy type, InetSocketAddress address,
        String username, String password, String nonProxyHosts) {
        return tcpClient.proxy(typeSpec -> typeSpec.type(type)
            .address(address)
            .username(username)
            .password(user -> password)
            .nonProxyHosts(nonProxyHosts));
    }

    /*
     * Maps a 'ProxyOptions.Type' to a 'ProxyProvider.Proxy', if the type is unknown or cannot be mapped an
     * IllegalStateException will be thrown.
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
                throw logger.logExceptionAsError(new IllegalStateException(
                    String.format("Unknown Proxy type '%s' in use. Not configuring Netty proxy.", type)));
        }
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.http.netty.NettyAsyncHttpClientBuilder#proxy}
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.core.http.netty.NettyAsyncHttpClientBuilder#nioEventLoopGroup}
     *
     * @param nioEventLoopGroup The {@link NioEventLoopGroup} that will run IO loops.
     * @return the updated NettyAsyncHttpClientBuilder object.
     */
    public NettyAsyncHttpClientBuilder nioEventLoopGroup(NioEventLoopGroup nioEventLoopGroup) {
        this.nioEventLoopGroup = nioEventLoopGroup;
        return this;
    }

    /**
     * Sets the configuration store that is used during construction of the HTTP client.
     *
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
}
