package io.clientcore.http.netty;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpClientProvider;
import io.clientcore.core.util.ClientLogger;

/**
 * Provider class for creating instances of HttpClient using plain Netty.
 */
public final class NettyHttpClientProvider extends HttpClientProvider {
    private static final boolean ENABLE_HTTP_CLIENT_SHARING
        = Boolean.parseBoolean(System.getProperty("ENABLE_HTTP_CLIENT_SHARING", "false"));
    private static final ClientLogger LOGGER = new ClientLogger(NettyHttpClientProvider.class);
    private static final int DEFAULT_MAX_CONNECTIONS = 500;

    private enum GlobalNettyHttpClient {
        HTTP_CLIENT(new NettyHttpClientBuilder().build());

        private final HttpClient httpClient;

        GlobalNettyHttpClient(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        public HttpClient getHttpClient() {
            return httpClient;
        }
    }

    /**
     * Creates a new HttpClient instance based on the configuration.
     *
     * @return A new HttpClient instance or a shared one if sharing is enabled.
     */
    public HttpClient createInstance() {
        if (ENABLE_HTTP_CLIENT_SHARING) {
            return GlobalNettyHttpClient.HTTP_CLIENT.getHttpClient();
        }
        return new NettyHttpClient();
    }

    /**
     * Creates a new instance of {@link NettyHttpClientProvider}.
     */
    public NettyHttpClientProvider() {
    }

    @Override
    public HttpClient getNewInstance() {
        return new NettyHttpClientBuilder().build();
    }

    @Override
    public HttpClient getSharedInstance() {
        return NettyHttpClientProvider.GlobalNettyHttpClient.HTTP_CLIENT.getHttpClient();
    }
}
