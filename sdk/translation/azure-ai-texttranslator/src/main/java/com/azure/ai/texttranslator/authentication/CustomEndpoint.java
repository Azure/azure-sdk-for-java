// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.texttranslator.authentication;

import java.util.Objects;

/**
 * Custom endpoint for translator service.
 */
public class CustomEndpoint {
    private final String endpoint;

    /**
     * Creates an instance of CustomEndpoint class.
     *
     * @param endpoint Custom Endpoint for translator service.
     */
    public CustomEndpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "'endpoint' cannot be null.");
        this.endpoint = endpoint;
    }

    /**
     * Get the endpoint.
     *
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }
}
