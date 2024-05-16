// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is the property for creating the 'FunctionDefinition'.
 *
 * @see FunctionParameters
 * @see FunctionDefinition
 */
public class FunctionProperties {
    // Type of Property
    private String type;
    // Description of the Property
    private String description;
    // Enum values for the Property
    @JsonProperty("enum")
    private List<String> enumString;

    /**
     * Get type of property.
     *
     * @return Type of property.
     */
    public String getType() {
        return type;
    }

    /**
     * Set type of property.
     *
     * @param type type of property.
     * @return Object itself.
     */
    public FunctionProperties setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get description of property.
     *
     * @return Description of property.
     */
    public String getDescription() {
        return description;
    }


    /**
     * Set description of property.
     *
     * @param description description of property.
     * @return Object itself.
     */
    public FunctionProperties setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get enum values for the property.
     *
     * @return Enum values for the property.
     */
    public List<String> getEnumString() {
        return enumString;
    }

    /**
     * Set enum values for the property.
     *
     * @param enumString enum values for the property.
     * @return Object itself.
     */
    public FunctionProperties setEnumString(List<String> enumString) {
        this.enumString = enumString;
        return this;
    }
}
