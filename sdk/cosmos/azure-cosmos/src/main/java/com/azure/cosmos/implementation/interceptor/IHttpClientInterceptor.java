// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.interceptor;

import io.netty.resolver.AddressResolverGroup;
import reactor.netty.Connection;

import java.util.function.Consumer;

/**
 * Interceptor for HTTP client configuration at construction time.
 * <p>
 * This interface allows test code (azure-cosmos-test) to inject custom DNS resolvers
 * and connection handlers without polluting public API classes like ConnectionPolicy.
 * <p>
 * Production code checks: if interceptor is null → normal flow, zero overhead.
 */
public interface IHttpClientInterceptor {

    /**
     * Returns a custom AddressResolverGroup for DNS resolution, or null to use the default.
     */
    AddressResolverGroup<?> getAddressResolverGroup();

    /**
     * Returns a doOnConnected callback to install on the HTTP client, or null for none.
     */
    Consumer<Connection> getDoOnConnectedCallback();
}
