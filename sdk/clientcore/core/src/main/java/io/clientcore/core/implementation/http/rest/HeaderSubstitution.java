// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeaderName;

public final class HeaderSubstitution extends Substitution {
    private final HttpHeaderName headerName;

    /**
     * Create a new Substitution.
     *
     * @param uriParameterName The name that is used between curly quotes as a placeholder in the target URI.
     * @param methodParameterIndex The index of the parameter in the original interface method where the value for the
     * placeholder is.
     * @param shouldEncode Whether the value from the method's argument should be encoded when the substitution is
     * taking place.
     */
    public HeaderSubstitution(String uriParameterName, int methodParameterIndex, boolean shouldEncode) {
        super(uriParameterName, methodParameterIndex, shouldEncode);
        this.headerName = (uriParameterName == null) ? null : HttpHeaderName.fromString(uriParameterName);
    }

    /**
     * Gets the header name.
     * <p>
     * header name.
     *
     * @return The header name.
     */
    public HttpHeaderName getHeaderName() {
        return headerName;
    }
}
