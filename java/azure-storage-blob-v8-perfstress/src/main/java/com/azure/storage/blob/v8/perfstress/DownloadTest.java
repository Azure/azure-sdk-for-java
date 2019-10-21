package com.azure.storage.blob.v8.perfstress;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;

import com.azure.perfstress.RandomStream;
import com.azure.perfstress.SizeOptions;
import com.azure.storage.blob.v8.perfstress.core.ContainerTest;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import reactor.core.publisher.Mono;

public class DownloadTest extends ContainerTest<SizeOptions> {
    private final CloudBlockBlob _cloudBlockBlob;

    public DownloadTest(SizeOptions options) {
        super(options);

        try {
            _cloudBlockBlob = CloudBlobContainer.getBlockBlobReference("downloadtest");
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException(e);
        }
    }

    public Mono<Void> GlobalSetupAsync() {
        return super.GlobalSetupAsync().doOnTerminate(() -> {
            try {
                _cloudBlockBlob.upload(RandomStream.create(Options.Size), Options.Size);
            } catch (StorageException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void Run() {
        try {
            _cloudBlockBlob.download(OutputStream.nullOutputStream());
        } catch (StorageException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> RunAsync() {
        throw new UnsupportedOperationException();
    }
}