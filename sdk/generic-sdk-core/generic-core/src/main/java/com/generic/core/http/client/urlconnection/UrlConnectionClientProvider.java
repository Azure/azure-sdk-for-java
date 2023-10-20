// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.urlconnection;

import com.generic.core.http.client.HttpClient;
import com.generic.core.http.client.HttpClientProvider;
import com.generic.core.util.configuration.Configuration;
import com.generic.core.http.models.HttpClientOptions;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpURLConnection.
 */
public class UrlConnectionClientProvider implements HttpClientProvider {
    private static final boolean AZURE_ENABLE_HTTP_CLIENT_SHARING =
        Configuration.getGlobalConfiguration().get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    private final boolean enableHttpClientSharing;

    // Enum Singleton Pattern
    private enum GlobalHttpUrlConnectionHttpClient {
        HTTP_CLIENT(new UrlConnectionClientBuilder().build());

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
    public UrlConnectionClientProvider() {
        enableHttpClientSharing = AZURE_ENABLE_HTTP_CLIENT_SHARING;
    }

    UrlConnectionClientProvider(Configuration configuration) {
        enableHttpClientSharing = configuration.get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
    }

    @Override
    public HttpClient createInstance() {
        if (enableHttpClientSharing) {
            return GlobalHttpUrlConnectionHttpClient.HTTP_CLIENT.getHttpClient();
        }
        return new UrlConnectionClientBuilder().build();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        if (clientOptions == null) {
            return createInstance();
        }

        UrlConnectionClientBuilder builder = new UrlConnectionClientBuilder();
        builder = builder
            .configuration(clientOptions.getConfiguration())
            .connectionTimeout(clientOptions.getConnectTimeout())
            .readTimeout(clientOptions.getReadTimeout())
            .writeTimeout(clientOptions.getWriteTimeout())
            .responseTimeout(clientOptions.getResponseTimeout());

        return builder.build();
    }
}
