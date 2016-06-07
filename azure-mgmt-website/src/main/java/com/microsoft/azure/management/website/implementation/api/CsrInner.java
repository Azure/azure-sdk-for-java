/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Certificate signing request object.
 */
@JsonFlatten
public class CsrInner extends Resource {
    /**
     * Name used to locate CSR object.
     */
    @JsonProperty(value = "properties.name")
    private String csrName;

    /**
     * Distinguished name of certificate to be created.
     */
    @JsonProperty(value = "properties.distinguishedName")
    private String distinguishedName;

    /**
     * Actual CSR string created.
     */
    @JsonProperty(value = "properties.csrString")
    private String csrString;

    /**
     * PFX certifcate of created certificate.
     */
    @JsonProperty(value = "properties.pfxBlob")
    private String pfxBlob;

    /**
     * PFX password.
     */
    @JsonProperty(value = "properties.password")
    private String password;

    /**
     * Hash of the certificates public key.
     */
    @JsonProperty(value = "properties.publicKeyHash")
    private String publicKeyHash;

    /**
     * Hosting environment.
     */
    @JsonProperty(value = "properties.hostingEnvironment")
    private String hostingEnvironment;

    /**
     * Get the csrName value.
     *
     * @return the csrName value
     */
    public String csrName() {
        return this.csrName;
    }

    /**
     * Set the csrName value.
     *
     * @param csrName the csrName value to set
     * @return the CsrInner object itself.
     */
    public CsrInner withCsrName(String csrName) {
        this.csrName = csrName;
        return this;
    }

    /**
     * Get the distinguishedName value.
     *
     * @return the distinguishedName value
     */
    public String distinguishedName() {
        return this.distinguishedName;
    }

    /**
     * Set the distinguishedName value.
     *
     * @param distinguishedName the distinguishedName value to set
     * @return the CsrInner object itself.
     */
    public CsrInner withDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
        return this;
    }

    /**
     * Get the csrString value.
     *
     * @return the csrString value
     */
    public String csrString() {
        return this.csrString;
    }

    /**
     * Set the csrString value.
     *
     * @param csrString the csrString value to set
     * @return the CsrInner object itself.
     */
    public CsrInner withCsrString(String csrString) {
        this.csrString = csrString;
        return this;
    }

    /**
     * Get the pfxBlob value.
     *
     * @return the pfxBlob value
     */
    public String pfxBlob() {
        return this.pfxBlob;
    }

    /**
     * Set the pfxBlob value.
     *
     * @param pfxBlob the pfxBlob value to set
     * @return the CsrInner object itself.
     */
    public CsrInner withPfxBlob(String pfxBlob) {
        this.pfxBlob = pfxBlob;
        return this;
    }

    /**
     * Get the password value.
     *
     * @return the password value
     */
    public String password() {
        return this.password;
    }

    /**
     * Set the password value.
     *
     * @param password the password value to set
     * @return the CsrInner object itself.
     */
    public CsrInner withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the publicKeyHash value.
     *
     * @return the publicKeyHash value
     */
    public String publicKeyHash() {
        return this.publicKeyHash;
    }

    /**
     * Set the publicKeyHash value.
     *
     * @param publicKeyHash the publicKeyHash value to set
     * @return the CsrInner object itself.
     */
    public CsrInner withPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
        return this;
    }

    /**
     * Get the hostingEnvironment value.
     *
     * @return the hostingEnvironment value
     */
    public String hostingEnvironment() {
        return this.hostingEnvironment;
    }

    /**
     * Set the hostingEnvironment value.
     *
     * @param hostingEnvironment the hostingEnvironment value to set
     * @return the CsrInner object itself.
     */
    public CsrInner withHostingEnvironment(String hostingEnvironment) {
        this.hostingEnvironment = hostingEnvironment;
        return this;
    }

}
