package com.azure.storage.blob.v8.perfstress;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.UUID;

import com.azure.perfstress.RandomStream;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.v8.perfstress.core.ContainerTest;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import reactor.core.publisher.Mono;

public class UploadTest extends ContainerTest<SizeOptions> {
    private final CloudBlockBlob _cloudBlockBlob;

    public UploadTest(SizeOptions options) {
        super(options);

        String blobName = "uploadtest-" + UUID.randomUUID().toString();

        try {
            _cloudBlockBlob = CloudBlobContainer.getBlockBlobReference(blobName);
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
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