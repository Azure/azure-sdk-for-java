package com.azure.core.management.implementation.polling;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FooWithProvisioningState {
    @JsonProperty(value = "properties")
    public Properties properties;

    public FooWithProvisioningState() {}

    FooWithProvisioningState(String state) {
        this.properties = new Properties();
        this.properties.provisioningState = state;
    }

    FooWithProvisioningState(String state, String resourceId) {
        this.properties = new Properties();
        this.properties.provisioningState = state;
        this.properties.resourceId = resourceId;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public String getProvisioningState() {
        return this.properties.provisioningState;
    }

    public String getResourceId() {
        return this.properties.resourceId;
    }

    public class Properties {
        // Standard ProvisioningState property
        @JsonProperty(value = "provisioningState")
        public String provisioningState;
        // resourceId available when Foo is provisioned.
        @JsonProperty(value = "resourceId")
        public String resourceId;

        public Properties() {}
    }
}
