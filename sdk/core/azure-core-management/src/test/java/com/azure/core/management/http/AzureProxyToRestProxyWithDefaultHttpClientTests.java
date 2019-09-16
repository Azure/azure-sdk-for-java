// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.http;

import com.azure.core.http.HttpClient;
import com.azure.core.management.AzureProxyToRestProxyTests;
import org.junit.Ignore;

@Ignore
public class AzureProxyToRestProxyWithDefaultHttpClientTests extends AzureProxyToRestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return HttpClient.createDefault();
    }
}
