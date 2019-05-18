// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.AppendPositionAccessConditions;
import com.microsoft.azure.storage.blob.models.LeaseAccessConditions;
import com.microsoft.azure.storage.blob.models.ModifiedAccessConditions;

/**
 * This class contains values that restrict the successful completion of AppendBlock operations to certain conditions.
 * Any field may be set to null if no access conditions are desired.
 * <p>
 * Please refer to the request header section
 * <a href=https://docs.microsoft.com/en-us/rest/api/storageservices/append-block>here</a> for more conceptual
 * information.
 */
public final class AppendBlobAccessConditions {

    private AppendPositionAccessConditions appendPositionAccessConditions;

    private ModifiedAccessConditions modifiedAccessConditions;

    private LeaseAccessConditions leaseAccessConditions;

    /**
     * Creates an instance which has fields set to non-null, empty values.
     */
    public AppendBlobAccessConditions() {
        appendPositionAccessConditions = new AppendPositionAccessConditions();
        modifiedAccessConditions = new ModifiedAccessConditions();
        leaseAccessConditions = new LeaseAccessConditions();
    }

    /**
     * Access conditions used for appending data only if the operation meets the provided conditions related to the
     * size of the append blob.
     */
    public AppendPositionAccessConditions appendPositionAccessConditions() {
        return appendPositionAccessConditions;
    }

    /**
     * Access conditions used for appending data only if the operation meets the provided conditions related to the
     * size of the append blob.
     */
    public AppendBlobAccessConditions withAppendPositionAccessConditions(
            AppendPositionAccessConditions appendPositionAccessConditions) {
        this.appendPositionAccessConditions = appendPositionAccessConditions;
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
    public AppendBlobAccessConditions withModifiedAccessConditions(ModifiedAccessConditions modifiedAccessConditions) {
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
    public AppendBlobAccessConditions withLeaseAccessConditions(LeaseAccessConditions leaseAccessConditions) {
        this.leaseAccessConditions = leaseAccessConditions;
        return this;
    }
}
