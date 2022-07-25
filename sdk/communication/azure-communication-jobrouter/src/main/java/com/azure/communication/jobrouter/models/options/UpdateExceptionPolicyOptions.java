// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models.options;

import com.azure.communication.jobrouter.models.ExceptionRule;
import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Request options for updating ExceptionPolicy.
 * ExceptionPolicy: A policy that defines actions to execute when exception are triggered.
 */
@Fluent
public class UpdateExceptionPolicyOptions {
    /**
     * The Id of the exception policy
     */
    private final String id;

    /**
     * (Optional) A dictionary collection of exception rules on the exception
     * policy. Key is the Id of each exception rule.
     */
    private Map<String, ExceptionRule> exceptionRules;

    /**
     * (Optional) The name of the exception policy.
     */
    private String name;

    /**
     * Constructor for UpdateExceptionPolicyOptions.
     * @param id id of ExceptionPolicy.
     */
    public UpdateExceptionPolicyOptions(String id) {
        this.id = id;
    }

    /**
     * Sets exception rules.
     * @param exceptionRules Map of exception rules with a string key.
     * @return this
     */
    public UpdateExceptionPolicyOptions setExceptionRules(Map<String, ExceptionRule> exceptionRules) {
        this.exceptionRules = exceptionRules;
        return this;
    }

    /**
     * Sets ExceptionPolicy name.
     * @param name ExceptionPolicy name
     * @return this
     */
    public UpdateExceptionPolicyOptions setName(String name) {
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
     * Returns Exception Policy name.
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns Exception Rules.
     * @return exceptionRules.
     */
    public Map<String, ExceptionRule> getExceptionRules() {
        return this.exceptionRules;
    }
}
