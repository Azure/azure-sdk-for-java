package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.implementation.http.spi.HttpClientProvider;

public class ReactorNettyClientProvider implements HttpClientProvider {

    @Override
    public HttpClient createInstance() {
        return new ReactorNettyClient();
    }
}
