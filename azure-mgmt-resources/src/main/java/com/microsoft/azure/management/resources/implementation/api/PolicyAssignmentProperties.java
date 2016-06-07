/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Policy Assignment properties.
 */
public class PolicyAssignmentProperties {
    /**
     * Gets or sets the policy assignment scope.
     */
    private String scope;

    /**
     * Gets or sets the policy assignment display name.
     */
    private String displayName;

    /**
     * Gets or sets the policy definition Id.
     */
    private String policyDefinitionId;

    /**
     * Get the scope value.
     *
     * @return the scope value
     */
    public String scope() {
        return this.scope;
    }

    /**
     * Set the scope value.
     *
     * @param scope the scope value to set
     * @return the PolicyAssignmentProperties object itself.
     */
    public PolicyAssignmentProperties withScope(String scope) {
        this.scope = scope;
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
     * @return the PolicyAssignmentProperties object itself.
     */
    public PolicyAssignmentProperties withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the policyDefinitionId value.
     *
     * @return the policyDefinitionId value
     */
    public String policyDefinitionId() {
        return this.policyDefinitionId;
    }

    /**
     * Set the policyDefinitionId value.
     *
     * @param policyDefinitionId the policyDefinitionId value to set
     * @return the PolicyAssignmentProperties object itself.
     */
    public PolicyAssignmentProperties withPolicyDefinitionId(String policyDefinitionId) {
        this.policyDefinitionId = policyDefinitionId;
        return this;
    }

}
