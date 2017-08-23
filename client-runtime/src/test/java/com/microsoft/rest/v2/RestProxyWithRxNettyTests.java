package com.microsoft.rest.v2;

import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.RxNettyClient;

public class RestProxyWithRxNettyTests extends RestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new RxNettyClient();
    }
}
