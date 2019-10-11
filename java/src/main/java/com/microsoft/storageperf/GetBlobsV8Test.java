package com.microsoft.storageperf;

import java.io.InputStream;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class GetBlobsV8Test extends ContainerV8Test<CountOptions> {
    public GetBlobsV8Test(CountOptions options) {
        super(options);
    }

    public Mono<Void> GlobalSetupAsync() {
        return super.GlobalSetupAsync().then(
            Flux.range(0, Options.Count)
                .map(i -> "getblobstest-" + UUID.randomUUID())
                .flatMap(b -> Upload(b).then(Mono.just(1)))
                .then());
    }

    private Mono<Void> Upload(String blobName) {
        return Mono.empty()
            .publishOn(Schedulers.elastic())
            .then(Mono.fromCallable(() -> {
                CloudBlobContainer.getBlockBlobReference(blobName).upload(InputStream.nullInputStream(), 0);
                return 1;
            }))
            .then();
    }

    @Override
    public void Run() {
        CloudBlobContainer.listBlobs().forEach(b -> {});
    }

    @Override
    public Mono<Void> RunAsync() {
        throw new UnsupportedOperationException();
    }
}