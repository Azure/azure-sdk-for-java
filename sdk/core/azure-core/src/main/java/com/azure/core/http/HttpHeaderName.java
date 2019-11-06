// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import java.util.Locale;

/**
 * Declare various headers which can be used to interact with Azure services.
 */
public enum HttpHeaderName {

    /**
     * The header which will have retry after value in milliseconds.
     */
    AZURE_RETRY_AFTER_MS_HEADER("retry-after-ms"),

    /**
     * The header which will have retry after value in milliseconds.
     */
     AZURE_X_MS_RETRY_AFTER_MS_HEADER("x-ms-retry-after-ms");

    private final String value;

    HttpHeaderName(final String value) {
        this.value = value;
    }

    /**
     * Returns string representation of the {@link HttpHeaderName}.
     */
    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Creates a {@link HttpHeaderName} from its string value.
     *
     * @param value The string value of the  {@link HttpHeaderName}.
     * @return The  {@link HttpHeaderName} represented by the value.
     * @throws IllegalArgumentException If a {@link HttpHeaderName} cannot be parsed from the string value.
     */
    public static HttpHeaderName fromString(final String value) {
        for (HttpHeaderName httpHeaderType : values()) {
            if (httpHeaderType.value.equalsIgnoreCase(value)) {
                return httpHeaderType;
            }
        }

        throw new IllegalArgumentException(String.format(Locale.US, "Could not convert %s to a HttpHeaderName",
            value));
    }
}
