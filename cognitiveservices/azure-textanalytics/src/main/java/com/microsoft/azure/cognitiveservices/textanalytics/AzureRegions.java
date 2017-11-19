/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.textanalytics;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for AzureRegions.
 */
public enum AzureRegions {
    /** Enum value westus. */
    WESTUS("westus"),

    /** Enum value westeurope. */
    WESTEUROPE("westeurope"),

    /** Enum value southeastasia. */
    SOUTHEASTASIA("southeastasia"),

    /** Enum value eastus2. */
    EASTUS2("eastus2"),

    /** Enum value westcentralus. */
    WESTCENTRALUS("westcentralus"),

    /** Enum value westus2. */
    WESTUS2("westus2"),

    /** Enum value eastus. */
    EASTUS("eastus"),

    /** Enum value southcentralus. */
    SOUTHCENTRALUS("southcentralus"),

    /** Enum value northeurope. */
    NORTHEUROPE("northeurope"),

    /** Enum value eastasia. */
    EASTASIA("eastasia"),

    /** Enum value australiaeast. */
    AUSTRALIAEAST("australiaeast"),

    /** Enum value brazilsouth. */
    BRAZILSOUTH("brazilsouth");

    /** The actual serialized value for a AzureRegions instance. */
    private String value;

    AzureRegions(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a AzureRegions instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed AzureRegions object, or null if unable to parse.
     */
    @JsonCreator
    public static AzureRegions fromString(String value) {
        AzureRegions[] items = AzureRegions.values();
        for (AzureRegions item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }
}
