// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ExceptionRule;
import com.azure.core.annotation.Fluent;

import java.util.Map;

/** Request options for Create ExceptionPolicy. */
@Fluent
public class CreateExceptionPolicyOptions {

    private String id;

    private String name;

    private Map<String, ExceptionRule> exceptionRules;

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
     * Returns id of ExceptionPolicy.
     * @return id
     */
    public String getId() {
        return this.id;
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
