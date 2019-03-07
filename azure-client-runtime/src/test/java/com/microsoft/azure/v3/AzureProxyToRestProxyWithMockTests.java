package com.microsoft.azure.v3;

import com.microsoft.azure.v3.http.MockAzureHttpClient;
import com.microsoft.rest.v3.http.HttpClient;

public class AzureProxyToRestProxyWithMockTests extends AzureProxyToRestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new MockAzureHttpClient();
    }
}
