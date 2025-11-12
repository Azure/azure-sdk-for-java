// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.polling;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FooWithProvisioningState {
    @JsonProperty(value = "properties")
    private Properties properties;

    public FooWithProvisioningState() {
    }

    public FooWithProvisioningState(String state) {
        this.properties = new Properties();
        this.properties.provisioningState = state;
    }

    public FooWithProvisioningState(String state, String resourceId) {
        this.properties = new Properties();
        this.properties.provisioningState = state;
        this.properties.resourceId = resourceId;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public String getProvisioningState() {
        return this.properties.getProvisioningState();
    }

    public String getResourceId() {
        return this.properties.getResourceId();
    }

    public class Properties {
        // Standard ProvisioningState property
        @JsonProperty(value = "provisioningState")
        private String provisioningState;
        // resourceId available when Foo is provisioned.
        @JsonProperty(value = "resourceId")
        private String resourceId;

        public Properties() {
        }

        public String getProvisioningState() {
            return provisioningState;
        }

        public String getResourceId() {
            return resourceId;
        }
    }
}
