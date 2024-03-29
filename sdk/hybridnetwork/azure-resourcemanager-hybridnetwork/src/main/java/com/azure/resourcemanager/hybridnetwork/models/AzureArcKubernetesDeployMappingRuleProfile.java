// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridnetwork.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Azure arc kubernetes deploy mapping rule profile.
 */
@Fluent
public final class AzureArcKubernetesDeployMappingRuleProfile extends MappingRuleProfile {
    /*
     * The helm mapping rule profile.
     */
    @JsonProperty(value = "helmMappingRuleProfile")
    private HelmMappingRuleProfile helmMappingRuleProfile;

    /**
     * Creates an instance of AzureArcKubernetesDeployMappingRuleProfile class.
     */
    public AzureArcKubernetesDeployMappingRuleProfile() {
    }

    /**
     * Get the helmMappingRuleProfile property: The helm mapping rule profile.
     * 
     * @return the helmMappingRuleProfile value.
     */
    public HelmMappingRuleProfile helmMappingRuleProfile() {
        return this.helmMappingRuleProfile;
    }

    /**
     * Set the helmMappingRuleProfile property: The helm mapping rule profile.
     * 
     * @param helmMappingRuleProfile the helmMappingRuleProfile value to set.
     * @return the AzureArcKubernetesDeployMappingRuleProfile object itself.
     */
    public AzureArcKubernetesDeployMappingRuleProfile
        withHelmMappingRuleProfile(HelmMappingRuleProfile helmMappingRuleProfile) {
        this.helmMappingRuleProfile = helmMappingRuleProfile;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AzureArcKubernetesDeployMappingRuleProfile
        withApplicationEnablement(ApplicationEnablement applicationEnablement) {
        super.withApplicationEnablement(applicationEnablement);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
        if (helmMappingRuleProfile() != null) {
            helmMappingRuleProfile().validate();
        }
    }
}
