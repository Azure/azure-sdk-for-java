// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import io.netty.resolver.AbstractAddressResolver;
import io.netty.resolver.AddressResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementation of {@link AddressResolver} used when there is a proxy applied, and it has non-proxy hosts configured.
 * <p>
 * This class maintains a reference to the non-proxy hosts configured in the Reactor Netty HttpClient it is associated
 * with and will determine whether to use the default or no-op resolver when resolving the address based on whether the
 * request would be sent to the proxy.
 * <p>
 * Before this class was created it was an either-or situation. If a proxy existed the no-op resolver would be used. If
 * a proxy didn't exist, we left the behavior up to the Reactor Netty HttpClient to determine which one to use. This
 * caused issues with non-proxy host situations as the address would need to be resolved for the request to be sent when
 * the proxy wouldn't be used.
 */
final class NonProxyHostAddressResolver extends AbstractAddressResolver<InetSocketAddress> {
    private final Pattern nonProxyHostsPattern;
    private final AddressResolver<InetSocketAddress> defaultResolver;
    private final AddressResolver<InetSocketAddress> noopResolver;

    NonProxyHostAddressResolver(EventExecutor eventExecutor, Pattern nonProxyHostsPattern,
        AddressResolver<InetSocketAddress> defaultResolver, AddressResolver<InetSocketAddress> noopResolver) {
        super(eventExecutor);
        this.nonProxyHostsPattern = nonProxyHostsPattern;
        this.defaultResolver = defaultResolver;
        this.noopResolver = noopResolver;
    }

    @Override
    protected boolean doIsResolved(InetSocketAddress address) {
        return !address.isUnresolved();
    }

    @Override
    protected void doResolve(InetSocketAddress unresolvedAddress, Promise<InetSocketAddress> promise) {
        if (nonProxyHostsPattern.matcher(unresolvedAddress.getHostString()).matches()) {
            defaultResolver.resolve(unresolvedAddress, promise);
        } else {
            noopResolver.resolve(unresolvedAddress, promise);
        }
    }

    @Override
    protected void doResolveAll(InetSocketAddress unresolvedAddress, Promise<List<InetSocketAddress>> promise) {
        if (nonProxyHostsPattern.matcher(unresolvedAddress.getHostString()).matches()) {
            defaultResolver.resolveAll(unresolvedAddress, promise);
        } else {
            noopResolver.resolveAll(unresolvedAddress, promise);
        }
    }
}
