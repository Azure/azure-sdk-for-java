/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.blob.models;

public class GetBlobOptions extends BlobServiceOptions {
    private String snapshot;
    private String leaseId;
    private boolean computeRangeMD5;
    private Long rangeStart;
    private Long rangeEnd;
    private AccessCondition accessCondition;

    @Override
    public GetBlobOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public GetBlobOptions setSnapshot(String snapshot) {
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

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public GetBlobOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
