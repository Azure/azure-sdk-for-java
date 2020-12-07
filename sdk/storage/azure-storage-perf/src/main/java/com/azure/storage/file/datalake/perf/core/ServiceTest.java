// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.DataLakeServiceAsyncClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final DataLakeServiceClient dataLakeServiceClient;
    protected final DataLakeServiceAsyncClient dataLakeServiceAsyncClient;


    public ServiceTest(TOptions options) {
        super(options);
        String accountName = System.getenv("STORAGE_DATALAKE_ACCOUNT_NAME");
        String accountKey = System.getenv("STORAGE_DATALAKE_ACCOUNT_KEY");
        String endpoint = System.getenv("STORAGE_DATALAKE_ENDPOINT");

        if (CoreUtils.isNullOrEmpty(accountName) || CoreUtils.isNullOrEmpty(accountKey)) {
            System.out.println("Environment variable STORAGE_DATALAKE_ENDPOINT must be set");
            System.exit(1);
        }

        dataLakeServiceClient = new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new StorageSharedKeyCredential(accountName, accountKey))
            .buildClient();

        dataLakeServiceAsyncClient = new DataLakeServiceClientBuilder()
            .endpoint(endpoint)
            .credential(new StorageSharedKeyCredential(accountName, accountKey))
            .buildAsyncClient();
    }
}
