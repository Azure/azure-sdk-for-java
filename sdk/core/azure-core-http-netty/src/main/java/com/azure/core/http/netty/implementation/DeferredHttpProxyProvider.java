// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.ProxyOptions;
import com.azure.core.util.AuthorizationChallengeHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * This class defers supplying a channel pipeline with a proxy handler.
 */
public class DeferredHttpProxyProvider implements Function<Bootstrap, BiConsumer<ConnectionObserver, Channel>> {
    private final AuthorizationChallengeHandler challengeHandler;
    private final AtomicReference<ChallengeHolder> proxyChallengeHolderReference;
    private final InetSocketAddress proxyAddress;
    private final String username;
    private final String password;
    private final String nonProxyHosts;

    private final Pattern nonProxyHostsPattern;

    public DeferredHttpProxyProvider(AuthorizationChallengeHandler challengeHandler,
        AtomicReference<ChallengeHolder> proxyChallengeHolderReference, ProxyOptions proxyOptions) {
        this.challengeHandler = challengeHandler;
        this.proxyChallengeHolderReference = proxyChallengeHolderReference;
        this.proxyAddress = proxyOptions.getAddress();
        this.username = proxyOptions.getUsername();
        this.password = proxyOptions.getPassword();
        this.nonProxyHosts = proxyOptions.getNonProxyHosts();

        this.nonProxyHostsPattern = (nonProxyHosts == null)
            ? null
            : Pattern.compile(nonProxyHosts, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public BiConsumer<ConnectionObserver, Channel> apply(Bootstrap bootstrap) {
        return ((connectionObserver, channel) -> {
            if (shouldApplyProxy(bootstrap.config().remoteAddress())) {
                channel.pipeline()
                    .addFirst(NettyPipeline.ProxyHandler, new HttpProxyHandler(proxyAddress, challengeHandler,
                        proxyChallengeHolderReference))
                    .addLast("azure.proxy.exceptionHandler", new HttpProxyExceptionHandler());
            }
        });
    }

    private boolean shouldApplyProxy(SocketAddress socketAddress) {
        if (nonProxyHostsPattern == null) {
            return true;
        }

        if (!(socketAddress instanceof  InetSocketAddress)) {
            return true;
        }

        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;

        return !nonProxyHostsPattern.matcher(inetSocketAddress.getHostString()).matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof DeferredHttpProxyProvider)) {
            return false;
        }

        DeferredHttpProxyProvider other = (DeferredHttpProxyProvider) o;

        return Objects.equals(username, other.username)
            && Objects.equals(password, other.password)
            && Objects.equals(proxyAddress, other.proxyAddress)
            && Objects.equals(nonProxyHosts, other.nonProxyHosts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(proxyAddress, password, proxyAddress, nonProxyHosts);
    }
}
