// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.util.Configuration;
import com.azure.core.validation.http.HttpProtocolVersionTests;
import okhttp3.OkHttpClient;

/**
 * Tests HTTP-version negotiation with OkHttp.
 */
public class OkHttpAsyncHttpClientHttp2Tests extends HttpProtocolVersionTests {
    @Override
    protected HttpClient createHttpClient(HttpProtocolVersion version, boolean applySetting) throws Exception {
        OkHttpClient nativeClient
            = new OkHttpClient.Builder().sslSocketFactory(getSslContext().getSocketFactory(), getTrustManager())
                .build();
        OkHttpAsyncHttpClientBuilder builder
            = new OkHttpAsyncHttpClientBuilder(nativeClient).configuration(Configuration.NONE);
        if (applySetting) {
            builder.maximumHttpVersion(version);
        }
        return builder.build();
    }

    @Override
    protected HttpProtocolVersion getDefaultMaximumHttpVersion() {
        return HttpProtocolVersion.HTTP_2;
    }

    @Override
    protected void closeHttpClient(HttpClient client) {
        OkHttpClient nativeClient = ((OkHttpAsyncHttpClient) client).httpClient;
        nativeClient.connectionPool().evictAll();
        nativeClient.dispatcher().executorService().shutdown();
    }
}
