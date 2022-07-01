// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter.models;

import com.azure.communication.jobrouter.implementation.models.ExceptionRule;

import java.util.Map;

/**
 * Abstract class for Create and Update ExceptionPolicyOptions.
 */
public abstract class ExceptionPolicyOptions {

    /**
     * The Id of the exception policy
     */
    protected String id;

    /**
     * (Optional) The name of the exception policy.
     */
    protected String name;

    /**
     * (Optional) A dictionary collection of exception rules on the exception
     * policy. Key is the Id of each exception rule.
     */
    protected Map<String, ExceptionRule> exceptionRules;

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
