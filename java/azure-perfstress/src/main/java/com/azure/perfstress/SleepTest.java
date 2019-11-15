package com.azure.perfstress;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class SleepTest extends PerfStressTest<PerfStressOptions> {
    private static final AtomicInteger _instanceCount = new AtomicInteger();
    private final int _secondsPerOperation;

    public SleepTest(PerfStressOptions options) {
        super(options);

        int instanceCount = _instanceCount.incrementAndGet();
        _secondsPerOperation = Pow(2, instanceCount);
    }

    private static int Pow(int value, int exponent) {
        int power = 1;
        for (int i=0; i < exponent; i++) {
            power *= value;
        }
        return power;
    }

    @Override
    public void Run() {
        try {
            Thread.sleep(_secondsPerOperation * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<Void> RunAsync() {
        return Mono.delay(Duration.ofSeconds(_secondsPerOperation)).then();
    }
}