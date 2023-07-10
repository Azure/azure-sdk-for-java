// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

/**
 *  Request options to list ClassificationPolicies.
 *  ClassificationPolicy: A container for the rules that govern how jobs are classified.
 * */
@Fluent
public class ListClassificationPoliciesOptions {
    /**
     * Maximum number of items per page.
     */
    private Integer maxPageSize;

    /**
     * Constructor for ListClassificationPoliciesOptions.
     */
    public ListClassificationPoliciesOptions() {
    }

    /**
     * Setter for maxPageSize.
     * @param maxPageSize maxPageSize.
     * @return object of type ListClassificationPoliciesOptions.
     */
    public ListClassificationPoliciesOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Returns maxPageSize.
     * @return maxPageSize
     */
    public Integer getMaxPageSize() {
        return this.maxPageSize;
    }
}
