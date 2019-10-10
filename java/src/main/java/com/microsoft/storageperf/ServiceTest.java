package com.microsoft.storageperf;

import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import com.microsoft.storageperf.core.PerfStressOptions;
import com.microsoft.storageperf.core.PerfStressTest;

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

        BlobServiceClientBuilder builder = new BlobServiceClientBuilder().connectionString(connectionString);

        BlobServiceClient = builder.buildClient();
        BlobServiceAsyncClient = builder.buildAsyncClient();
    }
}
