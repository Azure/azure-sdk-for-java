// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.implementation.parsergen;

/**
 * Class that formats obverse names as various java element names.
 */
public class NameFormatter {
    private static final String CLASS_PREFIX = "DT";
    private static final String CLASS_SUFFIX = "Info";

    private static final String ENUM_PREFIX = "DT";
    private static final String ENUM_SUFFIX = "Kind";

    /**
     * Format an obverse name as a class name.
     *
     * @param name The obverse name.
     * @return The name for an obverse class.
     */
    public static String formatNameAsClass(String name) {
        if (name == null || name.length() < 2 || name.contains(" ")) {
            throw new IllegalArgumentException("Expected a non-null input with at least 2 characters and no spaces. Found: '" + name + "'");
        }

        return CLASS_PREFIX + Character.toUpperCase(name.charAt(0)) + name.substring(1) + CLASS_SUFFIX;
    }

    /**
     * Format an obverse name as a parameter name.
     *
     * @param name The obverse name.
     * @return The name for a parameter.
     */
    public static String formatNameAsParameter(String name) {
        if (name == null || name.length() < 2 || name.contains(" ")) {
            throw new IllegalArgumentException("Expected a non-null input with at least 2 characters and no spaces. Found: '" + name + "'");
        }

        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Format an obverse name as a property name.
     *
     * @param name The obverse name.
     * @return The name for a parameter of an obverse class.
     */
    public static String formatNameAsProperty(String name) {
        if (name == null || name.length() < 2 || name.contains(" ")) {
            throw new IllegalArgumentException("Expected a non-null input with at least 2 characters and no spaces. Found: '" + name + "'");
        }

        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Format an obverse name as an Enum name.
     *
     * @param name The obverse name.
     * @return The name for an obverse Enum.
     */
    public static String formatNameAsEnum(String name) {
        if (name == null || name.length() < 2 || name.contains(" ")) {
            throw new IllegalArgumentException("Expected a non-null input with at least 2 characters and no spaces. Found: '" + name + "'");
        }

        return ENUM_PREFIX + Character.toUpperCase(name.charAt(0)) + name.substring(1) + ENUM_SUFFIX;
    }

    /**
     * Format an obverse name as an Enum property.
     *
     * @param name The obverse name.
     * @return The name for an Enum property in an obverse class.
     */
    public static String formatNameAsEnumProperty(String name) {
        if (name == null || name.length() < 2 || name.contains(" ")) {
            throw new IllegalArgumentException("Expected a non-null input with at least 2 characters and no spaces. Found: '" + name + "'");
        }

        return Character.toLowerCase(name.charAt(0)) + name.substring(1) + ENUM_SUFFIX;
    }

    /**
     * Format an obverse name as an Enum value.
     *
     * @param name The obverse name.
     * @return The name for an Enum value.
     */
    public static String formatNameAsEnumValue(String name) {
        if (name == null || name.length() < 2 || name.contains(" ")) {
            throw new IllegalArgumentException("Expected a non-null input with at least 2 characters and no spaces. Found: '" + name + "'");
        }

        return camelCaseToUnderScoreUpperCase(name);
    }

    public static String formatNameAsField(String name) {
        if (name == null || name.length() < 2 || name.contains(" ")) {
            throw new IllegalArgumentException("Expected a non-null input with at least 2 characters and no spaces. Found: '" + name + "'");
        }

        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    public static String camelCaseToUnderScoreUpperCase(String camelCase) {
        StringBuilder result = new StringBuilder();
        boolean prevUpperCase = false;
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);

            if (Character.isUpperCase(c)) {
                if (prevUpperCase) {
                    return camelCase;
                }

                result.append("_").append(c);
                prevUpperCase = true;
            } else {
                result.append(Character.toUpperCase(c));
                prevUpperCase = false;
            }
        }

        if (result.toString().startsWith("_")) {
            result = new StringBuilder(result.substring(1));
        }

        return result.toString();
    }
}
