// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.file.share.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFileShare;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.util.UUID;

public abstract class ShareTest<TOptions extends PerfStressOptions> extends ServiceTest<TOptions> {
    protected final CloudFileShare cloudFileShare;

    public ShareTest(TOptions options) {
        super(options);
        // Setup the container clients
        try {
            String shareName = "perfstress-share-" + UUID.randomUUID().toString();
            cloudFileShare = cloudFileClient.getShareReference(shareName);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }

    // NOTE: the pattern setup the parent first, then yourself.
    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(Mono.fromCallable(() -> {
                cloudFileShare.create();
                return 1;
            })).then();
    }

    // NOTE: the pattern, cleanup yourself, then the parent.
    @Override
    public Mono<Void> globalCleanupAsync() {
        return Mono.fromCallable(() -> {
            cloudFileShare.delete();
            return 1;
        }).then(super.globalCleanupAsync());
    }
}
