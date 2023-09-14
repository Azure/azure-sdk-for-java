// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management;

import java.util.LinkedHashMap;

/**
 * The return object of getVariant that has the name of the variant and the instance value of the variant.
 */
public class Variant {

    private String name;

    private LinkedHashMap<String, Object> value;

    /**
     * Variant
     * @param name Name of the Variant
     * @param value Instance of the Variant
     */
    public Variant(String name, LinkedHashMap<String, Object> value) {
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
    public LinkedHashMap<String, Object> getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(LinkedHashMap<String, Object> value) {
        this.value = value;
    }

}
