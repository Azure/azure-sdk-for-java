// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.file.share.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.util.UUID;

public abstract class DirectoryTest<TOptions extends PerfStressOptions> extends ShareTest<TOptions> {
    protected final CloudFileDirectory cloudFileDirectory;

    public DirectoryTest(TOptions options) {
        super(options);
        // Setup the container clients
        try {
            String directoryName = "perfstress-directory-" + UUID.randomUUID().toString();
            cloudFileDirectory = cloudFileShare.getRootDirectoryReference().getDirectoryReference(directoryName);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }

    // NOTE: the pattern setup the parent first, then yourself.
    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(Mono.fromCallable(() -> {
                cloudFileDirectory.create();
                return 1; }))
            .then();
    }
}
