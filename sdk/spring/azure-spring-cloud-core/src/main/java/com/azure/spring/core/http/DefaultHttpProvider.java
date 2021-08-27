package com.azure.spring.core.http;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpClientProvider;
import com.azure.core.util.HttpClientOptions;

public class DefaultHttpProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return HttpClient.createDefault();
    }

    @Override
    public HttpClient createInstance(HttpClientOptions clientOptions) {
        return HttpClient.createDefault(clientOptions);
    }
}