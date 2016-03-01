/**
 * Object]
 */

package com.microsoft.azure.management.website.models;

import java.util.Map;

/**
 * String dictionary resource.
 */
public class ConnectionStringDictionary extends Resource {
    /**
     * Connection strings.
     */
    private Map<String, ConnStringValueTypePair> properties;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public Map<String, ConnStringValueTypePair> getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(Map<String, ConnStringValueTypePair> properties) {
        this.properties = properties;
    }

}
