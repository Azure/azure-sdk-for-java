// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.test.perf.PerfStressHttpClient;
import com.azure.core.test.perf.PerfStressOptions;
import com.azure.core.test.perf.PerfStressTest;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final BlobServiceClient BlobServiceClient;
    protected final BlobServiceAsyncClient BlobServiceAsyncClient;

    public ServiceTest(TOptions options) {
        super(options);

        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");

        if (connectionString == null || connectionString.isEmpty()) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        BlobServiceClientBuilder builder = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .httpClient(PerfStressHttpClient.create(options));

        BlobServiceClient = builder.buildClient();
        BlobServiceAsyncClient = builder.buildAsyncClient();
    }
}
