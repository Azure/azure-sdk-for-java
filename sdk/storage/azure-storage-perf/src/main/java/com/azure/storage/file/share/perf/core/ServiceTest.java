// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final ShareServiceClient shareServiceClient;
    protected final ShareServiceAsyncClient shareServiceAsyncClient;


    public ServiceTest(TOptions options) {
        super(options);
        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        shareServiceClient = new ShareServiceClientBuilder().connectionString(connectionString).
            buildClient();

        shareServiceAsyncClient = new ShareServiceClientBuilder().connectionString(connectionString).
            buildAsyncClient();
    }
}
