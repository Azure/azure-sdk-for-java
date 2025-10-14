// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.http.netty4.NettyHttpClientBuilder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A {@link Predicate} that determines whether a Netty {@link ChannelPipeline} should have proxying configured during
 * {@link ChannelInitializer channel initialization}.
 */
public final class ChannelInitializationProxyHandler implements Predicate<SocketAddress> {
    private final ProxyOptions proxyOptions;
    private final Pattern nonProxyHostsPattern;

    /**
     * Creates a new instance of {@link ChannelInitializationProxyHandler}.
     *
     * @param proxyOptions The {@link ProxyOptions} configured in a {@link NettyHttpClientBuilder} when
     * {@link NettyHttpClientBuilder#build()} was called or inferred using
     * {@link ProxyOptions#fromConfiguration(Configuration)}.
     */
    public ChannelInitializationProxyHandler(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        if (proxyOptions == null || CoreUtils.isNullOrEmpty(proxyOptions.getNonProxyHosts())) {
            nonProxyHostsPattern = null;
        } else {
            // HTTP host names are case-insensitive.
            nonProxyHostsPattern = Pattern.compile(proxyOptions.getNonProxyHosts(), Pattern.CASE_INSENSITIVE);
        }
    }

    @Override
    public boolean test(SocketAddress socketAddress) {
        // If there aren't ProxyOptions, don't proxy.
        if (proxyOptions == null || proxyOptions.getAddress() == null) {
            return false;
        }

        // If there aren't any non-proxy hosts, always add the proxy.
        if (nonProxyHostsPattern == null) {
            return true;
        }

        if (!(socketAddress instanceof InetSocketAddress)) {
            // Cannot determine host string from non-InetSocketAddress, don't proxy.
            return false;
        }

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        String hostString = inetSocketAddress.getHostString();

        return hostString != null && !nonProxyHostsPattern.matcher(hostString).matches();
    }

    /**
     * Creates the {@link ProxyHandler} that will be added to the {@link ChannelPipeline}.
     *
     * @param proxyChallenges The holder for {@link AuthenticateChallenge}s across requests. Used to calculate
     * {@code Proxy-Authorization} eagerly if the {@link HttpClient} that passed the {@link AtomicReference} previously
     * received {@code Proxy-Authenticate} headers from the proxy.
     * @return The {@link ProxyHandler} that will handle proxying.
     */
    public ProxyHandler createProxy(AtomicReference<List<AuthenticateChallenge>> proxyChallenges) {
        if (proxyOptions.getType() == ProxyOptions.Type.SOCKS4) {
            return new Socks4ProxyHandler(proxyOptions.getAddress(), proxyOptions.getUsername());
        } else if (proxyOptions.getType() == ProxyOptions.Type.SOCKS5) {
            return new Socks5ProxyHandler(proxyOptions.getAddress(), proxyOptions.getUsername(),
                proxyOptions.getPassword());
        } else if (proxyOptions.getUsername() != null && proxyOptions.getPassword() != null) {
            return new Netty4HttpProxyHandler(proxyOptions, proxyChallenges);
        } else {
            return new HttpProxyHandler(proxyOptions.getAddress());
        }
    }
}
