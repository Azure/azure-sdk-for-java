package com.azure.common.implementation;

import com.azure.common.http.HttpClient;

public class RestProxyWithNettyTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return HttpClient.createDefault();
    }
}
