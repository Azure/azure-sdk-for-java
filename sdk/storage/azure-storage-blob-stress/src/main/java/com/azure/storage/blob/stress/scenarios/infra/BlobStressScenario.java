package com.azure.storage.blob.stress.scenarios.infra;

import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.stress.builders.BlobScenarioBuilder;
import com.azure.storage.stress.RandomInputStream;

import java.util.UUID;

public abstract class BlobStressScenario<TBuilder extends BlobScenarioBuilder> extends BlobContainerStressScenario<TBuilder> {
    private final BlobClient syncClient;
    private final BlobAsyncClient asyncClient;
    private final BlobClient syncNoFaultClient;

    private final boolean initializeBlob;
    private final long blobSize;
    private final String blobPrefix;

    public BlobStressScenario(TBuilder builder, boolean initializeBlob) {
        super(builder);
        this.initializeBlob = initializeBlob;
        this.blobSize = builder.getBlobSize();
        this.blobPrefix = builder.getBlobPrefix();

        String blobName = makeBlobName();
        syncNoFaultClient = getSyncContainerClientNoFault().getBlobClient(blobName);
        syncClient = getSyncContainerClient().getBlobClient(blobName);
        asyncClient = getAsyncContainerClient().getBlobAsyncClient(blobName);
    }

    public BlobClient getSyncBlobClient() {
        return syncClient;
    }

    public BlobAsyncClient getAsyncBlobClient() {
        return asyncClient;
    }

    public BlobClient getSyncBlobClientNoFault() {
        return syncNoFaultClient;
    }

    public long getBlobSize() {
        return blobSize;
    }

    @Override
    public void setup() {
        super.setup();
        if (initializeBlob) {
            setupBlob();
        }
    }

    protected void setupBlob() {
        syncNoFaultClient.upload(new RandomInputStream(blobSize));
    }

    public String makeBlobName()
    {
        return blobPrefix;
    }
}
