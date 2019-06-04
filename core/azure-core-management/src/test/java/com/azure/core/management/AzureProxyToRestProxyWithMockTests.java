// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.http.HttpClient;
import com.azure.core.management.http.MockAzureHttpClient;

public class AzureProxyToRestProxyWithMockTests extends AzureProxyToRestProxyTests {
    @Override
    protected HttpClient createHttpClient() {
        return new MockAzureHttpClient();
    }
}
