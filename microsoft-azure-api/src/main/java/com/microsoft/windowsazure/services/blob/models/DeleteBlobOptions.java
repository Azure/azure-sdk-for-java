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

public class DeleteBlobOptions extends BlobServiceOptions {
    private String snapshot;
    private String leaseId;
    private boolean deleteSnaphotsOnly;
    private AccessCondition accessCondition;

    @Override
    public DeleteBlobOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public DeleteBlobOptions setSnapshot(String snapshot) {
        this.snapshot = snapshot;
        return this;
    }

    public String getLeaseId() {
        return leaseId;
    }

    public DeleteBlobOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    public boolean getDeleteSnaphotsOnly() {
        return deleteSnaphotsOnly;
    }

    public DeleteBlobOptions setDeleteSnaphotsOnly(boolean deleteSnaphotsOnly) {
        this.deleteSnaphotsOnly = deleteSnaphotsOnly;
        return this;
    }

    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    public DeleteBlobOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
