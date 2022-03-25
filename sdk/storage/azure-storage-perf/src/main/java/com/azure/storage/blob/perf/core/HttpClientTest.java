// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.util.UUID;

public abstract class HttpClientTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    public final Configuration configuration;
    public final String connectionString;
    private static final String CONTAINER_NAME = "perfstress-" + UUID.randomUUID().toString();


    public HttpClientTest(TOptions options) {
        super(options);
        configuration = Configuration.getGlobalConfiguration().clone();
        connectionString = configuration.get("STORAGE_CONNECTION_STRING");
//        boolean isHttpClientShared = configuration.get("AZURE_DISABLE_HTTP_CLIENT_SHARING").;
//        if (CoreUtils.isNullOrEmpty(connectionString)) {
//            throw new IllegalStateException("Environment variable STORAGE_CONNECTION_STRING must be set");
//    }

        // Setup the service client
//        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
//                                               .connectionString(connectionString);
//
//        configureClientBuilder(builder);

    }
}
