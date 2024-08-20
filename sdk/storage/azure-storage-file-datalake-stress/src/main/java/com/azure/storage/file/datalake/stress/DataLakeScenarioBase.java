// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.stress;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.file.datalake.DataLakeFileSystemAsyncClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeServiceAsyncClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;
import com.azure.storage.stress.FaultInjectingHttpPolicy;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.StorageStressOptions;
import com.azure.storage.stress.TelemetryHelper;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public abstract class DataLakeScenarioBase<TOptions extends StorageStressOptions> extends PerfStressTest<TOptions> {
    private static final String FILE_SYSTEM_NAME = "stress-" + UUID.randomUUID();
    protected final TelemetryHelper telemetryHelper = new TelemetryHelper(this.getClass());
    private final DataLakeFileSystemClient syncFileSystemClient;
    private final DataLakeFileSystemAsyncClient asyncFileSystemClient;
    private final DataLakeFileSystemAsyncClient asyncNoFaultFileSystemClient;
    private final DataLakeFileSystemClient syncNoFaultFileSystemClient;
    private Instant startTime;

    public DataLakeScenarioBase(TOptions options) {
        super(options);

        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();
        String endpoint = options.getEndpointString();

        DataLakeServiceClientBuilder clientBuilder = new DataLakeServiceClientBuilder()
            .credential(defaultAzureCredential)
            .endpoint(endpoint)
            .httpLogOptions(getLogOptions());

        DataLakeServiceAsyncClient asyncNoFaultClient = clientBuilder.buildAsyncClient();
        DataLakeServiceClient syncNoFaultClient = clientBuilder.buildClient();

        if (options.isFaultInjectionEnabledForDownloads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(true, getFaultProbabilities(), false));
        } else if (options.isFaultInjectionEnabledForUploads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(true, getFaultProbabilities(), true));
        }

        DataLakeServiceClient syncClient = clientBuilder.buildClient();
        DataLakeServiceAsyncClient asyncClient = clientBuilder.buildAsyncClient();
        asyncNoFaultFileSystemClient = asyncNoFaultClient.getFileSystemAsyncClient(FILE_SYSTEM_NAME);
        syncNoFaultFileSystemClient = syncNoFaultClient.getFileSystemClient(FILE_SYSTEM_NAME);
        syncFileSystemClient = syncClient.getFileSystemClient(FILE_SYSTEM_NAME);
        asyncFileSystemClient = asyncClient.getFileSystemAsyncClient(FILE_SYSTEM_NAME);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        startTime = Instant.now();
        telemetryHelper.recordStart(options);
        return super.globalSetupAsync()
            .then(asyncNoFaultFileSystemClient.createIfNotExists())
            .then();
    }

    @Override
    public void globalSetup() {
        startTime = Instant.now();
        telemetryHelper.recordStart(options);
        super.globalSetup();
        syncNoFaultFileSystemClient.createIfNotExists();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        telemetryHelper.recordEnd(startTime);
        return asyncNoFaultFileSystemClient.deleteIfExists()
            .then(super.globalCleanupAsync());
    }

    @Override
    public void globalCleanup() {
        telemetryHelper.recordEnd(startTime);
        syncNoFaultFileSystemClient.deleteIfExists();
        super.globalCleanup();
    }

    @SuppressWarnings("try")
    @Override
    public void run() {
        telemetryHelper.instrumentRun(this::runInternal);
    }

    @SuppressWarnings("try")
    @Override
    public Mono<Void> runAsync() {
        return telemetryHelper.instrumentRunAsync(this::runInternalAsync)
            .onErrorResume(e -> Mono.empty());
    }

    protected abstract void runInternal(Context context) throws Exception;
    protected abstract Mono<Void> runInternalAsync(Context context);

    protected DataLakeFileSystemClient getSyncFileSystemClient() {
        return syncFileSystemClient;
    }

    protected DataLakeFileSystemAsyncClient getAsyncFileSystemClient() {
        return asyncFileSystemClient;
    }

    protected DataLakeFileSystemClient getSyncFileSystemClientNoFault() {
        return syncNoFaultFileSystemClient;
    }

    protected DataLakeFileSystemAsyncClient getAsyncFileSystemClientNoFault() {
        return asyncNoFaultFileSystemClient;
    }

    protected String generateFileName() {
        return "datalake-" + UUID.randomUUID();
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
