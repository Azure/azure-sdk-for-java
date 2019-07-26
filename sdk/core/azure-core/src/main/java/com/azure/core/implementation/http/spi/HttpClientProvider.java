package com.azure.core.implementation.http.spi;

import com.azure.core.http.HttpClient;

public interface HttpClientProvider {

    HttpClient createInstance();
}
