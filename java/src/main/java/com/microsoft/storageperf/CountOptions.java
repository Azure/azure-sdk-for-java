package com.microsoft.storageperf;

import com.beust.jcommander.Parameter;
import com.microsoft.storageperf.core.PerfStressOptions;

public class CountOptions extends PerfStressOptions {
    @Parameter(names = { "-c", "--count" }, description = "Number of blobs")
    public int Count = 500;
}