// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.http.HttpClient;

public class RestProxyWithNettyTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return HttpClient.createDefault();
    }
}
