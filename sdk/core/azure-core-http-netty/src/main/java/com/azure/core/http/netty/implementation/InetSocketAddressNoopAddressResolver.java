// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import io.netty.resolver.AbstractAddressResolver;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;

/**
 * An {@link AbstractAddressResolver} implementation that no-ops resolving the {@link InetSocketAddress}.
 */
final class InetSocketAddressNoopAddressResolver extends AbstractAddressResolver<InetSocketAddress> {

    InetSocketAddressNoopAddressResolver(EventExecutor executor) {
        super(executor);
    }

    @Override
    protected boolean doIsResolved(InetSocketAddress address) {
        return true;
    }

    @Override
    protected void doResolve(InetSocketAddress unresolvedAddress, Promise<InetSocketAddress> promise) {
        promise.setSuccess(unresolvedAddress);
    }

    @Override
    protected void doResolveAll(InetSocketAddress unresolvedAddress, Promise<List<InetSocketAddress>> promise) {
        promise.setSuccess(Collections.singletonList(unresolvedAddress));
    }
}
