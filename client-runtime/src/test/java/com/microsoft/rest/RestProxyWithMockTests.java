package com.microsoft.rest;

import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.MockHttpClient;

public class RestProxyWithMockTests extends RestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new MockHttpClient();
    }
}
