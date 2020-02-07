// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the error in {@link CertificateOperation}.
 */
public final class CertificateOperationError {
    /**
     * The error code.
     */
    @JsonProperty(value = "code", access = JsonProperty.Access.WRITE_ONLY)
    private String code;

    /**
     * The error message.
     */
    @JsonProperty(value = "message", access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * The getInnerError property.
     */
    @JsonProperty(value = "innererror", access = JsonProperty.Access.WRITE_ONLY)
    private CertificateOperationError innerError;

    /**
     * Get the code.
     *
     * @return the code
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message.
     *
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the inner error.
     *
     * @return the inner error
     */
    public CertificateOperationError getInnerError() {
        return this.innerError;
    }

}
