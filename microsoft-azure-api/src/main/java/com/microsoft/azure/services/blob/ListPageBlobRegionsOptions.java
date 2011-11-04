package com.microsoft.azure.services.blob;

public class ListPageBlobRegionsOptions {
    private String leaseId;
    private String snapshot;
    private Long rangeStart;
    private Long rangeEnd;

    public String getLeaseId() {
        return leaseId;
    }

    public ListPageBlobRegionsOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public ListPageBlobRegionsOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public Long getRangeStart() {
        return rangeStart;
    }

    public ListPageBlobRegionsOptions setRangeStart(Long rangeStart) {
        this.rangeStart = rangeStart;
        return this;
    }

    public Long getRangeEnd() {
        return rangeEnd;
    }

    public ListPageBlobRegionsOptions setRangeEnd(Long rangeEnd) {
        this.rangeEnd = rangeEnd;
        return this;
    }
}
