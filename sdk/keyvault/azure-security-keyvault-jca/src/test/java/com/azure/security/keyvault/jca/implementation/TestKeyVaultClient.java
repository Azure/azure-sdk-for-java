// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Test client that replaces Key Vault HTTP calls with a caller-provided response function.
 */
public final class TestKeyVaultClient extends KeyVaultClient {
    private final BiFunction<String, Map<String, String>, String> httpGet;

    /**
     * Creates a test client.
     *
     * @param accessToken the fixed access token
     * @param disableAiaDownload whether AIA downloads are disabled
     * @param httpGet the HTTP GET response function
     */
    public TestKeyVaultClient(String accessToken, boolean disableAiaDownload,
        BiFunction<String, Map<String, String>, String> httpGet) {
        super("https://fake.vault.azure.net/", null, null, null, null, accessToken, false, disableAiaDownload);
        this.httpGet = httpGet;
    }

    @Override
    String httpGet(String uri, Map<String, String> headers) {
        return httpGet.apply(uri, headers);
    }
}
