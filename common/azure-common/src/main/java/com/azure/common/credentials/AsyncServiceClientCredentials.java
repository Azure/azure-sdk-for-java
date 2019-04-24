// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.credentials;

import com.azure.common.http.HttpRequest;
import reactor.core.publisher.Mono;

/**
 * Provides credentials for an HTTP request's 'Authorization' header.
 */
public interface AsyncServiceClientCredentials {
    /**
     * Given the {@code httpRequest}, generates a value for the 'Authorization' header.
     *
     * @param httpRequest The HTTP request that requires an authorization header.
     * @return The value containing currently valid credentials to put in the HTTP header, 'Authorization'.
     */
    Mono<String> authorizationHeaderValueAsync(HttpRequest httpRequest);
}
