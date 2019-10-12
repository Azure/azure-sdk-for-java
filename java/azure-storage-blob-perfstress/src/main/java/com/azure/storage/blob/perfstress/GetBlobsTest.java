package com.azure.storage.blob.perfstress;

import java.util.UUID;

import com.azure.perfstress.CountOptions;

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
                .flatMap(b -> ContainerAsyncClient.getBlockBlobAsyncClient(b).upload(Flux.empty(), 0))
                .then());
    }

    @Override
    public void Run() {
        ContainerClient.listBlobsFlat().forEach(b -> {});
    }

    @Override
    public Mono<Void> RunAsync() {
        return ContainerAsyncClient.listBlobsFlat().then();
    }
}