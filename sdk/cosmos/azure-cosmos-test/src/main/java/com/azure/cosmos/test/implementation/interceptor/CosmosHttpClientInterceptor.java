// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation.interceptor;

import com.azure.cosmos.implementation.interceptor.IHttpClientInterceptor;
import io.netty.resolver.AddressResolverGroup;
import reactor.netty.Connection;

import java.util.function.Consumer;

/**
 * Test-side HTTP client interceptor for injecting custom DNS resolvers and
 * connection handlers at client construction time.
 */
public class CosmosHttpClientInterceptor implements IHttpClientInterceptor {

    private final AddressResolverGroup<?> addressResolverGroup;
    private final Consumer<Connection> doOnConnectedCallback;

    public CosmosHttpClientInterceptor(
        AddressResolverGroup<?> addressResolverGroup,
        Consumer<Connection> doOnConnectedCallback) {

        this.addressResolverGroup = addressResolverGroup;
        this.doOnConnectedCallback = doOnConnectedCallback;
    }

    @Override
    public AddressResolverGroup<?> getAddressResolverGroup() {
        return this.addressResolverGroup;
    }

    @Override
    public Consumer<Connection> getDoOnConnectedCallback() {
        return this.doOnConnectedCallback;
    }
}
