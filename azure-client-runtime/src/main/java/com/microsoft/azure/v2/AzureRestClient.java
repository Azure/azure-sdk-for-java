/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.azure.v2.serializer.AzureJacksonAdapter;
import com.microsoft.rest.v2.RestClient;

/**
 * Helpers for producing {@link RestClient} with Azure-specific configuration.
 */
public final class AzureRestClient {
    private AzureRestClient() { }

    /**
     * A RestClient which provides default values for Azure clients.
     * Users can modify the default values by calling {@link RestClient#newBuilder()}.
     */
    public static final RestClient DEFAULT = new RestClient.Builder(new AzureJacksonAdapter()).build();

    /**
     * @return A new {@link RestClient.Builder} instance with default values for Azure clients.
     */
    public static RestClient.Builder newDefaultBuilder() {
        return DEFAULT.newBuilder();
    }
}
