// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The ResourceWithProvisioningState class is a POJO representation of any Azure resource that has a
 * provisioningState property.
 */
public class ResourceWithProvisioningState {
    @JsonProperty(value = "properties")
    private Properties properties;

    /**
     * @return The inner properties object.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Set the properties of this ResourceWithProvisioningState.
     * @param properties The properties of this ResourceWithProvisioningState.
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Inner properties class.
     */
    public static class Properties {
        @JsonProperty(value = "provisioningState")
        private String provisioningState;

        /**
         * @return The provisioning state of the resource.
         */
        String getProvisioningState() {
            return provisioningState;
        }

        /**
         * Set the provisioning state of this ResourceWithProvisioningState.
         * @param provisioningState The provisioning state of this ResourceWithProvisioningState.
         */
        public void setProvisioningState(String provisioningState) {
            this.provisioningState = provisioningState;
        }
    }
}
