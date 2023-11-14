package com.azure.storage.blob.stress.scenarios.infra;

import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.stress.StressScenarioBuilder;

import java.util.UUID;

public abstract class BlobContainerStressScenario<TOptions extends StressScenarioBuilder> extends BlobStorageStressScenario<TOptions> {
    private static final String containerName = "stress-" + UUID.randomUUID();
    private final BlobContainerClient syncClient;
    private final BlobContainerAsyncClient asyncClient;
    private final BlobContainerClient syncNoFaultClient;

    public BlobContainerStressScenario(TOptions options) {
        super(options);
        syncNoFaultClient = getSyncServiceClientNoFault().getBlobContainerClient(containerName);
        syncClient = getSyncServiceClient().getBlobContainerClient(containerName);
        asyncClient = getAsyncServiceClient().getBlobContainerAsyncClient(containerName);
    }

    @Override
    public void setup() {
        syncNoFaultClient.createIfNotExists();
    }

    @Override
    public void teardown() {
        syncNoFaultClient.deleteIfExists();
    }

    public BlobContainerClient getSyncContainerClient() {
        return syncClient;
    }

    public BlobContainerAsyncClient getAsyncContainerClient() {
        return asyncClient;
    }

    public BlobContainerClient getSyncContainerClientNoFault() {
        return syncNoFaultClient;
    }
}
