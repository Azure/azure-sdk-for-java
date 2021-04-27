// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.jca.implementation.http.client;

public final class RestClientFactory {

    private RestClientFactory() {
    }

    public static RestClient createClient() {
        return new LegacyRestClient();
    }
}
