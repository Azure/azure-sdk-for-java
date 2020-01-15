package com.azure.perfstress;

import reactor.core.publisher.Mono;

public class ExceptionTest extends PerfStressTest<PerfStressOptions> {
    public ExceptionTest(PerfStressOptions options) {
        super(options);
    }

    @Override
    public void Run() {
        try {
            throw new IllegalArgumentException();
        }
        catch (Exception ex) {
        }
    }

    @Override
    public Mono<Void> RunAsync() {
        try {
            throw new IllegalArgumentException();
        }
        catch (Exception ex) {
        }

        return Mono.empty();
    }
}