// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.storage.file.datalake.implementation.models.LeaseAccessConditions;
import com.azure.storage.file.datalake.implementation.models.ModifiedAccessConditions;

/**
 * This class contains values which will restrict the successful operation of a variety of requests to the conditions
 * present. These conditions are entirely optional. The entire object or any of its properties may be set to null when
 * passed to a method to indicate that those conditions are not desired. Please refer to the type of each field for more
 * information on those particular access conditions.
 */
public class PathAccessConditions {

    private ModifiedAccessConditions modifiedAccessConditions;
    private LeaseAccessConditions leaseAccessConditions;

    /**
     * Creates an instance which has fields set to non-null, empty values.
     */
    public PathAccessConditions() {
        this.modifiedAccessConditions = new ModifiedAccessConditions();
        this.leaseAccessConditions = new LeaseAccessConditions();
    }

    /**
     * Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used to
     * construct conditions related to when the blob was changed relative to the given request. The request
     * will fail if the specified condition is not satisfied.
     *
     * @return the modified access conditions
     */
    public ModifiedAccessConditions getModifiedAccessConditions() {
        return modifiedAccessConditions;
    }

    /**
     * Standard HTTP Access conditions related to the modification of data. ETag and LastModifiedTime are used to
     * construct conditions related to when the blob was changed relative to the given request. The request
     * will fail if the specified condition is not satisfied.
     *
     * @param modifiedAccessConditions the modified access conditions to set
     * @return the updated PathAccessConditions object
     */
    public PathAccessConditions setModifiedAccessConditions(ModifiedAccessConditions modifiedAccessConditions) {
        this.modifiedAccessConditions = modifiedAccessConditions;
        return this;
    }

    /**
     * By setting lease access conditions, requests will fail if the provided lease does not match the active lease on
     * the path.
     *
     * @return the lease access conditions
     */
    public LeaseAccessConditions getLeaseAccessConditions() {
        return leaseAccessConditions;
    }

    /**
     * By setting lease access conditions, requests will fail if the provided lease does not match the active lease on
     * the path.
     *
     * @param leaseAccessConditions the lease access conditions to set
     * @return the updated PathAccessConditions object
     */
    public PathAccessConditions setLeaseAccessConditions(LeaseAccessConditions leaseAccessConditions) {
        this.leaseAccessConditions = leaseAccessConditions;
        return this;
    }
}
