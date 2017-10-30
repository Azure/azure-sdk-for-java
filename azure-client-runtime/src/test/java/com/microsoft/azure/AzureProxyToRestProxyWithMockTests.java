package com.microsoft.azure;

import com.microsoft.azure.http.MockAzureHttpClient;
import com.microsoft.rest.v2.http.HttpClient;

public class AzureProxyToRestProxyWithMockTests extends AzureProxyToRestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new MockAzureHttpClient();
    }
}
