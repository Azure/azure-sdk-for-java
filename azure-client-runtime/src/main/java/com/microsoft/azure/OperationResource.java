/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The OperationResource class is a POJO representation of the Azure-AsyncOperation Operation
 * Resource format (see https://github.com/Azure/azure-resource-manager-rpc/blob/master/v1.0/Addendum.md#operation-resource-format
 * for more information).
 */
public class OperationResource {
    @JsonProperty(value = "properties")
    private Properties properties;

    /**
     * Get the inner properties object.
     * @return The inner properties object.
     */
    public Properties properties() {
        return properties;
    }

    /**
     * Set the properties of this OperationResource.
     * @param properties The properties of this OperationResource.
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
         * Get the provisioning state of the resource.
         * @return The provisioning state of the resource.
         */
        String provisioningState() {
            return provisioningState;
        }

        /**
         * Set the provisioning state of this OperationResource.
         * @param provisioningState The provisioning state of this OperationResource.
         */
        public void setProvisioningState(String provisioningState) {
            this.provisioningState = provisioningState;
        }
    }
}