package com.azure.communication.jobrouter.models.options;

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
     * @param maxPageSize Maximum number of items per page.
     */
    public ListClassificationPoliciesOptions(Integer maxPageSize) {
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
