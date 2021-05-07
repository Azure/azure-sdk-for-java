// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected static final long MAX_SHARE_SIZE = 4398046511104L;

    protected final ShareServiceClient shareServiceClient;
    protected final ShareServiceAsyncClient shareServiceAsyncClient;
    private final Configuration configuration;

    public ServiceTest(TOptions options) {
        super(options);
        configuration = Configuration.getGlobalConfiguration().clone();
        String connectionString = configuration.get("STORAGE_CONNECTION_STRING");
        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable STORAGE_CONNECTION_STRING must be set");
        }

        shareServiceClient = new ShareServiceClientBuilder().connectionString(connectionString).
            buildClient();

        shareServiceAsyncClient = new ShareServiceClientBuilder().connectionString(connectionString).
            buildAsyncClient();
    }
}
