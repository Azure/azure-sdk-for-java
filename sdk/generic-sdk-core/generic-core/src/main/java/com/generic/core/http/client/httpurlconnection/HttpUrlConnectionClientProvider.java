// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.httpurlconnection;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.client.HttpClientProvider;
import com.generic.core.http.models.HttpClientOptions;
import com.generic.core.util.configuration.Configuration;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpURLConnection.
 */
public class HttpUrlConnectionClientProvider implements HttpClientProvider {
    private static final boolean ENABLE_HTTP_CLIENT_SHARING =
        Configuration.getGlobalConfiguration().get("ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    private final boolean enableHttpClientSharing;

    // Enum Singleton Pattern
    private enum GlobalHttpUrlConnectionHttpClient {
        HTTP_CLIENT(new HttpUrlConnectionClientBuilder().build());

        private final HttpClient httpClient;

        GlobalHttpUrlConnectionHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private HttpClient getHttpClient() {
            return httpClient;
        }
    }

    /**
     * For testing purpose only, assigning 'AZURE_ENABLE_HTTP_CLIENT_SHARING' to 'enableHttpClientSharing' for
     * 'final' modifier.
     */
    public HttpUrlConnectionClientProvider() {
        enableHttpClientSharing = ENABLE_HTTP_CLIENT_SHARING;
    }

    HttpUrlConnectionClientProvider(Configuration configuration) {
        enableHttpClientSharing = configuration.get("ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    }

    @Override
    public HttpClient createInstance() {
        if (enableHttpClientSharing) {
            return GlobalHttpUrlConnectionHttpClient.HTTP_CLIENT.getHttpClient();
        }
        return new HttpUrlConnectionClientBuilder().build();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        if (clientOptions == null) {
            return createInstance();
        }

        HttpUrlConnectionClientBuilder builder = new HttpUrlConnectionClientBuilder();
        builder = builder.proxy(clientOptions.getProxyOptions())
            .connectionTimeout(clientOptions.getConnectTimeout())
            .readTimeout(clientOptions.getReadTimeout());

        return builder.build();
    }
}
