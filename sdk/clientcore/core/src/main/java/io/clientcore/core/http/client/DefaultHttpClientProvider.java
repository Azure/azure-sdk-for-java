// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.http.client;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on the default {@link HttpClient}.
 */
public class DefaultHttpClientProvider extends HttpClientProvider {
    // Enum Singleton Pattern
    private enum GlobalDefaultHttpClient {
        HTTP_CLIENT(new DefaultHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalDefaultHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        private HttpClient getHttpClient() {
            return httpClient;
        }
    }

    /**
     * Creates a new instance of {@link DefaultHttpClientProvider}.
     */
    public DefaultHttpClientProvider() {
    }

    @Override
    public HttpClient getNewInstance() {
        return new DefaultHttpClientBuilder().build();
    }

    @Override
    public HttpClient getSharedInstance() {
        return GlobalDefaultHttpClient.HTTP_CLIENT.getHttpClient();
    }
}
