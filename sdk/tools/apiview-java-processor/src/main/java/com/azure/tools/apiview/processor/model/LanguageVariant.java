// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

/**
 * Java library variants recognized by APIView.
 */
public enum LanguageVariant {
    DEFAULT("None"), SPRING("Spring"), ANDROID("Android");

    private final String variantName;

    LanguageVariant(String name) {
        this.variantName = name;
    }

    @Override
    public String toString() {
        return this.variantName;
    }

    /**
     * Finds the Java library variant by its serialized name.
     *
     * @param name the name
     * @return the matching variant, or null if the name is unknown
     */
    public static LanguageVariant fromString(String name) {
        for (LanguageVariant variant : LanguageVariant.values()) {
            if (variant.toString().equals(name)) {
                return variant;
            }
        }
        return null;
    }
}
