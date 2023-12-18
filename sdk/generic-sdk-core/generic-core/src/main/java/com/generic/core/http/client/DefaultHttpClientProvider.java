// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client;

import com.generic.core.util.configuration.Configuration;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpURLConnection.
 */
class DefaultHttpClientProvider implements HttpClientProvider {
    private static final boolean ENABLE_HTTP_CLIENT_SHARING =
        Configuration.getGlobalConfiguration().get("ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    private final boolean enableHttpClientSharing;

    // Enum Singleton Pattern
    private enum GlobalHttpUrlConnectionHttpClient {
        HTTP_CLIENT(new DefaultHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalHttpUrlConnectionHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private HttpClient getHttpClient() {
            return httpClient;
        }
    }

    /**
     * For testing purpose only, assigning 'ENABLE_HTTP_CLIENT_SHARING' to 'enableHttpClientSharing' for
     * 'final' modifier.
     */
    public DefaultHttpClientProvider() {
        enableHttpClientSharing = ENABLE_HTTP_CLIENT_SHARING;
    }

    DefaultHttpClientProvider(Configuration configuration) {
        enableHttpClientSharing = configuration.get("ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    }

    @Override
    public HttpClient createInstance() {
        if (enableHttpClientSharing) {
            return GlobalHttpUrlConnectionHttpClient.HTTP_CLIENT.getHttpClient();
        }
        return new DefaultHttpClientBuilder().build();
    }
}
