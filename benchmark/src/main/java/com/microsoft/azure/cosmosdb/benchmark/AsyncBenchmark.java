package com.microsoft.azure.cosmosdb.benchmark;

public interface AsyncBenchmark {

    public abstract void run() throws Exception;

    public abstract void shutdown();
}
