package com.azure.storage.blob.perfstress;

import java.io.IOException;

import com.azure.perfstress.RandomFlux;
import com.azure.perfstress.RandomStream;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.perfstress.core.RandomBlobTest;

import reactor.core.publisher.Mono;

public class UploadTest extends RandomBlobTest<SizeOptions> {

    public UploadTest(SizeOptions options) {
        super(options);
    }

    @Override
    public void Run() {
        try {
            _blobClient.getBlockBlobClient().upload(RandomStream.create(Options.Size), Options.Size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> RunAsync() {
        return _blobAsyncClient.upload(RandomFlux.create(Options.Size), new ParallelTransferOptions()).then();
    }
}