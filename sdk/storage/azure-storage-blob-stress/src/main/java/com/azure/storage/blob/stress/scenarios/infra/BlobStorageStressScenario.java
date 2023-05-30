package com.azure.storage.blob.stress.scenarios.infra;

import com.azure.core.http.HttpClient;
import com.azure.core.util.Configuration;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.stress.StorageStressScenario;
import com.azure.storage.stress.StressScenarioBuilder;

public abstract class BlobStorageStressScenario<TBuilder extends StressScenarioBuilder> extends StorageStressScenario {
    private final BlobServiceClient syncClient;
    private final BlobServiceAsyncClient asyncClient;
    private final BlobServiceClient syncNoFaultClient;

    public BlobStorageStressScenario(TBuilder builder) {
        super(builder);
        String connectionString = Configuration.getGlobalConfiguration().get("STORAGE_CONNECTION_STRING");
        BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder().connectionString(connectionString);
        syncNoFaultClient = clientBuilder.buildClient();

        if (builder.getFaultInjectingClient() != null) {
            clientBuilder.httpClient(builder.getFaultInjectingClient());
        }
        syncClient = clientBuilder.buildClient();
        asyncClient = clientBuilder.buildAsyncClient();
    }

    public BlobServiceClient getSyncServiceClient() {
        return syncClient;
    }

    public BlobServiceAsyncClient getAsyncServiceClient() {
        return asyncClient;
    }

    public BlobServiceClient getSyncServiceClientNoFault() {
        return syncNoFaultClient;
    }
}
