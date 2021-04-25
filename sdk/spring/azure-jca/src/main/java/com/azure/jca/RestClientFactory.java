// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca;

/**
 * The RestClientFactory.
 */
final class RestClientFactory {

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
    static RestClient createClient() {
        return new LegacyRestClient();
    }
}
