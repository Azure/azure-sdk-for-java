// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

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
     *
     * @return the sequence number access conditions
     */
    public SequenceNumberAccessConditions sequenceNumberAccessConditions() {
        return sequenceNumberAccessConditions;
    }

    /**
     * Access conditions that will fail the request if the sequence number does not meet the provided condition.
     *
     * @param sequenceNumberAccessConditions the sequence number access conditions to set
     * @return the updated PageBlobAccessConditions object
     */
    public PageBlobAccessConditions sequenceNumberAccessConditions(
            SequenceNumberAccessConditions sequenceNumberAccessConditions) {
        this.sequenceNumberAccessConditions = sequenceNumberAccessConditions;
        return this;
    }

    /**
     * Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used to
     * construct conditions related to when the blob was changed relative to the given request. The request
     * will fail if the specified condition is not satisfied.
     *
     * @return the modified access conditions
     */
    public ModifiedAccessConditions modifiedAccessConditions() {
        return modifiedAccessConditions;
    }

    /**
     * Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used to
     * construct conditions related to when the blob was changed relative to the given request. The request
     * will fail if the specified condition is not satisfied.
     *
     * @param modifiedAccessConditions the modified access conditions to set
     * @return the updated PageBlobAccessConditions object
     */
    public PageBlobAccessConditions modifiedAccessConditions(ModifiedAccessConditions modifiedAccessConditions) {
        this.modifiedAccessConditions = modifiedAccessConditions;
        return this;
    }

    /**
     * By setting lease access conditions, requests will fail if the provided lease does not match the active lease on
     * the blob.
     *
     * @return the lease access conditions
     */
    public LeaseAccessConditions leaseAccessConditions() {
        return leaseAccessConditions;
    }

    /**
     * By setting lease access conditions, requests will fail if the provided lease does not match the active lease on
     * the blob.
     *
     * @param leaseAccessConditions the lease access conditions to set
     * @return the updated PageBlobAccessConditions object
     */
    public PageBlobAccessConditions leaseAccessConditions(LeaseAccessConditions leaseAccessConditions) {
        this.leaseAccessConditions = leaseAccessConditions;
        return this;
    }
}
