package com.azure.storage.blob.perfstress;

import com.azure.perfstress.RandomFlux;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.perfstress.core.RandomBlobTest;

import reactor.core.publisher.Mono;

public class UploadTest extends RandomBlobTest<SizeOptions> {

    public UploadTest(SizeOptions options) {
        super(options);
    }

    @Override
    public void Run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> RunAsync() {
        return _blobAsyncClient.upload(RandomFlux.create(Options.Size), null).then();
    }
}