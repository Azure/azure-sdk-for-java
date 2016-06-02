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
 * Instance of a web app.
 */
@JsonFlatten
public class SiteInstance extends Resource {
    /**
     * Name of instance.
     */
    @JsonProperty(value = "properties.name")
    private String siteInstanceName;

    /**
     * Get the siteInstanceName value.
     *
     * @return the siteInstanceName value
     */
    public String siteInstanceName() {
        return this.siteInstanceName;
    }

    /**
     * Set the siteInstanceName value.
     *
     * @param siteInstanceName the siteInstanceName value to set
     * @return the SiteInstance object itself.
     */
    public SiteInstance withSiteInstanceName(String siteInstanceName) {
        this.siteInstanceName = siteInstanceName;
        return this;
    }

}
