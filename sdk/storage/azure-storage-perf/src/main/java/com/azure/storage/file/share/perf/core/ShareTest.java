// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class ShareTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    private static final String SHARE_NAME = "perfstress-sharev11-" + UUID.randomUUID().toString();

    protected final ShareClient shareClient;
    protected final ShareAsyncClient shareAsyncClient;

    public ShareTest(TOptions options) {
        super(options);
        // Setup the container clients
        shareClient = shareServiceClient.getShareClient(SHARE_NAME);
        shareAsyncClient = shareServiceAsyncClient.getShareAsyncClient(SHARE_NAME);
    }

    // NOTE: the pattern setup the parent first, then yourself.
    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(shareAsyncClient.create().then());
    }

    // NOTE: the pattern, cleanup yourself, then the parent.
    @Override
    public Mono<Void> globalCleanupAsync() {
        return shareAsyncClient.delete().then(super.globalCleanupAsync());
    }
}
