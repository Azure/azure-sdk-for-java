/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.LeaseAccessConditions;
import com.microsoft.azure.storage.blob.models.ModifiedAccessConditions;
import com.microsoft.azure.storage.blob.models.SequenceNumberAccessConditions;

/**
 * This class contains values that restrict the successful completion of PageBlob operations to certain conditions.
 * It may be set to null if no access conditions are desired.
 * <p>
 * Please refer to the request header section
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/put-page>here</a> for more conceptual information.
 */
public final class PageBlobAccessConditions {

    private SequenceNumberAccessConditions sequenceNumberAccessConditions;

    private ModifiedAccessConditions modifiedAccessConditions;

    private LeaseAccessConditions leaseAccessConditions;

    /**
     * Creates an instance which has fields set to non-null, empty values.
     */
    public PageBlobAccessConditions() {
        this.sequenceNumberAccessConditions = new SequenceNumberAccessConditions();
        this.modifiedAccessConditions = new ModifiedAccessConditions();
        this.leaseAccessConditions = new LeaseAccessConditions();
    }

    /**
     * Access conditions that will fail the request if the sequence number does not meet the provided condition.
     */
    public SequenceNumberAccessConditions sequenceNumberAccessConditions() {
        return sequenceNumberAccessConditions;
    }

    /**
     * Access conditions that will fail the request if the sequence number does not meet the provided condition.
     */
    public PageBlobAccessConditions withSequenceNumberAccessConditions(
            SequenceNumberAccessConditions sequenceNumberAccessConditions) {
        this.sequenceNumberAccessConditions = sequenceNumberAccessConditions;
        return this;
    }

    /**
     * Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used to
     * construct conditions related to when the blob was changed relative to the given request. The request
     * will fail if the specified condition is not satisfied.
     */
    public ModifiedAccessConditions modifiedAccessConditions() {
        return modifiedAccessConditions;
    }

    /**
     * Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used to
     * construct conditions related to when the blob was changed relative to the given request. The request
     * will fail if the specified condition is not satisfied.
     */
    public PageBlobAccessConditions withModifiedAccessConditions(ModifiedAccessConditions modifiedAccessConditions) {
        this.modifiedAccessConditions = modifiedAccessConditions;
        return this;
    }

    /**
     * By setting lease access conditions, requests will fail if the provided lease does not match the active lease on
     * the blob.
     */
    public LeaseAccessConditions leaseAccessConditions() {
        return leaseAccessConditions;
    }

    /**
     * By setting lease access conditions, requests will fail if the provided lease does not match the active lease on
     * the blob.
     */
    public PageBlobAccessConditions withLeaseAccessConditions(LeaseAccessConditions leaseAccessConditions) {
        this.leaseAccessConditions = leaseAccessConditions;
        return this;
    }
}
