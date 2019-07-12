// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.http.HttpClient;

public class AzureProxyToRestProxyWithNettyTests extends AzureProxyToRestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return HttpClient.createDefault();
    }
}
