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
     * Gets or sets an identifier for the certificate deletion error.  Codes
     * are invariant and are intended to be consumed programmatically.
     */
    private String code;

    /**
     * Gets or sets a message describing the certificate deletion error,
     * intended to be suitable for display in a user interface.
     */
    private String message;

    /**
     * Gets or sets a list of additional error details related to the
     * certificate deletion error.
     */
    private List<NameValuePair> values;

    /**
     * Get the code value.
     *
     * @return the code value
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Set the code value.
     *
     * @param code the code value to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Get the message value.
     *
     * @return the message value
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message value.
     *
     * @param message the message value to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Get the values value.
     *
     * @return the values value
     */
    public List<NameValuePair> getValues() {
        return this.values;
    }

    /**
     * Set the values value.
     *
     * @param values the values value to set
     */
    public void setValues(List<NameValuePair> values) {
        this.values = values;
    }

}
