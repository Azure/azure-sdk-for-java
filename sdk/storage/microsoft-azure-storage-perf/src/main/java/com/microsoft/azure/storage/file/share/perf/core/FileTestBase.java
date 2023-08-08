// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.file.share.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.util.UUID;

public abstract class FileTestBase<TOptions extends PerfStressOptions> extends DirectoryTest<TOptions> {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    protected final CloudFile cloudFile;

    public FileTestBase(TOptions options) {
        super(options);

        String fileName = "randomfiletest-" + UUID.randomUUID().toString();

        try {
            cloudFile = cloudFileDirectory.getFileReference(fileName);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync().then(Mono.fromCallable(() -> {
            cloudFile.create(options.getSize() + DEFAULT_BUFFER_SIZE);
            return 1;
        })).then();
    }
}
