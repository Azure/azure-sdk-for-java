package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.Configuration;
import com.azure.core.util.HttpClientOptions;

/**
 * An {@link HttpClientProvider} that provides an implementation of HttpClient based on native JDK HttpUrlConnection.
 * <p>
 * NOTE: This implementation is only available in Java 8+ as that is when {@link java.net.http.HttpUrlConnection} was
 * introduced.
 */
public class HttpUrlConnectionClientProvider implements HttpClientProvider {
    private static final boolean AZURE_ENABLE_HTTP_CLIENT_SHARING =
        Configuration.getGlobalConfiguration().get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
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
        enableHttpClientSharing = AZURE_ENABLE_HTTP_CLIENT_SHARING;
    }

    HttpUrlConnectionClientProvider(Configuration configuration) {
        enableHttpClientSharing = configuration.get("AZURE_ENABLE_HTTP_CLIENT_SHARING", Boolean.FALSE);
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
            .configuration(clientOptions.getConfiguration())
            .connectionTimeout(clientOptions.getConnectTimeout());

        return builder.build();
    }
}
