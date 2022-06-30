// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ExceptionRule;
import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Request options for updating ExceptionPolicy.
 * ExceptionPolicy: A policy that defines actions to execute when exception are triggered.
 */
@Fluent
public class UpdateExceptionPolicyOptions {
    private String id;

    private String name;

    private Map<String, ExceptionRule> exceptionRules;

    /**
     * Constructor for UpdateExceptionPolicyOptions.
     * @param id id of ExceptionPolicy.
     */
    public UpdateExceptionPolicyOptions(String id) {
        this.id = id;
    }

    /**
     * Returns ExceptionPolicy id.
     * @return id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets name of ExceptionPolicy
     * @param name name of exceptionPolicy
     * @return this
     */
    public UpdateExceptionPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns ExceptionPolicy name.
     * @return name
     */
    public String getName() {
        return name;
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
     * Returns exception rules.
     * @return exceptionRules
     */
    public Map<String, ExceptionRule> getExceptionRules() {
        return this.exceptionRules;
    }
}
