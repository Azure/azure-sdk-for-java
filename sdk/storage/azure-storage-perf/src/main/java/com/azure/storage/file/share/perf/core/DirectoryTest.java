// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.share.ShareAsyncClient;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryAsyncClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class DirectoryTest<TOptions extends PerfStressOptions> extends ShareTest<TOptions> {
    private static final String DIRECTORY_NAME = "perfstress-directoryv11-" + UUID.randomUUID().toString();

    protected final ShareDirectoryClient shareDirectoryClient;
    protected final ShareDirectoryAsyncClient shareDirectoryAsyncClient;

    public DirectoryTest(TOptions options) {
        super(options);
        // Setup the container clients
        shareDirectoryClient = shareClient.getDirectoryClient(DIRECTORY_NAME);
        shareDirectoryAsyncClient = shareAsyncClient.getDirectoryClient(DIRECTORY_NAME);
    }

    // NOTE: the pattern setup the parent first, then yourself.
    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(shareDirectoryAsyncClient.create().then());
    }

    // NOTE: the pattern, cleanup yourself, then the parent.
    @Override
    public Mono<Void> globalCleanupAsync() {
        return shareDirectoryAsyncClient.delete().then(super.globalCleanupAsync());
    }
}
