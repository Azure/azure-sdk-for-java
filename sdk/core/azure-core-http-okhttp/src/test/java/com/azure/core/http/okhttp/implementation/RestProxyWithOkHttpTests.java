// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.okhttp.OkHttpAsyncHttpClientBuilder;
import com.azure.core.test.implementation.RestProxyTests;
import org.junit.Ignore;
import org.junit.Test;

public class RestProxyWithOkHttpTests extends RestProxyTests {

    @Override
    protected HttpClient createHttpClient() {
        return new OkHttpAsyncHttpClientBuilder().build();
    }

    @Test
    @Override
    @Ignore("OKHttp does not support setting null as header value.")
    public void syncGetRequestWithNullHeader() {
        super.syncGetRequestWithNullHeader();
    }
}
