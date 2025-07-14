// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.DefaultAddressResolverGroup;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.concurrent.EventExecutor;

import java.net.InetSocketAddress;
import java.util.regex.Pattern;

/**
 * Implementation of {@link AddressResolverGroup} used when there is a proxy applied, and it has non-proxy hosts
 * configured.
 * <p>
 * This class maintains a reference to the non-proxy hosts configured in the Reactor Netty HttpClient it is associated
 * with and will determine whether to use {@link DefaultAddressResolverGroup} or {@link NoopAddressResolverGroup} when
 * resolving the address based on whether the request would be sent to the proxy.
 * <p>
 * Before this class was created it was an either-or situation. If a proxy existed {@link NoopAddressResolverGroup}
 * would be used. If a proxy didn't exist, we left the behavior up to the Reactor Netty HttpClient to determine which
 * one to use. This caused issues with non-proxy host situations as the address would need to be resolved for the
 * request to be sent when the proxy wouldn't be used.
 */
public final class NonProxyHostAddressResolverGroup extends AddressResolverGroup<InetSocketAddress> {
    private final Pattern nonProxyHostsPattern;

    /**
     * Creates an instance of {@link NonProxyHostAddressResolverGroup}.
     *
     * @param nonProxyHostsPattern The pattern that will determine whether to use {@link DefaultAddressResolverGroup}
     * or {@link NoopAddressResolverGroup} to resolve the address.
     */
    public NonProxyHostAddressResolverGroup(Pattern nonProxyHostsPattern) {
        this.nonProxyHostsPattern = nonProxyHostsPattern;
    }

    @Override
    protected AddressResolver<InetSocketAddress> newResolver(EventExecutor executor) {
        return new NonProxyHostAddressResolver(executor, nonProxyHostsPattern,
            DefaultAddressResolverGroup.INSTANCE.getResolver(executor),
            new InetSocketAddressNoopAddressResolver(executor));
    }
}
