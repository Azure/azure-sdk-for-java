package com.azure.storage.blob.perfstress;

import java.util.UUID;

import com.azure.perfstress.CountOptions;
import com.azure.storage.blob.models.ParallelTransferOptions;
import com.azure.storage.blob.perfstress.core.ContainerTest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class GetBlobsTest extends ContainerTest<CountOptions> {
    public GetBlobsTest(CountOptions options) {
        super(options);
    }

    public Mono<Void> GlobalSetupAsync() {
        return super.GlobalSetupAsync().then(
            Flux.range(0, Options.Count)
                .map(i -> "getblobstest-" + UUID.randomUUID())
                .flatMap(b -> BlobContainerAsyncClient.getBlobAsyncClient(b).upload(Flux.empty(), null))
                .then());
    }

    @Override
    public void Run() {
        BlobContainerClient.listBlobs().forEach(b -> {});
    }

    @Override
    public Mono<Void> RunAsync() {
        return BlobContainerAsyncClient.listBlobs().then();
    }
}