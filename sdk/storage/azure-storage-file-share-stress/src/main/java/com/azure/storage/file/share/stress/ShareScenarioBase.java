// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.stress;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryAsyncClient;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.azure.storage.file.share.models.ShareTokenIntent;
import com.azure.storage.stress.TelemetryHelper;
import com.azure.storage.stress.FaultInjectingHttpPolicy;
import com.azure.storage.stress.FaultInjectionProbabilities;
import com.azure.storage.stress.StorageStressOptions;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public abstract class ShareScenarioBase<TOptions extends StorageStressOptions> extends PerfStressTest<TOptions> {
    private static final String SHARE_NAME = "stress-" + UUID.randomUUID();
    private static final ClientLogger LOGGER = new ClientLogger(ShareScenarioBase.class);
    protected final TelemetryHelper telemetryHelper = new TelemetryHelper(this.getClass());
    private final ShareClient syncShareClient;
    private final ShareAsyncClient asyncShareClient;
    private final ShareAsyncClient asyncNoFaultShareClient;
    private final ShareClient syncNoFaultShareClient;
    private Instant startTime;

    public ShareScenarioBase(TOptions options) {
        super(options);

        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();
        String endpoint = options.getEndpointString();

        ShareServiceClientBuilder clientBuilder = new ShareServiceClientBuilder()
            .credential(defaultAzureCredential)
            .shareTokenIntent(ShareTokenIntent.BACKUP)
            .endpoint(endpoint)
            .httpLogOptions(getLogOptions());

        ShareServiceAsyncClient asyncNoFaultClient = clientBuilder.buildAsyncClient();
        ShareServiceClient syncNoFaultClient = clientBuilder.buildClient();

        if (options.isFaultInjectionEnabledForDownloads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(true, getFaultProbabilities(), false));
        } else if (options.isFaultInjectionEnabledForUploads()) {
            clientBuilder.addPolicy(new FaultInjectingHttpPolicy(true, getFaultProbabilities(), true));
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
        return cleanupShareWithRetry()
            .then(super.globalCleanupAsync())
            .onErrorResume(error -> {
                // Log cleanup failure but don't fail the overall test
                LOGGER.atWarning()
                    .addKeyValue("error", error.getMessage())
                    .log("Share cleanup failed");
                return Mono.empty();
            })
            .then(super.globalCleanupAsync());
    }

    /**
     * Enhanced cleanup with timeout and retry logic to ensure shares are properly destroyed.
     */
    private Mono<Void> cleanupShareWithRetry() {
        return asyncNoFaultShareClient.deleteIfExists()
            .then()  // Convert Mono<Boolean> to Mono<Void>
            .timeout(java.time.Duration.ofSeconds(30))
            .retry(3)
            .onErrorResume(error -> {
                // If share deletion fails, try to delete all files first then retry
                return deleteAllFilesInShare()
                    .then(asyncNoFaultShareClient.deleteIfExists())
                    .then()  // Convert Mono<Boolean> to Mono<Void>
                    .timeout(java.time.Duration.ofSeconds(30))
                    .onErrorResume(finalError -> {
                        // Log the error but don't fail the test
                        LOGGER.atWarning()
                            .addKeyValue("error", finalError.getMessage())
                            .log("Final share cleanup failed after retries");
                        return Mono.empty();
                    });
            });
    }

    /**
     * Delete all files in the share to help with cleanup.
     */
    private Mono<Void> deleteAllFilesInShare() {
        return deleteDirectoryContentsRecursively(asyncNoFaultShareClient.getDirectoryClient(""))
            .timeout(java.time.Duration.ofSeconds(60))
            .onErrorResume(error -> {
                // Log but continue - some files might have been deleted
                LOGGER.atWarning()
                    .addKeyValue("error", error.getMessage())
                    .log("File cleanup partially failed");
                return Mono.empty();
            });
    }

    /**
     * Recursively delete all contents of a directory (files first, then subdirectories).
     */
    private Mono<Void> deleteDirectoryContentsRecursively(
            ShareDirectoryAsyncClient directoryClient) {
        return directoryClient.listFilesAndDirectories()
            // Use concatMap to ensure we process each file/directory sequentially, which is important for correct deletion order
            .concatMap(fileRef -> {
                if (fileRef.isDirectory()) {
                    ShareDirectoryAsyncClient subDirClient =
                        directoryClient.getSubdirectoryClient(fileRef.getName());
                    // First delete all contents recursively, then delete the directory itself
                    return deleteDirectoryContentsRecursively(subDirClient)
                        .then(subDirClient.delete());
                } else {
                    return directoryClient.getFileClient(fileRef.getName()).delete();
                }
            })
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
            .onErrorMap(e -> {
                // Log the error for debugging but let legitimate failures propagate
                System.err.println("Share test operation failed after retries: " + e.getMessage());
                return e;
            });
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
