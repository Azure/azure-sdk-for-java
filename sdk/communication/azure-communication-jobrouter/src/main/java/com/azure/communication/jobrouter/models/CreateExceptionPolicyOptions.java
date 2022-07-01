// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ExceptionRule;
import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Request options for Create ExceptionPolicy.
 * ExceptionPolicy: A policy that defines actions to execute when exception are triggered.
 */
@Fluent
public class CreateExceptionPolicyOptions extends ExceptionPolicyOptions {

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
}
