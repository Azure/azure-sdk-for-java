/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for BuiltInAuthenticationProvider.
 */
public enum BuiltInAuthenticationProvider {
    /** Enum value AzureActiveDirectory. */
    AZURE_ACTIVE_DIRECTORY("AzureActiveDirectory"),

    /** Enum value Facebook. */
    FACEBOOK("Facebook"),

    /** Enum value Google. */
    GOOGLE("Google"),

    /** Enum value MicrosoftAccount. */
    MICROSOFT_ACCOUNT("MicrosoftAccount"),

    /** Enum value Twitter. */
    TWITTER("Twitter");

    /** The actual serialized value for a BuiltInAuthenticationProvider instance. */
    private String value;

    BuiltInAuthenticationProvider(String value) {
        this.value = value;
    }

    /**
     * Gets the serialized value for a BuiltInAuthenticationProvider instance.
     *
     * @return the serialized value.
     */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /**
     * Parses a serialized value to a BuiltInAuthenticationProvider instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed BuiltInAuthenticationProvider object, or null if unable to parse.
     */
    @JsonCreator
    public static BuiltInAuthenticationProvider fromValue(String value) {
        BuiltInAuthenticationProvider[] items = BuiltInAuthenticationProvider.values();
        for (BuiltInAuthenticationProvider item : items) {
            if (item.toValue().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return toValue();
    }
}
