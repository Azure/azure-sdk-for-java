// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.stress.TelemetryHelper;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.FaultInjectingHttpPolicy;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.time.Duration;

public abstract class BlobScenarioBase<TOptions extends StorageStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONTAINER_NAME = "stress-" + UUID.randomUUID();
    private static final ClientLogger LOGGER = new ClientLogger(BlobScenarioBase.class);
    protected final TelemetryHelper telemetryHelper = new TelemetryHelper(this.getClass());
    private final BlobContainerClient syncContainerClient;
    private final BlobContainerAsyncClient asyncContainerClient;
    private final BlobContainerAsyncClient asyncNoFaultContainerClient;
    private final BlobContainerClient syncNoFaultContainerClient;
    private Instant startTime;

    public BlobScenarioBase(TOptions options) {
        super(options);

        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();
        String endpoint = options.getEndpointString();

        BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder()
            .credential(defaultAzureCredential)
            .endpoint(endpoint)
            .httpLogOptions(getLogOptions());

        BlobServiceAsyncClient asyncNoFaultClient = clientBuilder.buildAsyncClient();
        BlobServiceClient syncNoFaultClient = clientBuilder.buildClient();

        if (options.isFaultInjectionEnabledForDownloads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(true, getFaultProbabilities(), false));
        } else if (options.isFaultInjectionEnabledForUploads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(true, getFaultProbabilities(), true));
        }

        BlobServiceClient syncClient = clientBuilder.buildClient();
        BlobServiceAsyncClient asyncClient = clientBuilder.buildAsyncClient();
        asyncNoFaultContainerClient = asyncNoFaultClient.getBlobContainerAsyncClient(CONTAINER_NAME);
        syncNoFaultContainerClient = syncNoFaultClient.getBlobContainerClient(CONTAINER_NAME);
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
        return cleanupContainerWithRetry()
            .then(super.globalCleanupAsync())
            .onErrorResume(error -> {
                // Log cleanup failure but don't fail the overall test
                LOGGER.atWarning()
                    .addKeyValue("error", error.getMessage())
                    .log("Container cleanup failed");
                return super.globalCleanupAsync();
            });
    }

    private static final int DELETE_TIMEOUT_SECONDS = 30;
    private static final int BLOB_CLEANUP_TIMEOUT_SECONDS = 60;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private Mono<Void> cleanupContainerWithRetry() {
        return tryDeleteContainer()
            .onErrorResume(error -> fallbackCleanup());
    }

    private Mono<Void> tryDeleteContainer() {
        return asyncNoFaultContainerClient.deleteIfExists()
            .then()
            .timeout(Duration.ofSeconds(DELETE_TIMEOUT_SECONDS))
            .retry(MAX_RETRY_ATTEMPTS);
    }

    private Mono<Void> fallbackCleanup() {
        return deleteAllBlobsInContainer()
            .then(tryDeleteContainerOnce())
            .onErrorResume(this::logCleanupFailure);
    }

    private Mono<Void> tryDeleteContainerOnce() {
        return asyncNoFaultContainerClient.deleteIfExists()
            .then()
            .timeout(Duration.ofSeconds(DELETE_TIMEOUT_SECONDS));
    }

    private Mono<Void> logCleanupFailure(Throwable error) {
        LOGGER.atWarning()
            .addKeyValue("error", error.getMessage())
            .log("Final container cleanup failed after retries");
        return Mono.empty();
    }

    /**
     * Deletes all blobs in the container sequentially to avoid throttling.
     */
    private Mono<Void> deleteAllBlobsInContainer() {
        return asyncNoFaultContainerClient.listBlobs()
            .concatMap(this::deleteBlobQuietly)
            .then()
            .timeout(Duration.ofSeconds(BLOB_CLEANUP_TIMEOUT_SECONDS))
            .onErrorResume(error -> {
                LOGGER.atWarning()
                    .addKeyValue("error", error.getMessage())
                    .log("Blob cleanup partially failed");
                return Mono.empty();
            });
    }

    private Mono<Void> deleteBlobQuietly(com.azure.storage.blob.models.BlobItem blobItem) {
        return asyncNoFaultContainerClient.getBlobAsyncClient(blobItem.getName())
            .deleteIfExists()
            .onErrorResume(e -> Mono.empty())
            .then();
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
            .retryWhen(reactor.util.retry.Retry.max(3)
                .filter(e -> !(reactor.core.Exceptions.unwrap(e) instanceof com.azure.storage.stress.ContentMismatchException)))
            .doOnError(e -> {
                // Log the error for debugging but let legitimate failures propagate
                LOGGER.atError()
                    .addKeyValue("error", e.getMessage())
                    .addKeyValue("errorType", e.getClass().getSimpleName())
                    .log("Test operation failed after retries");
            });
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
        return syncNoFaultContainerClient;
    }


    protected String generateBlobName() {
        return "blob-" + UUID.randomUUID();
    }

    protected static HttpLogOptions getLogOptions() {
        return new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS)
            .addAllowedHeaderName("x-ms-faultinjector-response-option")
            .addAllowedHeaderName("Content-Range")
            .addAllowedHeaderName("Accept-Ranges")
            .addAllowedHeaderName("x-ms-blob-content-md5")
            .addAllowedHeaderName("x-ms-error-code")
            .addAllowedHeaderName("x-ms-range");
    }

    protected static FaultInjectionProbabilities getFaultProbabilities() {
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
