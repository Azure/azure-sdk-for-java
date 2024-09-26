// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.jdk.httpclient;

import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.util.configuration.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.net.ProxySelector;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link JdkHttpClientProvider}.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkHttpClientProviderTests {
    @Test
    public void testGetSharedInstance() {
        JdkHttpClient jdkHttpClient = (JdkHttpClient) new JdkHttpClientProvider().getSharedInstance();
        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());

        if (environmentProxy == null) {
            assertTrue(jdkHttpClient.jdkHttpClient.proxy().isEmpty());
        } else {
            // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
            Optional<ProxySelector> optionalProxySelector = jdkHttpClient.jdkHttpClient.proxy();

            assertTrue(optionalProxySelector.isPresent());
            assertEquals(environmentProxy.getAddress(), optionalProxySelector.get().select(null).get(0).address());
        }
    }

    @Test
    public void testGetNewInstance() {
        JdkHttpClient jdkHttpClient = (JdkHttpClient) new JdkHttpClientProvider().getNewInstance();
        ProxyOptions environmentProxy = ProxyOptions.fromConfiguration(Configuration.getGlobalConfiguration());

        if (environmentProxy == null) {
            assertTrue(jdkHttpClient.jdkHttpClient.proxy().isEmpty());
        } else {
            // Proxy isn't configured on the OkHttp HttpClient when a proxy exists, the ProxySelector is configured.
            Optional<ProxySelector> optionalProxySelector = jdkHttpClient.jdkHttpClient.proxy();

            assertTrue(optionalProxySelector.isPresent());
            assertEquals(environmentProxy.getAddress(), optionalProxySelector.get().select(null).get(0).address());
        }
    }
}
