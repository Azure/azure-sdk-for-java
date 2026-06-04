// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.http.HttpClient;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
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
import com.azure.storage.stress.ContentMismatchException;
import com.azure.storage.stress.TelemetryHelper;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.FaultInjectingHttpPolicy;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.Exceptions;
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
            .httpClient(buildHttpClient(options.getSize()))
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
            .onErrorResume(error -> {
                // Log cleanup failure but don't fail the overall test
                LOGGER.atWarning()
                    .addKeyValue("error", error.getMessage())
                    .log("Container cleanup failed");

                return Mono.empty();
            })
            .then(super.globalCleanupAsync());
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
        return telemetryHelper.instrumentRunAsync(ctx ->
            runInternalAsync(ctx)
                .retryWhen(reactor.util.retry.Retry.max(3)
                    .filter(e -> !(Exceptions.unwrap(e)
                        instanceof ContentMismatchException)))
                .doOnError(e -> {
                    // Log the error for debugging but let legitimate failures propagate
                    LOGGER.atError()
                        .addKeyValue("error", e.getMessage())
                        .addKeyValue("errorType", e.getClass().getSimpleName())
                        .log("Test operation failed after retries");
                }));
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

    /**
     * Builds a Netty HTTP client whose {@code responseTimeout}, {@code readTimeout},
     * and {@code writeTimeout} are all sized to the payload being exercised by the
     * scenario.
     *
     * <p>Background: the Azure SDK Netty client defaults to a 60&nbsp;s
     * {@code responseTimeoutInSeconds}. Under fault injection, scenarios whose
     * single logical operation issues many HTTP requests (e.g. chunked 50&nbsp;MB
     * uploads/downloads) cross a per-op fault probability of &gt;50%, so the
     * median operation time becomes dominated by the 60&nbsp;s timeout firing
     * rather than the real payload-transfer time. See
     * {@code stress-logs-ibrandes/perfRuntimeCatch-uncompiled-storage-stress-storage-blob/operation-duration-analysis.md}
     * for the full analysis.</p>
     *
     * <p>Note: {@code responseTimeout} alone is insufficient for download faults,
     * because download faults arrive after the response status (200 OK) is already
     * received and instead truncate or hang the response body. That triggers
     * {@code readTimeout} (idle interval between body reads), not
     * {@code responseTimeout}. The 2026-06-03 16:36 UTC stress run confirmed
     * this empirically: upload-fault scenarios honored the new short timeouts,
     * but download-fault scenarios still clustered at the default 60&nbsp;s. We
     * therefore set all three timeouts to the same per-tier value. Streaming
     * downloads at network speed have continuous read activity, so the short
     * idle-{@code readTimeout} only fires when the body stops flowing
     * (i.e. when a fault actually occurs).</p>
     *
     * <p>The tier table below is taken from the analysis doc's "Recommended
     * {@code responseTimeoutInSeconds} per tier" section, validated against the
     * 2026-06-02 22:16 UTC no-fault baseline (see
     * {@code stress-logs-ibrandes/nofault-baseline.md}). Headroom of at least
     * ~2&times; the worst-case observed median is preserved.</p>
     *
     * <table>
     *   <caption>Per-tier I/O timeouts</caption>
     *   <tr><th>Payload size</th><th>Real median</th><th>Suggested timeout</th></tr>
     *   <tr><td>&le; 1&nbsp;MB</td><td>22&ndash;80&nbsp;ms</td><td>5&nbsp;s</td></tr>
     *   <tr><td>&le; 4&nbsp;MB</td><td>55&nbsp;ms (per page)</td><td>10&nbsp;s</td></tr>
     *   <tr><td>&le; 25&nbsp;MB</td><td>308&ndash;361&nbsp;ms</td><td>10&nbsp;s</td></tr>
     *   <tr><td>&le; 50&nbsp;MB</td><td>0.6&ndash;5&nbsp;s</td><td>30&nbsp;s</td></tr>
     *   <tr><td>&gt; 50&nbsp;MB</td><td>n/a</td><td>60&nbsp;s (SDK default)</td></tr>
     * </table>
     */
    protected static HttpClient buildHttpClient(long payloadSizeBytes) {
        Duration timeout = suggestedResponseTimeout(payloadSizeBytes);
        return new NettyAsyncHttpClientBuilder()
            .responseTimeout(timeout)
            .readTimeout(timeout)
            .writeTimeout(timeout)
            .build();
    }

    static Duration suggestedResponseTimeout(long payloadSizeBytes) {
        final long oneMb = 1L * 1024 * 1024;
        if (payloadSizeBytes <= oneMb) {
            return Duration.ofSeconds(5);
        } else if (payloadSizeBytes <= 25L * oneMb) {
            // Covers 4 MB (uploadPages per-page) and 25 MB single-shot block uploads.
            return Duration.ofSeconds(10);
        } else if (payloadSizeBytes <= 50L * oneMb) {
            // 50 MB chunked upload/download paths can legitimately take 5-15 s per op.
            return Duration.ofSeconds(30);
        }
        // Fall back to the SDK default for anything larger.
        return Duration.ofSeconds(60);
    }
}
