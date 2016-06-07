/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * A mobile service.
 */
@JsonFlatten
public class ClassicMobileServiceInner extends Resource {
    /**
     * Name of the mobile service.
     */
    @JsonProperty(value = "properties.name")
    private String classicMobileServiceName;

    /**
     * Get the classicMobileServiceName value.
     *
     * @return the classicMobileServiceName value
     */
    public String classicMobileServiceName() {
        return this.classicMobileServiceName;
    }

    /**
     * Set the classicMobileServiceName value.
     *
     * @param classicMobileServiceName the classicMobileServiceName value to set
     * @return the ClassicMobileServiceInner object itself.
     */
    public ClassicMobileServiceInner withClassicMobileServiceName(String classicMobileServiceName) {
        this.classicMobileServiceName = classicMobileServiceName;
        return this;
    }

}
