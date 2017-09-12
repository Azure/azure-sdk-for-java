package com.microsoft.azure.v2;

import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.RxNettyAdapter;

public class AzureProxyToRestProxyWithRxNettyTests extends AzureProxyToRestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new RxNettyAdapter();
    }
}
