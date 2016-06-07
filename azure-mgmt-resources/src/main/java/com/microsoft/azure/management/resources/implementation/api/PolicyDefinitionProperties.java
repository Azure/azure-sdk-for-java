/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Policy definition properties.
 */
public class PolicyDefinitionProperties {
    /**
     * Gets or sets the policy definition description.
     */
    private String description;

    /**
     * Gets or sets the policy definition display name.
     */
    private String displayName;

    /**
     * The policy rule json.
     */
    private Object policyRule;

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public String description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the PolicyDefinitionProperties object itself.
     */
    public PolicyDefinitionProperties withDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the PolicyDefinitionProperties object itself.
     */
    public PolicyDefinitionProperties withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the policyRule value.
     *
     * @return the policyRule value
     */
    public Object policyRule() {
        return this.policyRule;
    }

    /**
     * Set the policyRule value.
     *
     * @param policyRule the policyRule value to set
     * @return the PolicyDefinitionProperties object itself.
     */
    public PolicyDefinitionProperties withPolicyRule(Object policyRule) {
        this.policyRule = policyRule;
        return this;
    }

}
