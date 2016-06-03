/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * AutoHealActions - Describes the actions which can be
 * taken by the auto-heal module when a rule is triggered.
 */
public class AutoHealActions {
    /**
     * ActionType - predefined action to be taken. Possible values include:
     * 'Recycle', 'LogEvent', 'CustomAction'.
     */
    @JsonProperty(required = true)
    private AutoHealActionType actionType;

    /**
     * CustomAction - custom action to be taken.
     */
    private AutoHealCustomAction customAction;

    /**
     * MinProcessExecutionTime - minimum time the process must execute
     * before taking the action.
     */
    private String minProcessExecutionTime;

    /**
     * Get the actionType value.
     *
     * @return the actionType value
     */
    public AutoHealActionType actionType() {
        return this.actionType;
    }

    /**
     * Set the actionType value.
     *
     * @param actionType the actionType value to set
     * @return the AutoHealActions object itself.
     */
    public AutoHealActions withActionType(AutoHealActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    /**
     * Get the customAction value.
     *
     * @return the customAction value
     */
    public AutoHealCustomAction customAction() {
        return this.customAction;
    }

    /**
     * Set the customAction value.
     *
     * @param customAction the customAction value to set
     * @return the AutoHealActions object itself.
     */
    public AutoHealActions withCustomAction(AutoHealCustomAction customAction) {
        this.customAction = customAction;
        return this;
    }

    /**
     * Get the minProcessExecutionTime value.
     *
     * @return the minProcessExecutionTime value
     */
    public String minProcessExecutionTime() {
        return this.minProcessExecutionTime;
    }

    /**
     * Set the minProcessExecutionTime value.
     *
     * @param minProcessExecutionTime the minProcessExecutionTime value to set
     * @return the AutoHealActions object itself.
     */
    public AutoHealActions withMinProcessExecutionTime(String minProcessExecutionTime) {
        this.minProcessExecutionTime = minProcessExecutionTime;
        return this;
    }

}
