// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.file.share.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.share.perf.core.FileTestBase;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;

public class UploadFileShareTest extends FileTestBase<PerfStressOptions> {
    public UploadFileShareTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void run() {
        try {
            cloudFile.upload(TestDataCreationHelper.createRandomInputStream(options.getSize()),
                options.getSize());
        } catch (StorageException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }


}
