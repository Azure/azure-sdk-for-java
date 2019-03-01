package com.microsoft.azure.v3;

import com.microsoft.rest.v3.http.HttpClient;

public class AzureProxyToRestProxyWithNettyTests extends AzureProxyToRestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return HttpClient.createDefault();
    }
}
