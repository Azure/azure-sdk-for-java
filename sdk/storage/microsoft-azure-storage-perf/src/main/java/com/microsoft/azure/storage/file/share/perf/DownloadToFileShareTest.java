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
    private final File targetFile;
    private final String targetFilePath;
    private final CloudFile cloudFile;

    public DownloadToFileShareTest(PerfStressOptions options) {
        super(options);
        try {
            String fileName = "perfstress-file-" + UUID.randomUUID().toString();
            targetFile = new File(UUID.randomUUID().toString());
            targetFilePath = targetFile.getAbsolutePath();
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
            cloudFile.downloadToFile(targetFilePath);
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> cleanupAsync() {
        return Mono.fromCallable(() -> {
            targetFile.delete();
            return 1;
        }).then(super.cleanupAsync());
    }

    @Override
    public Mono<Void> runAsync() {
        throw new UnsupportedOperationException();
    }
}
