package com.microsoft.storageperf.core;

public abstract class PerfStressTest<TOptions extends PerfStressOptions> {
    protected final TOptions Options;

    public PerfStressTest(TOptions options) {
        Options = options;
    }

    public void GlobalSetup() {
    }

    public void Setup() {
    }

    public abstract void Run();

    public void Cleanup() {
    }

    public void GlobalCleanup() {
    }
}
