/**
 * Object]
 */

package com.microsoft.azure.management.website.models;

import java.util.Map;

/**
 * String dictionary resource.
 */
public class StringDictionary extends Resource {
    /**
     * Settings.
     */
    private Map<String, String> properties;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

}
