package com.azure.storage.blob.perfstress;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.azure.perfstress.RandomFlux;
import com.azure.perfstress.RandomStream;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.perfstress.core.ContainerTest;
import com.azure.storage.blob.specialized.BlobOutputStream;

import reactor.core.publisher.Mono;

public class UploadTest extends ContainerTest<SizeOptions> {
    private final BlobClient _blobClient;
    private final BlobAsyncClient _blobAsyncClient;

    public UploadTest(SizeOptions options) {
        super(options);

        String blobName = "uploadtest-" + UUID.randomUUID().toString();
        _blobClient = BlobContainerClient.getBlobClient(blobName);
        _blobAsyncClient = BlobContainerAsyncClient.getBlobAsyncClient(blobName);
    }

    @Override
    public void Run() {
        try {
            InputStream inputStream = RandomStream.create(Options.Size);
            BlobOutputStream blobOutputStream = _blobClient.getBlockBlobClient().getBlobOutputStream();
            inputStream.transferTo(blobOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> RunAsync() {
        return _blobAsyncClient.upload(RandomFlux.create(Options.Size), new ParallelTransferOptions()).then();
    }
}