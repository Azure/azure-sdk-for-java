// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.share.ShareDirectoryAsyncClient;
import com.azure.storage.file.share.ShareDirectoryClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

public abstract class DirectoryTest<TOptions extends PerfStressOptions> extends ShareTest<TOptions> {
    protected final ShareDirectoryClient shareDirectoryClient;
    protected final ShareDirectoryAsyncClient shareDirectoryAsyncClient;

    public DirectoryTest(TOptions options) {
        super(options);
        // Setup the container clients
        String directroyName = "perfstressdirectoryv11" + UUID.randomUUID().toString();
        shareDirectoryClient = shareClient.getDirectoryClient(directroyName);
        shareDirectoryAsyncClient = shareAsyncClient.getDirectoryClient(directroyName);
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(shareDirectoryAsyncClient.create()).then();
    }
}
