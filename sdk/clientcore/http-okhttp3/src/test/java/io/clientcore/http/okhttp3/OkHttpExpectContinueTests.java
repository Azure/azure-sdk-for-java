// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.shared.ExpectContinueTests;
import io.clientcore.core.utils.configuration.Configuration;

public class OkHttpExpectContinueTests extends ExpectContinueTests {
    private final HttpClient client = new OkHttpHttpClientBuilder().configuration(Configuration.none()).build();

    @Override
    protected HttpClient getHttpClient() {
        return client;
    }
}
