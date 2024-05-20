// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.stress;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.stress.TelemetryHelper;
import com.azure.storage.stress.FaultInjectingHttpPolicy;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public abstract class ShareScenarioBase<TOptions extends StorageStressOptions> extends PerfStressTest<TOptions> {
    private static final String SHARE_NAME = "stress-" + UUID.randomUUID();
    protected final TelemetryHelper telemetryHelper = new TelemetryHelper(this.getClass());
    private final ShareClient syncShareClient;
    private final ShareAsyncClient asyncShareClient;
    private final ShareAsyncClient asyncNoFaultShareClient;
    private final ShareClient syncNoFaultShareClient;
    private Instant startTime;

    public ShareScenarioBase(TOptions options) {
        super(options);

        String connectionString = options.getConnectionString();

        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");

        ShareServiceClientBuilder clientBuilder = new ShareServiceClientBuilder()
            .connectionString(connectionString)
            .httpLogOptions(getLogOptions());

        ShareServiceAsyncClient asyncNoFaultClient = clientBuilder.buildAsyncClient();
        ShareServiceClient syncNoFaultClient = clientBuilder.buildClient();

        if (options.isFaultInjectionEnabledForDownloads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(false, getFaultProbabilities(), false));
        } else if (options.isFaultInjectionEnabledForUploads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(false, getFaultProbabilities(), true));
        }

        ShareServiceClient syncClient = clientBuilder.buildClient();
        ShareServiceAsyncClient asyncClient = clientBuilder.buildAsyncClient();
        asyncNoFaultShareClient = asyncNoFaultClient.getShareAsyncClient(SHARE_NAME);
        syncNoFaultShareClient = syncNoFaultClient.getShareClient(SHARE_NAME);
        syncShareClient = syncClient.getShareClient(SHARE_NAME);
        asyncShareClient = asyncClient.getShareAsyncClient(SHARE_NAME);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        startTime = Instant.now();
        telemetryHelper.recordStart(options);
        return super.globalSetupAsync()
            .then(asyncNoFaultShareClient.createIfNotExists())
            .then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        telemetryHelper.recordEnd(startTime);
        return asyncNoFaultShareClient.deleteIfExists()
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

    protected ShareClient getSyncShareClient() {
        return syncShareClient;
    }

    protected ShareAsyncClient getAsyncShareClient() {
        return asyncShareClient;
    }

    protected ShareAsyncClient getAsyncShareClientNoFault() {
        return asyncNoFaultShareClient;
    }

    protected ShareClient getSyncShareClientNoFault() {
        return syncNoFaultShareClient;
    }

    protected String generateFileName() {
        return "share-" + UUID.randomUUID();
    }

    private static HttpLogOptions getLogOptions() {
        return new HttpLogOptions()
            .setLogLevel(HttpLogDetailLevel.HEADERS)
            .addAllowedHeaderName("x-ms-faultinjector-response-option")
            .addAllowedHeaderName("Content-Range")
            .addAllowedHeaderName("Accept-Ranges")
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
