package com.azure.storage.blob.stress;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.HttpFaultInjectingHttpClient;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.storage.blob.stress.utils.TelemetryUtils;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.azure.storage.blob.stress.utils.TelemetryUtils.getTracer;

public abstract class BlobStorageStressScenarioBase<TOptions extends StorageStressOptions> extends PerfStressTest<TOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(BlobStorageStressScenarioBase.class);
    private static final String CONTAINER_NAME = "stress-" + UUID.randomUUID();
    private static final FaultInjectionProbabilities DEFAULT_FAULT_PROBABILITIES =
        new FaultInjectionProbabilities()
            .setNoResponseIndefinite(0.003D)
            .setNoResponseClose(0.004D)
            .setNoResponseAbort(0.003D)
            .setPartialResponseIndefinite(0.06)
            .setPartialResponseClose(0.06)
            .setPartialResponseAbort(0.06)
            .setPartialResponseFinishNormal(0.06);

    private final BlobServiceClient syncClient;
    private final BlobServiceAsyncClient asyncClient;
    private final BlobServiceAsyncClient asyncNoFaultClient;
    private final BlobContainerClient syncContainerClient;
    private final BlobContainerAsyncClient asyncContainerClient;
    private final BlobContainerAsyncClient asyncNoFaultContainerClient;
    private static final TelemetryUtils TELEMETRY_UTILS = new TelemetryUtils();

    public BlobStorageStressScenarioBase(TOptions options) {
        super(options);

        HttpLogOptions logOptions = new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS)
            .addAllowedHeaderName("x-ms-faultinjector-response-option")
            .addAllowedHeaderName("Content-Range")
            .addAllowedHeaderName("Accept-Ranges")
            .addAllowedHeaderName("x-ms-blob-content-md5")
            .addAllowedHeaderName("x-ms-error-code")
            .addAllowedHeaderName("x-ms-range");

        String connectionString = options.getConnectionString();
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(logOptions);
        asyncNoFaultClient = clientBuilder.buildAsyncClient();

        if (options.isFaultInjectionEnabled()) {
            clientBuilder.httpClient(new HttpFaultInjectingHttpClient(
                HttpClient.createDefault(), false, DEFAULT_FAULT_PROBABILITIES));
        }

        syncClient = clientBuilder.buildClient();
        asyncClient = clientBuilder.buildAsyncClient();
        asyncNoFaultContainerClient = asyncNoFaultClient.getBlobContainerAsyncClient(CONTAINER_NAME);
        syncContainerClient = syncClient.getBlobContainerClient(CONTAINER_NAME);
        asyncContainerClient = asyncClient.getBlobContainerAsyncClient(CONTAINER_NAME);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(asyncNoFaultContainerClient.createIfNotExists())
            .then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        LOGGER.atInfo()
            .addKeyValue("succeeded", TELEMETRY_UTILS.getSuccessfulRunCount())
            .addKeyValue("failed", TELEMETRY_UTILS.getFailedRunCount())
            .log("test ended");

        return super.globalCleanupAsync()
            .then(asyncNoFaultContainerClient.deleteIfExists())
            .then();
    }

    @SuppressWarnings("try")
    @Override
    public void run() {
        Context span = getTracer().start("run", Context.NONE);
        try (AutoCloseable s = getTracer().makeSpanCurrent(span)) {
            if (runInternal(span)) {
                TELEMETRY_UTILS.trackSuccess(span);
            } else {
                TELEMETRY_UTILS.trackMismatch(span);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("Timeout on blocking read") || e instanceof InterruptedException || e instanceof TimeoutException) {
                TELEMETRY_UTILS.trackCancellation(span);
            } else {
                TELEMETRY_UTILS.trackFailure(span, e);
            }
        }
    }

    @SuppressWarnings("try")
    @Override
    public Mono<Void> runAsync() {
        Context span = getTracer().start("runAsync", Context.NONE);
        try (AutoCloseable s = getTracer().makeSpanCurrent(span)) {
            return runInternalAsync(span)
                .doOnCancel(() -> TELEMETRY_UTILS.trackCancellation(span))
                .doOnError(e -> TELEMETRY_UTILS.trackFailure(span, e))
                .doOnNext(match -> {
                    if (match) {
                        TELEMETRY_UTILS.trackSuccess(span);
                    } else {
                        TELEMETRY_UTILS.trackMismatch(span);
                    }
                })
                .contextWrite(reactor.util.context.Context.of("TRACING_CONTEXT", span))
                .then()
                .onErrorResume(e -> Mono.empty());
        } catch (Exception e) {
            return Mono.empty();
        }
    }

    protected abstract boolean runInternal(Context context);
    protected abstract Mono<Boolean> runInternalAsync(Context context);

    protected BlobContainerClient getSyncContainerClient() {
        return syncContainerClient;
    }

    protected BlobContainerAsyncClient getAsyncContainerClient() {
        return asyncContainerClient;
    }

    protected BlobContainerAsyncClient getAsyncContainerClientNoFault() {
        return asyncNoFaultContainerClient;
    }
}
