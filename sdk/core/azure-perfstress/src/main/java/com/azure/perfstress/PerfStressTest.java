package com.azure.perfstress;

import reactor.core.publisher.Mono;

public abstract class PerfStressTest<TOptions extends PerfStressOptions> {
    protected final TOptions Options;

    public PerfStressTest(TOptions options) {
        Options = options;
    }

    public Mono<Void> GlobalSetupAsync() {
        return Mono.empty();
    }

    public Mono<Void> SetupAsync() {
        return Mono.empty();
    }

    public abstract void Run();

    public abstract Mono<Void> RunAsync();

    public Mono<Void> CleanupAsync() {
        return Mono.empty();
    }

    public Mono<Void> GlobalCleanupAsync() {
        return Mono.empty();
    }
}
