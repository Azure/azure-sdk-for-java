/**
 * Object]
 */

package com.microsoft.azure.management.website.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

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
    public String getSiteInstanceName() {
        return this.siteInstanceName;
    }

    /**
     * Set the siteInstanceName value.
     *
     * @param siteInstanceName the siteInstanceName value to set
     */
    public void setSiteInstanceName(String siteInstanceName) {
        this.siteInstanceName = siteInstanceName;
    }

}
