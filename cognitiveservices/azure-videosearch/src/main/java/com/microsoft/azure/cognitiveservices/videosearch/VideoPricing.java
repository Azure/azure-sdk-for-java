/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.videosearch;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for VideoPricing.
 */
public enum VideoPricing {
    /** Enum value All. */
    ALL("All"),

    /** Enum value Free. */
    FREE("Free"),

    /** Enum value Paid. */
    PAID("Paid");

    /** The actual serialized value for a VideoPricing instance. */
    private String value;

    VideoPricing(String value) {
        this.value = value;
    }

    /**
     * Parses a serialized value to a VideoPricing instance.
     *
     * @param value the serialized value to parse.
     * @return the parsed VideoPricing object, or null if unable to parse.
     */
    @JsonCreator
    public static VideoPricing fromString(String value) {
        VideoPricing[] items = VideoPricing.values();
        for (VideoPricing item : items) {
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
