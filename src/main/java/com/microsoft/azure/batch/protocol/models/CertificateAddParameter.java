/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A certificate that can be installed on compute nodes and can be used to
 * authenticate operations on the machine.
 */
public class CertificateAddParameter {
    /**
     * Get or sets the X.509 thumbprint of the certificate. This is a sequence
     * of up to 40 hex digits (it may include spaces but these are removed).
     */
    @JsonProperty(required = true)
    private String thumbprint;

    /**
     * Gets or sets the algorithm used to derive the thumbprint. This must be
     * sha1.
     */
    @JsonProperty(required = true)
    private String thumbprintAlgorithm;

    /**
     * Gets or sets the base64-encoded contents of the certificate. The
     * maximum size is 10KB. This property is not populated by the Get
     * Certificate operation.
     */
    @JsonProperty(required = true)
    private String data;

    /**
     * Gets or sets the format of the certificate data. Possible values
     * include: 'pfx', 'cer', 'unmapped'.
     */
    private CertificateFormat certificateFormat;

    /**
     * Gets or sets the password to access the certificate's private key. This
     * property is not populated by the Get Certificate operation.
     */
    private String password;

    /**
     * Get the thumbprint value.
     *
     * @return the thumbprint value
     */
    public String getThumbprint() {
        return this.thumbprint;
    }

    /**
     * Set the thumbprint value.
     *
     * @param thumbprint the thumbprint value to set
     */
    public void setThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
    }

    /**
     * Get the thumbprintAlgorithm value.
     *
     * @return the thumbprintAlgorithm value
     */
    public String getThumbprintAlgorithm() {
        return this.thumbprintAlgorithm;
    }

    /**
     * Set the thumbprintAlgorithm value.
     *
     * @param thumbprintAlgorithm the thumbprintAlgorithm value to set
     */
    public void setThumbprintAlgorithm(String thumbprintAlgorithm) {
        this.thumbprintAlgorithm = thumbprintAlgorithm;
    }

    /**
     * Get the data value.
     *
     * @return the data value
     */
    public String getData() {
        return this.data;
    }

    /**
     * Set the data value.
     *
     * @param data the data value to set
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Get the certificateFormat value.
     *
     * @return the certificateFormat value
     */
    public CertificateFormat getCertificateFormat() {
        return this.certificateFormat;
    }

    /**
     * Set the certificateFormat value.
     *
     * @param certificateFormat the certificateFormat value to set
     */
    public void setCertificateFormat(CertificateFormat certificateFormat) {
        this.certificateFormat = certificateFormat;
    }

    /**
     * Get the password value.
     *
     * @return the password value
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the password value.
     *
     * @param password the password value to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
