package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.logging.ClientLogger;

public class HttpUrlConnectionClientBuilder {
    private static final ClientLogger LOGGER = new ClientLogger(HttpUrlConnectionClientBuilder.class);

    public HttpClient build() {
        HttpUrlConnectionClient client = new HttpUrlConnectionClient();
        return client;
    }

    public HttpClient build(HttpRequest request) {
        HttpUrlConnectionClient client = new HttpUrlConnectionClient(request);
        return client;
    }
}
