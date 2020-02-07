package com.azure.perfstress;

import reactor.core.publisher.Mono;

public class NoOpTest extends PerfStressTest<PerfStressOptions> {
    public NoOpTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void Run() {
    }

    @Override
    public Mono<Void> RunAsync() {
        return Mono.empty();
    }
}