package com.microsoft.azure.v2;

import com.microsoft.azure.v2.http.MockAzureHttpClient;
import com.microsoft.rest.v2.http.HttpClient;

public class AzureProxyToRestProxyWithMockTests extends AzureProxyToRestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new MockAzureHttpClient();
    }
}
