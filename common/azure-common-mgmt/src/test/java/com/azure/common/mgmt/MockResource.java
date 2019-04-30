// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.mgmt;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MockResource {
    @JsonProperty()
    private String name;

    @JsonProperty()
    private Properties properties;

    /**
     * Gets the name of the resource.
     *
     * @return Name of the resource.
     */
    public String name() {
        return name;
    }

    /**
     * Sets the name of the resource.
     *
     * @param name Name of the resource.
     */
    public void name(String name) {
        this.name = name;
    }

    /**
     * Gets any properties associated with this resource.
     * @return Properties associated with resource.
     */
    public Properties properties() {
        return properties;
    }

    public void properties(Properties properties) {
        this.properties = properties;
    }

    public static class Properties {
        private String provisioningState;

        public String provisioningState() {
            return provisioningState;
        }

        public void provisioningState(String provisioningState) {
            this.provisioningState = provisioningState;
        }
    }
}
