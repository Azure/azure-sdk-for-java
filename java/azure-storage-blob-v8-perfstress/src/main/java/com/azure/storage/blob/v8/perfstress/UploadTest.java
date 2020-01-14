package com.azure.storage.blob.v8.perfstress;

import java.io.IOException;

import com.azure.perfstress.RandomStream;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.v8.perfstress.core.RandomBlobTest;
import com.microsoft.azure.storage.StorageException;

import reactor.core.publisher.Mono;

public class UploadTest extends RandomBlobTest<SizeOptions> {

    public UploadTest(SizeOptions options) {
        super(options);
    }

    @Override
    public void Run() {
        try {
            _cloudBlockBlob.upload(RandomStream.create(Options.Size), Options.Size);
        } catch (StorageException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> RunAsync() {
        throw new UnsupportedOperationException();
    }
}