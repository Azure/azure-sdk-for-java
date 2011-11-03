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

    public void setSnapshot(Date snapshot) {
        this.snapshot = snapshot;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public void setLeaseId(String leaseId) {
        this.leaseId = leaseId;
    }

    public boolean isComputeRangeMD5() {
        return computeRangeMD5;
    }

    public void setComputeRangeMD5(boolean computeRangeMD5) {
        this.computeRangeMD5 = computeRangeMD5;
    }

    public Long getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(Long rangeStart) {
        this.rangeStart = rangeStart;
    }

    public Long getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(Long rangeEnd) {
        this.rangeEnd = rangeEnd;
    }
}
