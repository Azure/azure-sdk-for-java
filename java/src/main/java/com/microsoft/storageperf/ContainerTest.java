package com.microsoft.storageperf;

import com.azure.storage.blob.ContainerClient;
import com.microsoft.storageperf.core.PerfStressOptions;
import java.util.UUID;

public abstract class ContainerTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    protected static final String ContainerName = "perfstress-" + UUID.randomUUID().toString();

    protected final ContainerClient ContainerClient;

    public ContainerTest(TOptions options) {
        super(options);

        ContainerClient = BlobServiceClient.getContainerClient(ContainerName);
    }

    @Override
    public void GlobalSetup() {
        super.GlobalSetup();
        ContainerClient.create();
    }

    @Override
    public void GlobalCleanup() {
        ContainerClient.delete();
        super.GlobalCleanup();
    }
}
