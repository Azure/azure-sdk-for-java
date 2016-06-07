/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A reference to a certificate to be installed on compute nodes in a pool.
 */
public class CertificateReference {
    /**
     * The thumbprint of the certificate.
     */
    @JsonProperty(required = true)
    private String thumbprint;

    /**
     * The algorithm with which the thumbprint is associated. This must be
     * sha1.
     */
    @JsonProperty(required = true)
    private String thumbprintAlgorithm;

    /**
     * The location of the certificate store on the compute node into which to
     * install the certificate. The default value is CurrentUser. Possible
     * values include: 'currentuser', 'localmachine', 'unmapped'.
     */
    private CertificateStoreLocation storeLocation;

    /**
     * The name of the certificate store on the compute node into which to
     * install the certificate. The default value is My.
     */
    private String storeName;

    /**
     * Which user accounts on the compute node should have access to the
     * private data of the certificate. This may be any subset of the values
     * 'starttask', 'task' and 'remoteuser', separated by commas. The default
     * is all accounts, corresponding to the string
     * 'starttask,task,remoteuser'.
     */
    private List<CertificateVisibility> visibility;

    /**
     * Get the thumbprint value.
     *
     * @return the thumbprint value
     */
    public String thumbprint() {
        return this.thumbprint;
    }

    /**
     * Set the thumbprint value.
     *
     * @param thumbprint the thumbprint value to set
     * @return the CertificateReference object itself.
     */
    public CertificateReference withThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
        return this;
    }

    /**
     * Get the thumbprintAlgorithm value.
     *
     * @return the thumbprintAlgorithm value
     */
    public String thumbprintAlgorithm() {
        return this.thumbprintAlgorithm;
    }

    /**
     * Set the thumbprintAlgorithm value.
     *
     * @param thumbprintAlgorithm the thumbprintAlgorithm value to set
     * @return the CertificateReference object itself.
     */
    public CertificateReference withThumbprintAlgorithm(String thumbprintAlgorithm) {
        this.thumbprintAlgorithm = thumbprintAlgorithm;
        return this;
    }

    /**
     * Get the storeLocation value.
     *
     * @return the storeLocation value
     */
    public CertificateStoreLocation storeLocation() {
        return this.storeLocation;
    }

    /**
     * Set the storeLocation value.
     *
     * @param storeLocation the storeLocation value to set
     * @return the CertificateReference object itself.
     */
    public CertificateReference withStoreLocation(CertificateStoreLocation storeLocation) {
        this.storeLocation = storeLocation;
        return this;
    }

    /**
     * Get the storeName value.
     *
     * @return the storeName value
     */
    public String storeName() {
        return this.storeName;
    }

    /**
     * Set the storeName value.
     *
     * @param storeName the storeName value to set
     * @return the CertificateReference object itself.
     */
    public CertificateReference withStoreName(String storeName) {
        this.storeName = storeName;
        return this;
    }

    /**
     * Get the visibility value.
     *
     * @return the visibility value
     */
    public List<CertificateVisibility> visibility() {
        return this.visibility;
    }

    /**
     * Set the visibility value.
     *
     * @param visibility the visibility value to set
     * @return the CertificateReference object itself.
     */
    public CertificateReference withVisibility(List<CertificateVisibility> visibility) {
        this.visibility = visibility;
        return this;
    }

}
