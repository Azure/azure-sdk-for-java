/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;

/**
 * An error encountered by the Batch service when deleting a certificate.
 */
public class DeleteCertificateError {
    /**
     * An identifier for the certificate deletion error. Codes are invariant
     * and are intended to be consumed programmatically.
     */
    private String code;

    /**
     * A message describing the certificate deletion error, intended to be
     * suitable for display in a user interface.
     */
    private String message;

    /**
     * A list of additional error details related to the certificate deletion
     * error.
     */
    private List<NameValuePair> values;

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public String code() {
        return this.code;
    }

    /**
     * Set the code value.
     *
     * @param code the code value to set
     * @return the DeleteCertificateError object itself.
     */
    public DeleteCertificateError withCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String message() {
        return this.message;
    }

    /**
     * Set the message value.
     *
     * @param message the message value to set
     * @return the DeleteCertificateError object itself.
     */
    public DeleteCertificateError withMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Get the values value.
     *
     * @return the values value
     */
    public List<NameValuePair> values() {
        return this.values;
    }

    /**
     * Set the values value.
     *
     * @param values the values value to set
     * @return the DeleteCertificateError object itself.
     */
    public DeleteCertificateError withValues(List<NameValuePair> values) {
        this.values = values;
        return this;
    }

}
