/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.SubResource;

/**
 * SSL certificates of application gateway.
 */
@JsonFlatten
public class ApplicationGatewaySslCertificate extends SubResource {
    /**
     * Gets or sets the certificate data.
     */
    @JsonProperty(value = "properties.data")
    private String data;

    /**
     * Gets or sets the certificate password.
     */
    @JsonProperty(value = "properties.password")
    private String password;

    /**
     * Gets or sets the certificate public data.
     */
    @JsonProperty(value = "properties.publicCertData")
    private String publicCertData;

    /**
     * Gets or sets Provisioning state of the ssl certificate resource
     * Updating/Deleting/Failed.
     */
    @JsonProperty(value = "properties.provisioningState")
    private String provisioningState;

    /**
     * Gets name of the resource that is unique within a resource group. This
     * name can be used to access the resource.
     */
    private String name;

    /**
     * A unique read-only string that changes whenever the resource is updated.
     */
    private String etag;

    /**
     * Get the data value.
     *
     * @return the data value
     */
    public String data() {
        return this.data;
    }

    /**
     * Set the data value.
     *
     * @param data the data value to set
     * @return the ApplicationGatewaySslCertificate object itself.
     */
    public ApplicationGatewaySslCertificate withData(String data) {
        this.data = data;
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
     * @return the ApplicationGatewaySslCertificate object itself.
     */
    public ApplicationGatewaySslCertificate withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the publicCertData value.
     *
     * @return the publicCertData value
     */
    public String publicCertData() {
        return this.publicCertData;
    }

    /**
     * Set the publicCertData value.
     *
     * @param publicCertData the publicCertData value to set
     * @return the ApplicationGatewaySslCertificate object itself.
     */
    public ApplicationGatewaySslCertificate withPublicCertData(String publicCertData) {
        this.publicCertData = publicCertData;
        return this;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the ApplicationGatewaySslCertificate object itself.
     */
    public ApplicationGatewaySslCertificate withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the ApplicationGatewaySslCertificate object itself.
     */
    public ApplicationGatewaySslCertificate withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the etag value.
     *
     * @return the etag value
     */
    public String etag() {
        return this.etag;
    }

    /**
     * Set the etag value.
     *
     * @param etag the etag value to set
     * @return the ApplicationGatewaySslCertificate object itself.
     */
    public ApplicationGatewaySslCertificate withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
