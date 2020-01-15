package com.azure.perfstress;

import com.beust.jcommander.Parameter;

public class CountOptions extends PerfStressOptions {
    @Parameter(names = { "-c", "--count" }, description = "Number of items")
    public int Count = 10;
}