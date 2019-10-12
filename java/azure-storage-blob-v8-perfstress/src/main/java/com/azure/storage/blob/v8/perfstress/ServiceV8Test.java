package com.azure.storage.blob.v8.perfstress;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;

import com.azure.perfstress.PerfStressOptions;
import com.azure.perfstress.PerfStressTest;

public abstract class ServiceV8Test<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {

    protected final CloudBlobClient CloudBlobClient;

    public ServiceV8Test(TOptions options) {
        super(options);

        String connectionString = System.getenv("STORAGE_CONNECTION_STRING");

        if (connectionString == null || connectionString.isEmpty()) {
            System.out.println("Environment variable STORAGE_CONNECTION_STRING must be set");
            System.exit(1);
        }

        try {
            CloudBlobClient = CloudStorageAccount.parse(connectionString).createCloudBlobClient();
        } catch (InvalidKeyException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
