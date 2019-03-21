package com.azure.common.mgmt;

import com.azure.common.mgmt.http.MockAzureHttpClient;
import com.azure.common.http.HttpClient;

public class AzureProxyToRestProxyWithMockTests extends AzureProxyToRestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new MockAzureHttpClient();
    }
}
