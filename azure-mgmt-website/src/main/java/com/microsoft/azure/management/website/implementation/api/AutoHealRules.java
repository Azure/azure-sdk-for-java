/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * AutoHealRules - describes the rules which can be defined for auto-heal.
 */
public class AutoHealRules {
    /**
     * Triggers - Conditions that describe when to execute the auto-heal
     * actions.
     */
    private AutoHealTriggers triggers;

    /**
     * Actions - Actions to be executed when a rule is triggered.
     */
    private AutoHealActions actions;

    /**
     * Get the triggers value.
     *
     * @return the triggers value
     */
    public AutoHealTriggers triggers() {
        return this.triggers;
    }

    /**
     * Set the triggers value.
     *
     * @param triggers the triggers value to set
     * @return the AutoHealRules object itself.
     */
    public AutoHealRules withTriggers(AutoHealTriggers triggers) {
        this.triggers = triggers;
        return this;
    }

    /**
     * Get the actions value.
     *
     * @return the actions value
     */
    public AutoHealActions actions() {
        return this.actions;
    }

    /**
     * Set the actions value.
     *
     * @param actions the actions value to set
     * @return the AutoHealRules object itself.
     */
    public AutoHealRules withActions(AutoHealActions actions) {
        this.actions = actions;
        return this;
    }

}
