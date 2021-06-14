// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf.core;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.connectionstring.StorageConnectionString;
import com.azure.storage.common.implementation.connectionstring.StorageEndpoint;
import com.azure.storage.file.datalake.DataLakeServiceAsyncClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final DataLakeServiceClient dataLakeServiceClient;
    protected final DataLakeServiceAsyncClient dataLakeServiceAsyncClient;
    private final Configuration configuration;

    public ServiceTest(TOptions options) {
        super(options);
        configuration = Configuration.getGlobalConfiguration().clone();
        String connectionString = configuration.get("STORAGE_CONNECTION_STRING");

        if (CoreUtils.isNullOrEmpty(connectionString)) {
            throw new IllegalStateException("Environment variable STORAGE_CONNECTION_STRING must be set");
        }

        StorageConnectionString storageConnectionString
            = StorageConnectionString.create(connectionString, null);
        StorageEndpoint endpoint = storageConnectionString.getBlobEndpoint();

        dataLakeServiceClient = new DataLakeServiceClientBuilder()
            .endpoint(endpoint.getPrimaryUri())
            .credential(new StorageSharedKeyCredential(storageConnectionString.getAccountName(),
                storageConnectionString.getStorageAuthSettings().getAccount().getAccessKey()))
            .buildClient();

        dataLakeServiceAsyncClient = new DataLakeServiceClientBuilder()
            .endpoint(endpoint.getPrimaryUri())
            .credential(new StorageSharedKeyCredential(storageConnectionString.getAccountName(),
                storageConnectionString.getStorageAuthSettings().getAccount().getAccessKey()))
            .buildAsyncClient();
    }
}
