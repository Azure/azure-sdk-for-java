package com.microsoft.azure.services.blob;

import java.util.Date;

public class GetBlobOptions {
    private Date snapshot;
    private String leaseId;
    private boolean computeRangeMD5;
    private Long rangeStart;
    private Long rangeEnd;

    public Date getSnapshot() {
        return snapshot;
    }

    public GetBlobOptions setSnapshot(Date snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public GetBlobOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public boolean isComputeRangeMD5() {
        return computeRangeMD5;
    }

    public GetBlobOptions setComputeRangeMD5(boolean computeRangeMD5) {
        this.computeRangeMD5 = computeRangeMD5;
        return this;
    }

    public Long getRangeStart() {
        return rangeStart;
    }

    public GetBlobOptions setRangeStart(Long rangeStart) {
        this.rangeStart = rangeStart;
        return this;
    }

    public Long getRangeEnd() {
        return rangeEnd;
    }

    public GetBlobOptions setRangeEnd(Long rangeEnd) {
        this.rangeEnd = rangeEnd;
        return this;
    }
}
