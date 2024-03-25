// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.client;

import com.generic.core.http.client.DefaultHttpClientBuilder;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.client.HttpClientProvider;
import com.generic.core.util.configuration.Configuration;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpURLConnection.
 */
public class DefaultHttpClientProvider implements HttpClientProvider {
    private static final boolean ENABLE_HTTP_CLIENT_SHARING =
        Configuration.getGlobalConfiguration().get("ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    private final boolean enableHttpClientSharing;

    // Enum Singleton Pattern
    public enum GlobalHttpUrlConnectionHttpClient {
        HTTP_CLIENT(new DefaultHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalHttpUrlConnectionHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public HttpClient getHttpClient() {
            return httpClient;
        }
    }

    /**
     * For testing purpose only, assigning 'ENABLE_HTTP_CLIENT_SHARING' to 'enableHttpClientSharing' for 'final'
     * modifier.
     */
    public DefaultHttpClientProvider() {
        enableHttpClientSharing = ENABLE_HTTP_CLIENT_SHARING;
    }

    public DefaultHttpClientProvider(Configuration configuration) {
        enableHttpClientSharing = configuration.get("ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    }

    @Override
    public HttpClient getInstance() {
        if (enableHttpClientSharing) {
            return GlobalHttpUrlConnectionHttpClient.HTTP_CLIENT.getHttpClient();
        }

        return new DefaultHttpClientBuilder().build();
    }
}
