package com.microsoft.storageperf.core;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder(alphabetic = true)
public class PerfStressOptions {
    public int Duration = 10;
    public boolean NoCleanup = false;
    public int Parallel = 1;
    public boolean Sync = false;
}