package com.azure.cosmos.cris.querystuckrepro;

public class PartReadAttris {
    private final int pageSize;
    public PartReadAttris(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageSize() {
        return this.pageSize;
    }
}
