/**
 * Object]
 */

package com.microsoft.azure.management.website.models;


/**
 * Name value pair.
 */
public class NameValuePair {
    /**
     * Pair name.
     */
    private String name;

    /**
     * Pair value.
     */
    private String value;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

}
