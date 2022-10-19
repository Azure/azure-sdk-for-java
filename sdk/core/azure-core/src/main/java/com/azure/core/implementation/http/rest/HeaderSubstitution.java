// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.http.rest;

import com.azure.core.http.HttpHeaders;

import java.util.Locale;

public final class HeaderSubstitution extends Substitution {
    private final String lowerCaseHeaderName;

    /**
     * Create a new Substitution.
     *
     * @param urlParameterName The name that is used between curly quotes as a placeholder in the target URL.
     * @param methodParameterIndex The index of the parameter in the original interface method where the value for the
     * placeholder is.
     * @param shouldEncode Whether the value from the method's argument should be encoded when the substitution is
     * taking place.
     */
    public HeaderSubstitution(String urlParameterName, int methodParameterIndex, boolean shouldEncode) {
        super(urlParameterName, methodParameterIndex, shouldEncode);
        this.lowerCaseHeaderName = (urlParameterName == null) ? null : urlParameterName.toLowerCase(Locale.ROOT);
    }

    /**
     * Gets the lower cased header name.
     * <p>
     * This enables a safe optimization for {@link HttpHeaders} where this substitution can bypass lower casing the
     * header name.
     *
     * @return The lower cased header name.
     */
    public String getLowerCaseHeaderName() {
        return lowerCaseHeaderName;
    }
}
