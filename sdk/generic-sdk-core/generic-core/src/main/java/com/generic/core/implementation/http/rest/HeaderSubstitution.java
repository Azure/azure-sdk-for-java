// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.models.HeaderName;

public final class HeaderSubstitution extends Substitution {
    private final HeaderName headerName;

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
        this.headerName = (urlParameterName == null) ? null : HeaderName.fromString(urlParameterName);
    }

    /**
     * Gets the header name.
     * <p>
     * header name.
     *
     * @return The header name.
     */
    public HeaderName getHeaderName() {
        return headerName;
    }
}
