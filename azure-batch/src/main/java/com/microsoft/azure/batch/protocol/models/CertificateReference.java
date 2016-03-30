/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A reference to a certificate to be installed on compute nodes in a pool.
 */
public class CertificateReference {
    /**
     * Gets or sets the thumbprint of the certificate.
     */
    @JsonProperty(required = true)
    private String thumbprint;

    /**
     * Gets or sets the algorithm with which the thumbprint is associated.
     * This must be sha1.
     */
    @JsonProperty(required = true)
    private String thumbprintAlgorithm;

    /**
     * Gets or sets the location of the certificate store on the compute node
     * into which to install the certificate. The default value is
     * CurrentUser. Possible values include: 'currentuser', 'localmachine',
     * 'unmapped'.
     */
    private CertificateStoreLocation storeLocation;

    /**
     * Gets or sets the name of the certificate store on the compute node into
     * which to install the certificate. The default value is My.
     */
    private String storeName;

    /**
     * Gets or sets which user accounts on the compute node should have access
     * to the private data of the certificate. This may be any subset of the
     * values 'starttask', 'task' and 'rdp', separated by commas. The default
     * is all accounts, corresponding to the string 'starttask,task,rdp'.
     */
    private String visibility;

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
     * Get the storeLocation value.
     *
     * @return the storeLocation value
     */
    public CertificateStoreLocation getStoreLocation() {
        return this.storeLocation;
    }

    /**
     * Set the storeLocation value.
     *
     * @param storeLocation the storeLocation value to set
     */
    public void setStoreLocation(CertificateStoreLocation storeLocation) {
        this.storeLocation = storeLocation;
    }

    /**
     * Get the storeName value.
     *
     * @return the storeName value
     */
    public String getStoreName() {
        return this.storeName;
    }

    /**
     * Set the storeName value.
     *
     * @param storeName the storeName value to set
     */
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    /**
     * Get the visibility value.
     *
     * @return the visibility value
     */
    public String getVisibility() {
        return this.visibility;
    }

    /**
     * Set the visibility value.
     *
     * @param visibility the visibility value to set
     */
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

}
