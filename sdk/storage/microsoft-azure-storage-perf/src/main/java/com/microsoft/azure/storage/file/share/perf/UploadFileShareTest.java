// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.file.share.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.share.perf.core.FileTestBase;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;

public class UploadFileShareTest extends FileTestBase<PerfStressOptions> {
    protected final RepeatingInputStream inputStream;

    public UploadFileShareTest(PerfStressOptions options) {
        super(options);
        inputStream = (RepeatingInputStream) TestDataCreationHelper.createRandomInputStream(options.getSize());
    }

    @Override
    public void run() {
        try {
            inputStream.reset();
            cloudFile.upload(inputStream, options.getSize());
        } catch (StorageException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }

}
