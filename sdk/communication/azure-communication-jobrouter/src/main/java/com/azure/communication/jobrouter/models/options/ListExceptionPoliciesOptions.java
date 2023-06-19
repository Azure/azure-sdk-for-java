package com.azure.communication.jobrouter.models.options;

import com.azure.core.annotation.Fluent;

/**
 *  Request options to list ExceptionPolicies.
 *  ExceptionPolicy: A policy that defines actions to execute when exception are triggered.
 * */
@Fluent
public class ListExceptionPoliciesOptions {
    /**
     * Maximum number of items per page.
     */
    private Integer maxPageSize;

    /**
     * Constructor for ListExceptionPoliciesOptions.
     * @param maxPageSize Maximum number of items per page.
     */
    public ListExceptionPoliciesOptions(Integer maxPageSize) {
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
