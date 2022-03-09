// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
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
        String shareName = "perfstress-sharev11-" + UUID.randomUUID().toString();
        shareClient = shareServiceClient.getShareClient(shareName);
        shareAsyncClient = shareServiceAsyncClient.getShareAsyncClient(shareName);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(shareAsyncClient.create()).then();
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return shareAsyncClient.delete().then(super.cleanupAsync());
    }
}
