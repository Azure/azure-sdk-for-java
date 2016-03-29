/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * A mobile service.
 */
@JsonFlatten
public class ClassicMobileService extends Resource {
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
    public String getClassicMobileServiceName() {
        return this.classicMobileServiceName;
    }

    /**
     * Set the classicMobileServiceName value.
     *
     * @param classicMobileServiceName the classicMobileServiceName value to set
     */
    public void setClassicMobileServiceName(String classicMobileServiceName) {
        this.classicMobileServiceName = classicMobileServiceName;
    }

}
