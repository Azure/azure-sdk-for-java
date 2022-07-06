// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Request options for updating ExceptionPolicy.
 * ExceptionPolicy: A policy that defines actions to execute when exception are triggered.
 */
@Fluent
public class UpdateExceptionPolicyOptions extends ExceptionPolicyOptions {
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
    public ExceptionPolicyOptions setName(String name) {
        this.name = name;
        return this;
    }
}
