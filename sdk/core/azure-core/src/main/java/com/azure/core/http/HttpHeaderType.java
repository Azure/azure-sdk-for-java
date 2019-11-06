package com.azure.core.http;

import java.util.Locale;

/**
 * Declare various headers which can be used to interact with Azure services.
 */
public enum HttpHeaderType {

    /**
     * The header which will have retry after value in milliseconds.
     */
    AZURE_RETRY_AFTER_MS_HEADER("retry-after-ms"),

    /**
     * The header which will have retry after value in milliseconds.
     */
     AZURE_X_MS_RETRY_AFTER_MS_HEADER ("x-ms-retry-after-ms");

    private final String value;

    HttpHeaderType(final String value) {
        this.value = value;
    }

    /**
     * Returns string representation of the value.
     */
    @Override
    public String toString() {
        return this.value;
    }

    /**
     * Creates an  {@link HttpHeaderType} from its display value.
     *
     * @param value The string value of the  {@link HttpHeaderType}.
     * @return The  {@link HttpHeaderType} represented by the value.
     * @throws IllegalArgumentException If a {@link HttpHeaderType} cannot be parsed from the string value.
     */
    public static HttpHeaderType fromString(final String value) {
        for (HttpHeaderType httpHeaderType : values()) {
            if (httpHeaderType.value.equalsIgnoreCase(value)) {
                return httpHeaderType;
            }
        }

        throw new IllegalArgumentException(String.format(Locale.US, "Could not convert %s to a HttpHeaderType", value));
    }
}
