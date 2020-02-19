// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.azure.perf.test.core.RandomStream;
import com.azure.perf.test.core.SizeOptions;
import com.azure.storage.blob.perf.core.BlobTestBase;
import reactor.core.publisher.Mono;

public class UploadFromFileTest extends BlobTestBase<SizeOptions> {

    private static Path tempFile;

    public UploadFromFileTest(SizeOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return super.globalSetupAsync().then(CreateTempFile());
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return DeleteTempFile().then(super.globalCleanupAsync());
    }

    private Mono<Void> CreateTempFile() {
        try {
            tempFile = Files.createTempFile(null, null);
            
            InputStream inputStream = RandomStream.create(options.getSize());
            OutputStream outputStream = new FileOutputStream(tempFile.toString());
            copyStream(inputStream, outputStream);
            outputStream.close();
            
            return Mono.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<Void> DeleteTempFile() {
        try {
            Files.delete(tempFile);
            return Mono.empty();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        blobClient.uploadFromFile(tempFile.toString(), true);
    }

    @Override
    public Mono<Void> runAsync() {
        return blobAsyncClient.uploadFromFile(tempFile.toString(), true);
    }
}
