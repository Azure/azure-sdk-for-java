// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.file.share.perf;

import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.share.perf.core.DirectoryTest;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public class DownloadToFileShareTest extends DirectoryTest<PerfStressOptions> {
    private static final String FILE_NAME = "perfstress-file-" + UUID.randomUUID().toString();

    private final CloudFile cloudFile;

    public DownloadToFileShareTest(PerfStressOptions options) {
        super(options);
        try {
            cloudFile = cloudFileDirectory.getFileReference(FILE_NAME);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }

    // Required resource setup goes here, upload the file to be downloaded during tests.
    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync()
            .then(Mono.fromCallable(() -> {
                try {
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
            File tempFile = File.createTempFile("tempFile", "fileshare");
            tempFile.deleteOnExit();
            cloudFile.downloadToFile(tempFile.getAbsolutePath());
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return Mono.fromCallable(() -> {
            cloudFile.delete();
            return 1;
        }).then(super.globalCleanupAsync());
    }
}
