package com.microsoft.rest.v3.implementation;

import com.microsoft.rest.v3.http.HttpClient;

public class RestProxyWithNettyTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return HttpClient.createDefault();
    }
}
