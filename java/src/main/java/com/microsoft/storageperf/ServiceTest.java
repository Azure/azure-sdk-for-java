package com.microsoft.storageperf;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import com.microsoft.storageperf.core.PerfStressOptions;
import com.microsoft.storageperf.core.PerfStressTest;

public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final BlobServiceClient BlobServiceClient;

    public ServiceTest(TOptions options) {
        super(options);

        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");

        if (connectionString == null || connectionString.isEmpty()) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        BlobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
    }
}
