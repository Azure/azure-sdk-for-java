// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import com.azure.core.annotation.Fluent;

/**
 *  Request options to list DistributionPolicies.
 *  DistributionPolicy: Policy governing how jobs are distributed to workers.
 * */
@Fluent
public class ListDistributionPoliciesOptions {

    /**
     * Maximum number of items per page.
     */
    private final Integer maxPageSize;

    /**
     * Constructor for ListDistributionPoliciesOptions.
     * @param maxPageSize Maximum number of items per page.
     */
    public ListDistributionPoliciesOptions(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    /**
     * Returns maxPageSize.
     * @return maxPageSize
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
    }
}
