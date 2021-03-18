// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.annotation.Get;
import com.azure.core.annotation.Host;

import com.azure.core.annotation.ServiceInterface;
import reactor.core.publisher.Mono;


/**
 * Mock Rest Proxy Service for Performance Testing.
 */
@Host("https://unused")
@ServiceInterface(name = "MyMockService")
public interface MyRestProxyService {

    /**
     * List all the subscriptions
     * @return A {@link Mono} containing Void.
     */
    @Get("ListSubscriptions")
    Mono<Void> listSubscriptions();
}
