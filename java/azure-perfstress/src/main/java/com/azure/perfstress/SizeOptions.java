package com.azure.perfstress;

import com.beust.jcommander.Parameter;

public class SizeOptions extends PerfStressOptions {
    @Parameter(names = { "-s", "--size" }, description = "Size of payload (in bytes)")
    public int Size = 10 * 1024;
}