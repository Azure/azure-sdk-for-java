package com.azure.cosmos.models;

public class NormalizedRange {
    private final String min;
    private final String max;

    public NormalizedRange(String min, String max) {
        this.min = min;
        this.max = max;
    }
}
