// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.file.share.perf;

import com.azure.perf.test.core.NullOutputStream;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.share.perf.core.DirectoryTest;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.UUID;

public class DownloadFileShareTest extends DirectoryTest<PerfStressOptions> {
    private static final OutputStream DEV_NULL = new NullOutputStream();

    private final CloudFile cloudFile;

    public DownloadFileShareTest(PerfStressOptions options) {
        super(options);
        try {
            String fileName = "perfstress-file-" + UUID.randomUUID().toString();
            cloudFile = cloudFileDirectory.getFileReference(fileName);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.

    @Override
    public Mono<Void> setupAsync() {
        return super.setupAsync()
            .then(Mono.fromCallable(() -> {
                try {
                    cloudFile.create(options.getSize());
                    cloudFile.upload(TestDataCreationHelper.createRandomInputStream(options.getSize()),
                        options.getSize());
                    return 1;
                } catch (URISyntaxException | StorageException | IOException e) {
                    throw new RuntimeException(e);
                }
            })).then();
    }


    // Perform the API call to be tested here
    @Override
    public void run() {
        try {
            cloudFile.download(DEV_NULL);
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
