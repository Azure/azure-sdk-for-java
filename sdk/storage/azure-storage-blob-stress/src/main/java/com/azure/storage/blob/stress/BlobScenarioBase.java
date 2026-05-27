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
        // We previously wrapped runInternalAsync with an unconditional `.retryWhen(Retry.max(3))`.
        // That mask hid a real liveness bug in the SDK upload pipeline: when an HTTP fault
        // (especially the request-side `pq*` indefinite variants from FaultInjectingHttpPolicy)
        // causes one parallel rail's Mono to stop making progress, the unconditional retry burns
        // 3 attempts of 60s Netty response timeouts on top of an already-stuck pipeline and the
        // hang propagates outward. With three parallel rails, a single such stall freezes the
        // whole `flatMap(runTestAsync, 1)` for the remainder of the configured --duration, which
        // is exactly what the six "Failed" large-payload pods exhibited on 2026-05-26 (see
        // sdk/storage/BUG-blob-upload-hang-on-fault.md).
        //
        // Replace the retry with an outer per-operation timeout. Any single iteration that
        // doesn't complete within `OPERATION_TIMEOUT` fails fast as a TimeoutException, which:
        //   1. lets the rail recycle and start the next iteration,
        //   2. surfaces a real failure on the dashboard instead of a frozen progress counter,
        //   3. produces evidence (the TimeoutException) for SDK bug triage, and
        //   4. still tolerates the natural per-op latency under fault injection (~6.3% of HTTP
        //      calls are configured-indefinite; effective avg op latency is a few seconds for
        //      small payloads and tens of seconds for large multi-request operations, well under
        //      the 2-minute cap below).
        // If a scenario legitimately needs longer than the default per op, override
        // `getOperationTimeout()` in the scenario class rather than blanket-raising it here.
        //
        // Tuning note (2026-05-26): the previous default was 2 minutes, which was too coarse.
        // Under fault injection the dominant wedge is the `pq*` (request-side, indefinite)
        // variant. Netty's `responseTimeout` already fires after 60s, so any operation still
        // unresponsive ~30s after that is a real liveness bug and we want to fail fast and
        // recycle the rail. Empirically this 4-6x's small-scenario throughput (small-blob ops
        // typically complete in <2s; even fault-injected ops are bounded by the 60s Netty
        // response timeout) while still leaving plenty of headroom for multi-block large
        // operations via per-scenario overrides.
        //
        // CRITICAL: the outer `.onErrorResume(e -> Mono.empty())` converts a failed iteration
        // into a successful "this iteration is done" signal *after* `instrumentRunAsync` has
        // already fired its `doOnError` side-effect (which calls `trackFailure` and increments
        // the `failed_runs` metric). Without this, a TimeoutException -- or any other error --
        // propagates out of `runTestAsync()` into `flatMap(runTestAsync, 1)` in
        // ApiPerfTestBase.runAllAsync and terminates the entire Flux, cancelling all parallel
        // rails and aborting the test loop. The previous `.retryWhen(Retry.max(3))` happened to
        // mask this because most ops succeeded within retries, so the propagation path was
        // rarely exercised; with the retry gone, every long-tail op would otherwise kill the
        // whole job after the first 2-minute timeout. The error has already been logged and
        // counted as a failure by the time `onErrorResume` runs, so no information is lost.
        return telemetryHelper.instrumentRunAsync(ctx ->
            runInternalAsync(ctx)
                .timeout(getOperationTimeout())
                .doOnError(e -> {
                    // Log the error for debugging but let legitimate failures propagate
                    LOGGER.atError()
                        .addKeyValue("error", e.getMessage())
                        .addKeyValue("errorType", e.getClass().getSimpleName())
                        .log("Test operation failed");
                }))
            .onErrorResume(e -> Mono.empty());
    }

    /**
     * Default per-operation timeout for stress scenarios. Small-payload scenarios complete
     * well under 2s in steady state and are bounded by Netty's 60s `responseTimeout` under
     * fault injection, so 30s catches genuine liveness wedges without throwing away healthy
     * long-tail ops. Multi-block large-payload variants need more time (a single op can be
     * many sequential block uploads), so we scale the default up based on `options.getSize()`:
     * one extra minute per 16 MiB above 1 MiB, capped at 5 minutes. Scenarios that need an
     * even larger envelope can override this method:
     *
     * <pre>{@code
     * @Override
     * protected Duration getOperationTimeout() { return Duration.ofMinutes(10); }
     * }</pre>
     */
    protected Duration getOperationTimeout() {
        long sizeBytes = options.getSize();
        // Baseline 30s, +60s per 16 MiB beyond the first 1 MiB. Capped at 5 minutes so a
        // genuinely-wedged rail still recovers promptly even for the largest configured payloads.
        long extraSeconds = Math.max(0, (sizeBytes - SMALL_PAYLOAD_THRESHOLD_BYTES) / BYTES_PER_EXTRA_MINUTE) * 60;
        long totalSeconds = Math.min(BASE_TIMEOUT_SECONDS + extraSeconds, MAX_TIMEOUT_SECONDS);
        return Duration.ofSeconds(totalSeconds);
    }

    private static final long SMALL_PAYLOAD_THRESHOLD_BYTES = (long) 1024 * 1024;       // 1 MiB
    private static final long BYTES_PER_EXTRA_MINUTE       = 16L * 1024 * 1024;       // 16 MiB
    private static final long BASE_TIMEOUT_SECONDS         = 30;
    private static final long MAX_TIMEOUT_SECONDS          = 5 * 60;

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
