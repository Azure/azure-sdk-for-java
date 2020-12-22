// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Proxy Resource model.
 */
public class ProxyResource {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String name;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String type;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private SystemData systemData;

    /**
     * Get the id value.
     *
     * @return the fully qualified resource ID for the resource.
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the name value.
     *
     * @return the name of the resource.
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the type value.
     *
     * @return the type of the resource.
     */
    public String type() {
        return this.type;
    }

    /**
     * Get the systemData value.
     *
     * @return the metadata pertaining to creation and last modification of the resource.
     */
    public SystemData systemData() {
        return this.systemData;
    }
}
