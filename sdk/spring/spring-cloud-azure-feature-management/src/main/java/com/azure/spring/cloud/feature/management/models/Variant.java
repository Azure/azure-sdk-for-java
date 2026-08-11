// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.models;

/**
 * Represents a feature flag variant in Azure Feature Management.
 * This class is the return object of the getVariant method and contains both
 * the name of the variant and its corresponding value instance. Variants allow
 * for multiple implementations of a feature beyond simple on/off states.
 */
public class Variant {

    /**
     * The name of the variant that identifies this specific variation of the feature.
     * This is used to match against variant references in feature flag configurations.
     */
    private final String name;

    /**
     * The actual implementation value of the variant. This can be any type of object
     * that represents the variant's behavior or configuration.
     */
    private final Object value;

    /**
     * Creates a new Variant with the specified name and value.
     * 
     * @param name The name that identifies this variant in feature flag configurations
     * @param value The implementation value or configuration for this variant
     */
    public Variant(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets the name of this variant.
     * 
     * @return the name that identifies this variant
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the implementation value of this variant.
     * This can be any object that represents the variant's behavior or configuration.
     * 
     * @return the implementation value for this variant
     */
    public Object getValue() {
        return value;
    }

}
