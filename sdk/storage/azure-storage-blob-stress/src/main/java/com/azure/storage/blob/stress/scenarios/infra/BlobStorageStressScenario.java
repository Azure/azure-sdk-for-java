package com.azure.storage.blob.stress.scenarios.infra;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Configuration;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.stress.StorageStressScenario;
import com.azure.storage.stress.StressScenarioBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class BlobStorageStressScenario<TBuilder extends StressScenarioBuilder> extends StorageStressScenario {
    private final BlobServiceClient syncClient;
    private final BlobServiceAsyncClient asyncClient;
    private final BlobServiceClient syncNoFaultClient;

    public BlobStorageStressScenario(TBuilder builder) {
        super(builder);
        String connectionString = Configuration.getGlobalConfiguration().get("STORAGE_CONNECTION_STRING");

        HttpLogOptions logOptions = new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS)
            .addAllowedHeaderName("x-ms-faultinjector-response-option")
            .addAllowedHeaderName("Content-Range")
            .addAllowedHeaderName("Accept-Ranges")
            .addAllowedHeaderName("x-ms-blob-content-md5")
            .addAllowedHeaderName("x-ms-error-code")
            .addAllowedHeaderName("x-ms-range");

        BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(logOptions);
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
