/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

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
    public Properties properties() {
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
        String provisioningState() {
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