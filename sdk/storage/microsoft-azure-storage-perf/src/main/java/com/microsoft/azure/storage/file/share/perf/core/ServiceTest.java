// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.file.share.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.file.CloudFileClient;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final CloudFileClient cloudFileClient;

    public ServiceTest(TOptions options) {
        super(options);
        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");
        if (connectionString == null || connectionString.isEmpty()) {
            throw new IllegalStateException("Environment variable STORAGE_CONNECTION_STRING must be set");
        }

        try {
            cloudFileClient = CloudStorageAccount.parse(connectionString).createCloudFileClient();
        } catch (InvalidKeyException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
