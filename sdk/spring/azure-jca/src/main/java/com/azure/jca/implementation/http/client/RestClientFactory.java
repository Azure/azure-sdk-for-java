// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.implementation.http.client;

/**
 * The RestClientFactory.
 */
public final class RestClientFactory {

    /**
     * Constructor.
     */
    private RestClientFactory() {
    }

    /**
     * Static helper method to create a RestClient.
     *
     * @return the RestClient.
     */
    public static RestClient createClient() {
        return new LegacyRestClient();
    }
}
