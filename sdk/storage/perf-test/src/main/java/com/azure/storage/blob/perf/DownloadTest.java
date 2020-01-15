package com.azure.storage.blob.perfstress;

import java.io.IOException;
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

        String blobName = "downloadtest";
        _blobClient = BlobContainerClient.getBlobClient(blobName);
        _blobAsyncClient = BlobContainerAsyncClient.getBlobAsyncClient(blobName);
    }

    public Mono<Void> GlobalSetupAsync() {
        return super.GlobalSetupAsync()
            .then(_blobAsyncClient.upload(RandomFlux.create(Options.Size), null))
            .then();
    }

    @Override
    public void Run() {
        _blobClient.download(new NullOutputStream());
    }


    /**Writes to nowhere*/
    class NullOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
        }
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