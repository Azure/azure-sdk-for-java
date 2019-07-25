package com.azure.core.http.netty;

import com.azure.core.http.HttpClient;
import com.azure.core.implementation.http.spi.HttpClientProvider;

public class ReactorNettyClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createNewInstance() {
        return new ReactorNettyClient();
    }
}
