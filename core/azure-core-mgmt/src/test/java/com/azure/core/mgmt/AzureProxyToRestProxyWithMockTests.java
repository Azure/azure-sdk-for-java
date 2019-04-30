// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.mgmt;

import com.azure.core.http.HttpClient;
import com.azure.core.mgmt.http.MockAzureHttpClient;

public class AzureProxyToRestProxyWithMockTests extends AzureProxyToRestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new MockAzureHttpClient();
    }
}
