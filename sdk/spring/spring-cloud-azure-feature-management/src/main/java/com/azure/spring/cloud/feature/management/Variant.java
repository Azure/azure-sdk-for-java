// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

/**
 * The return object of getVariant that has the name of the variant and the instance value of the variant.
 *
 * @param <T> Type of the returned Variant
 */
public class Variant<T> {

    private String name;

    private T value;

    /**
     * Variant
     * @param name Name of the Variant
     * @param value Instance of the Variant
     */
    public Variant(String name, T value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public T getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(T value) {
        this.value = value;
    }

}
