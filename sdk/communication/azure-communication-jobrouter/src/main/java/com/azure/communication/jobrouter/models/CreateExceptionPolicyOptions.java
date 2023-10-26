// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Request options for Create ExceptionPolicy.
 * ExceptionPolicy: A policy that defines actions to execute when exception are triggered.
 */
@Fluent
public final class CreateExceptionPolicyOptions {
    /**
     * The Id of the exception policy
     */
    private final String exceptionPolicyId;

    /**
     * (Optional) A dictionary collection of exception rules on the exception
     * policy. Key is the Id of each exception rule.
     */
    private final List<ExceptionRule> exceptionRules;

    /**
     * (Optional) The name of the exception policy.
     */
    private String name;

    /**
     * Constructor for CreateExceptionPolicyOptions
     * @param exceptionPolicyId ExceptionPolicy id
     * @param exceptionRules Map of exception rules with a string key
     */
    public CreateExceptionPolicyOptions(String exceptionPolicyId, List<ExceptionRule> exceptionRules) {
        this.exceptionPolicyId = exceptionPolicyId;
        this.exceptionRules = exceptionRules;
    }

    /**
     * Sets ExceptionPolicy name.
     * @param name ExceptionPolicy name
     * @return this
     */
    public CreateExceptionPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns id of ExceptionPolicy.
     * @return id
     */
    public String getExceptionPolicyId() {
        return this.exceptionPolicyId;
    }

    /**
     * Returns Exception Rules.
     * @return exceptionRules.
     */
    public List<ExceptionRule> getExceptionRules() {
        return this.exceptionRules;
    }

    /**
     * Returns Exception Policy name.
     * @return name
     */
    public String getName() {
        return this.name;
    }
}
