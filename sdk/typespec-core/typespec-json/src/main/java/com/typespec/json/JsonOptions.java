// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

/**
 * Contains configuration options for creating a {@link JsonReader} or {@link JsonWriter}.
 *
 * @see com.typespec.json
 * @see JsonProvider
 * @see JsonProviders
 */
public final class JsonOptions {

    private boolean nonNumericNumbersSupported = true;

    /**
     * Creates an instance of {@link JsonOptions}.
     */
    public JsonOptions() {
    }

    /**
     * Whether non-numeric numbers such as {@code NaN} and {@code INF} and {@code -INF} are supported.
     * <p>
     * By default, this is configured to true.
     *
     * @return Whether non-numeric numbers are supported.
     */
    public boolean isNonNumericNumbersSupported() {
        return nonNumericNumbersSupported;
    }

    /**
     * Sets whether non-numeric numbers such as {@code NaN} and {@code INF} and {@code -INF} are supported.
     * <p>
     * By default, this is configured to true.
     *
     * @param nonNumericNumbersSupported Whether non-numeric numbers are supported.
     * @return The updated JsonOptions object.
     */
    public JsonOptions setNonNumericNumbersSupported(boolean nonNumericNumbersSupported) {
        this.nonNumericNumbersSupported = nonNumericNumbersSupported;
        return this;
    }
}
