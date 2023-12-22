// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.stress.utils.TelemetryHelper;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.HttpFaultInjectingHttpClient;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public abstract class BlobScenarioBase<TOptions extends StorageStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONTAINER_NAME = "stress-" + UUID.randomUUID();
    private final BlobServiceClient syncClient;
    private final BlobServiceAsyncClient asyncClient;
    private final BlobServiceAsyncClient asyncNoFaultClient;
    private final BlobContainerClient syncContainerClient;
    private final BlobContainerAsyncClient asyncContainerClient;
    private final BlobContainerAsyncClient asyncNoFaultContainerClient;
    private final TelemetryHelper telemetryHelper;

    public BlobScenarioBase(TOptions options, TelemetryHelper telemetryHelper) {
        super(options);

        this.telemetryHelper = telemetryHelper;
        String connectionString = options.getConnectionString();

        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");

        BlobServiceClientBuilder clientBuilder = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(getLogOptions());

        asyncNoFaultClient = clientBuilder.buildAsyncClient();

        if (options.isFaultInjectionEnabled()) {
            clientBuilder.httpClient(new HttpFaultInjectingHttpClient(
                HttpClient.createDefault(), false, getFaultProbabilities()));
        }

        syncClient = clientBuilder.buildClient();
        asyncClient = clientBuilder.buildAsyncClient();
        asyncNoFaultContainerClient = asyncNoFaultClient.getBlobContainerAsyncClient(CONTAINER_NAME);
        syncContainerClient = syncClient.getBlobContainerClient(CONTAINER_NAME);
        asyncContainerClient = asyncClient.getBlobContainerAsyncClient(CONTAINER_NAME);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        telemetryHelper.logStart(options);
        return super.globalSetupAsync()
            .then(asyncNoFaultContainerClient.createIfNotExists())
            .then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        telemetryHelper.logEnd();

        return asyncNoFaultContainerClient.deleteIfExists()
            .then(super.globalCleanupAsync());
    }

    @SuppressWarnings("try")
    @Override
    public void run() {
        Context span = telemetryHelper.getTracer().start("run", Context.NONE);
        try (AutoCloseable s = telemetryHelper.getTracer().makeSpanCurrent(span)) {
            if (runInternal(span)) {
                telemetryHelper.trackSuccess(span);
            } else {
                telemetryHelper.trackMismatch(span);
            }
        } catch (Throwable e) {
            if (e.getMessage().contains("Timeout on blocking read") || e instanceof InterruptedException || e instanceof TimeoutException) {
                telemetryHelper.trackCancellation(span);
            } else {
                telemetryHelper.trackFailure(span, e);
            }
        }
    }

    @SuppressWarnings("try")
    @Override
    public Mono<Void> runAsync() {
        Context span = telemetryHelper.getTracer().start("runAsync", Context.NONE);
        try (AutoCloseable s = telemetryHelper.getTracer().makeSpanCurrent(span)) {
            return runInternalAsync(span)
                .doOnCancel(() -> telemetryHelper.trackCancellation(span))
                .doOnError(e -> telemetryHelper.trackFailure(span, e))
                .doOnNext(match -> {
                    if (match) {
                        telemetryHelper.trackSuccess(span);
                    } else {
                        telemetryHelper.trackMismatch(span);
                    }
                })
                .contextWrite(reactor.util.context.Context.of("TRACING_CONTEXT", span))
                .then()
                .onErrorResume(e -> Mono.empty());
        } catch (Throwable e) {
            return Mono.empty();
        }
    }

    protected abstract boolean runInternal(Context context) throws Exception;
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
            .setPartialResponseFinishNormal(0.06);
    }
}
