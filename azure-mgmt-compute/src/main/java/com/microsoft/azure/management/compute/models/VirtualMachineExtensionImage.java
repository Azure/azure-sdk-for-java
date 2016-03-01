/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Describes a Virtual Machine Extension Image.
 */
public class VirtualMachineExtensionImage extends SubResource {
    /**
     * The properties property.
     */
    private VirtualMachineExtensionImageProperties properties;

    /**
     * Gets or sets the name of the resource.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets the location of the resource.
     */
    @JsonProperty(required = true)
    private String location;

    /**
     * Gets or sets the tags attached to the resource.
     */
    private Map<String, String> tags;

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public VirtualMachineExtensionImageProperties getProperties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     */
    public void setProperties(VirtualMachineExtensionImageProperties properties) {
        this.properties = properties;
    }

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
     * Get the location value.
     *
     * @return the location value
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Set the location value.
     *
     * @param location the location value to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     */
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

}
