// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import reactor.core.publisher.Mono;

/**
 * Interface to provide capacity to fetch fresh token
 */
public interface TokenRefresher {
    /**
     * Asynchronous call to fetch a fresh token
     * @return Wrapper for asynchronous call
     */
    Mono<String> getTokenAsync();
}
