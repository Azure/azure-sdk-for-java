package com.azure.core.http.httpurlconnection;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;

public class HttpUrlConnectionClientProvider implements HttpClientProvider {
    @Override
    public HttpClient createInstance() {
        return new HttpUrlConnectionClientBuilder().build();
    }
}
