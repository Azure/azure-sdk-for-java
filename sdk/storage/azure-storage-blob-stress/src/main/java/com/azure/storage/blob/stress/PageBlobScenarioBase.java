// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.stress.TelemetryHelper;
import com.azure.storage.stress.FaultInjectingHttpPolicy;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public abstract class PageBlobScenarioBase<TOptions extends StorageStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONTAINER_NAME = "stress-" + UUID.randomUUID();
    protected final TelemetryHelper telemetryHelper = new TelemetryHelper(this.getClass());
    private final BlobServiceClient noFaultServiceClient;
    private final BlobContainerClient syncContainerClient;
    private final BlobContainerAsyncClient asyncContainerClient;
    private final BlobContainerAsyncClient asyncNoFaultContainerClient;
    private Instant startTime;

    public PageBlobScenarioBase(TOptions options) {
        super(options);

        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        String connectionString = options.getPageBlobConnectionString();
        String endpoint = options.getPageBlobEndpointString();

        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");

        BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder()
            //.connectionString(connectionString)
            .credential(defaultAzureCredential)
            .endpoint(endpoint)
            .httpLogOptions(getLogOptions());

        BlobServiceAsyncClient asyncNoFaultClient = clientBuilder.buildAsyncClient();
        noFaultServiceClient = clientBuilder.buildClient();

        if (options.isFaultInjectionEnabledForDownloads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(true, getFaultProbabilities(), false));
        } else if (options.isFaultInjectionEnabledForUploads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(true, getFaultProbabilities(), true));
        }

        BlobServiceClient syncClient = clientBuilder.buildClient();
        BlobServiceAsyncClient asyncClient = clientBuilder.buildAsyncClient();
        asyncNoFaultContainerClient = asyncNoFaultClient.getBlobContainerAsyncClient(CONTAINER_NAME);
        syncContainerClient = syncClient.getBlobContainerClient(CONTAINER_NAME);
        asyncContainerClient = asyncClient.getBlobContainerAsyncClient(CONTAINER_NAME);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        startTime = Instant.now();
        telemetryHelper.recordStart(options);
        return super.globalSetupAsync()
            .then(asyncNoFaultContainerClient.createIfNotExists())
            .then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        telemetryHelper.recordEnd(startTime);
        return asyncNoFaultContainerClient.deleteIfExists()
            .then(super.globalCleanupAsync());
    }

    @SuppressWarnings("try")
    @Override
    public void run() {
        telemetryHelper.instrumentRun(ctx -> runInternal(ctx));
    }

    @SuppressWarnings("try")
    @Override
    public Mono<Void> runAsync() {
        return telemetryHelper.instrumentRunAsync(ctx -> runInternalAsync(ctx))
            .onErrorResume(e -> Mono.empty());
    }

    protected abstract void runInternal(Context context) throws Exception;
    protected abstract Mono<Void> runInternalAsync(Context context);

    protected BlobContainerClient getSyncContainerClient() {
        return syncContainerClient;
    }

    protected BlobContainerAsyncClient getAsyncContainerClient() {
        return asyncContainerClient;
    }

    protected BlobContainerAsyncClient getAsyncContainerClientNoFault() {
        return asyncNoFaultContainerClient;
    }

    protected BlobContainerClient getSyncContainerClientNoFault() {
        return noFaultServiceClient.getBlobContainerClient(CONTAINER_NAME);
    }

    protected String generateBlobName() {
        return "blob-" + UUID.randomUUID();
    }

    private static HttpLogOptions getLogOptions() {
        return new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS)
            .addAllowedHeaderName("x-ms-faultinjector-response-option")
            .addAllowedHeaderName("Content-Range")
            .addAllowedHeaderName("Accept-Ranges")
            .addAllowedHeaderName("x-ms-blob-content-md5")
            .addAllowedHeaderName("x-ms-error-code")
            .addAllowedHeaderName("x-ms-range");
    }

    private static FaultInjectionProbabilities getFaultProbabilities() {
        return new FaultInjectionProbabilities()
            .setNoResponseIndefinite(0.003D)
            .setNoResponseClose(0.004D)
            .setNoResponseAbort(0.003D)
            .setPartialResponseIndefinite(0.06)
            .setPartialResponseClose(0.06)
            .setPartialResponseAbort(0.06)
            .setPartialResponseFinishNormal(0.06)
            .setNoRequestIndefinite(0.003D)
            .setNoRequestClose(0.004D)
            .setNoRequestAbort(0.003D)
            .setPartialRequestIndefinite(0.06)
            .setPartialRequestClose(0.06)
            .setPartialRequestAbort(0.06);
    }
}
