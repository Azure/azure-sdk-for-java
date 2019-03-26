package com.azure.common.mgmt;

import com.azure.common.http.HttpClient;

public class AzureProxyToRestProxyWithNettyTests extends AzureProxyToRestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return HttpClient.createDefault();
    }
}
