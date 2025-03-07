// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.net.ProxySelector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link OkHttpHttpClientProvider}.
 */
public class OkHttpHttpClientProviderTests {
    @Test
    public void testGetSharedClient() {
        OkHttpHttpClient okHttpHttpClient = (OkHttpHttpClient) new OkHttpHttpClientProvider().getSharedInstance();
        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());

        if (environmentProxy == null) {
            assertNull(okHttpHttpClient.httpClient.proxy());
        } else {
            // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
            ProxySelector proxySelector = okHttpHttpClient.httpClient.proxySelector();

            assertNotNull(proxySelector);
            assertEquals(environmentProxy.getAddress(), proxySelector.select(null).get(0).address());
        }
    }

    @Test
    public void testGetNewClient() {
        OkHttpHttpClient httpClient = (OkHttpHttpClient) new OkHttpHttpClientProvider().getNewInstance();
        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());

        if (environmentProxy == null) {
            assertNull(httpClient.httpClient.proxy());
        } else {
            // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
            ProxySelector proxySelector = httpClient.httpClient.proxySelector();
            assertNotNull(proxySelector);
            assertEquals(environmentProxy.getAddress(), proxySelector.select(null).get(0).address());
        }
    }
}
