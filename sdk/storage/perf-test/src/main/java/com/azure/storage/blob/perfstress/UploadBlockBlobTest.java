package com.azure.storage.blob.perfstress;

import java.io.IOException;

import com.azure.perfstress.RandomFlux;
import com.azure.perfstress.RandomStream;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.perfstress.core.RandomBlobTest;

import reactor.core.publisher.Mono;

public class UploadBlockBlobTest extends RandomBlobTest<SizeOptions> {
    public UploadBlockBlobTest(SizeOptions options) {
        super(options);
    }

    @Override
    public void Run() {
        _blockBlobClient.upload(RandomStream.create(Options.Size), Options.Size, true);
    }

    @Override
    public Mono<Void> RunAsync() {
        return _blockBlobAsyncClient.upload(RandomFlux.create(Options.Size), Options.Size, true).then();
    }
}