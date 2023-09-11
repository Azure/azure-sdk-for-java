// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Request options for Create ExceptionPolicy.
 * ExceptionPolicy: A policy that defines actions to execute when exception are triggered.
 */
@Fluent
public final class CreateExceptionPolicyOptions {
    /**
     * The Id of the exception policy
     */
    private final String id;

    /**
     * (Optional) A dictionary collection of exception rules on the exception
     * policy. Key is the Id of each exception rule.
     */
    private final Map<String, ExceptionRule> exceptionRules;

    /**
     * (Optional) The name of the exception policy.
     */
    private String name;

    /**
     * Constructor for CreateExceptionPolicyOptions
     * @param id ExceptionPolicy id
     * @param exceptionRules Map of exception rules with a string key
     */
    public CreateExceptionPolicyOptions(String id, Map<String, ExceptionRule> exceptionRules) {
        this.id = id;
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
    public String getId() {
        return this.id;
    }

    /**
     * Returns Exception Rules.
     * @return exceptionRules.
     */
    public Map<String, ExceptionRule> getExceptionRules() {
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
