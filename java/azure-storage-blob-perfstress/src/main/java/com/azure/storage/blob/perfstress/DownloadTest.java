package com.azure.storage.blob.perfstress;

import java.io.OutputStream;

import com.azure.perfstress.RandomFlux;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.perfstress.core.ContainerTest;

import reactor.core.publisher.Mono;

public class DownloadTest extends ContainerTest<SizeOptions> {
    private final BlobClient _blobClient;
    private final BlobAsyncClient _blobAsyncClient;

    public DownloadTest(SizeOptions options) {
        super(options);

        _blobClient = BlobContainerClient.getBlobClient("downloadtest");
        _blobAsyncClient = BlobContainerAsyncClient.getBlobAsyncClient("downloadtest");
    }

    public Mono<Void> GlobalSetupAsync() {
        return super.GlobalSetupAsync()
            .then(_blobAsyncClient.upload(RandomFlux.create(Options.Size), new ParallelTransferOptions()))
            .then();
    }

    @Override
    public void Run() {
        _blobClient.download(OutputStream.nullOutputStream());
    }

    @Override
    public Mono<Void> RunAsync() {
return _blobAsyncClient.download()
    .map(b -> {
        b.get(new byte[b.remaining()]);
        return 1;
    })
    .then();
    }
}