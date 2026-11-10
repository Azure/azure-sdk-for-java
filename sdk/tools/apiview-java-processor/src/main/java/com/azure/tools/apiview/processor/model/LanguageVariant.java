
package com.azure.tools.apiview.processor.model;

public enum LanguageVariant {
    DEFAULT("None"),
    SPRING("Spring"),
    ANDROID("Android");

    private final String variantName;

    LanguageVariant(String name) {
        this.variantName = name;
    }

    @Override
    public String toString() {
        return this.variantName;
    }

    public static LanguageVariant fromString(String name) {
        for (LanguageVariant variant : LanguageVariant.values()) {
            if (variant.toString().equals(name)) {
                return variant;
            }
        }
        return null;
    }
}
