// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

/**
 * Contains configuration options for creating a {@link JsonReader} or {@link JsonWriter}.
 *
 * @see com.azure.json
 * @see JsonProvider
 * @see JsonProviders
 */
public final class JsonOptions {

    private boolean nonNumericNumbersSupported = true;
    private boolean allowComments;
    private boolean allowTrailingCommas;
    private boolean allowUnescapedControlCharacters;

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

    /**
     * Whether comments are allowed in JSON being deserialized.
     * <p>
     * This only effects deserialization.
     * <p>
     * By default, this is configured to false.
     *
     * @return Whether comments are allowed in JSON being deserialized.
     */
    public boolean isAllowComments() {
        return allowComments;
    }

    /**
     * Sets whether comments are allowed in JSON being deserialized.
     * <p>
     * This only effects deserialization.
     * <p>
     * By default, this is configured to false.
     *
     * @param allowComments Whether comments are allowed in JSON being deserialized.
     * @return The updated JsonOptions object.
     */
    public JsonOptions setAllowComments(boolean allowComments) {
        this.allowComments = allowComments;
        return this;
    }

    /**
     * Whether trailing commas are allowed in JSON being deserialized.
     * <p>
     * This only effects deserialization.
     * <p>
     * By default, this is configured to false.
     *
     * @return Whether trailing commas are allowed in JSON being deserialized.
     */
    public boolean isAllowTrailingCommas() {
        return allowTrailingCommas;
    }

    /**
     * Sets whether trailing commas are allowed in JSON being deserialized.
     * <p>
     * This only effects deserialization.
     * <p>
     * By default, this is configured to false.
     *
     * @param allowTrailingCommas Whether trailing commas are allowed in JSON being deserialized.
     * @return The updated JsonOptions object.
     */
    public JsonOptions setAllowTrailingCommas(boolean allowTrailingCommas) {
        this.allowTrailingCommas = allowTrailingCommas;
        return this;
    }

    /**
     * Whether unescaped control characters are allowed in JSON being deserialized.
     * <p>
     * This only effects deserialization.
     * <p>
     * By default, this is configured to false.
     *
     * @return Whether unescaped control characters are allowed in JSON being deserialized.
     */
    public boolean isAllowUnescapedControlCharacters() {
        return allowUnescapedControlCharacters;
    }

    /**
     * Sets whether unescaped control characters are allowed in JSON being deserialized.
     * <p>
     * This only effects deserialization.
     * <p>
     * By default, this is configured to false.
     *
     * @param allowUnescapedControlCharacters Whether unescaped control characters are allowed in JSON being
     * deserialized.
     * @return The updated JsonOptions object.
     */
    public JsonOptions setAllowUnescapedControlCharacters(boolean allowUnescapedControlCharacters) {
        this.allowUnescapedControlCharacters = allowUnescapedControlCharacters;
        return this;
    }
}
