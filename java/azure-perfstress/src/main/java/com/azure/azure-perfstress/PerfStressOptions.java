package com.azure.perfstress;

import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
public class PerfStressOptions {
    @Parameter(names = { "-d", "--duration" }, description = "Duration of test in seconds")
    public int Duration = 10;

    @Parameter(names = { "--no-cleanup" }, description = "Disables test cleanup")
    public boolean NoCleanup = false;

    @Parameter(names = { "-p", "--parallel" }, description = "Number of operations to execute in parallel")
    public int Parallel = 1;

    @Parameter(names = { "-w", "--warmup" }, description = "Duration of warmup in seconds")
    public int Warmup = 10;

    @Parameter(names = { "--sync" }, description = "Runs sync version of test")
    public boolean Sync = false;
}