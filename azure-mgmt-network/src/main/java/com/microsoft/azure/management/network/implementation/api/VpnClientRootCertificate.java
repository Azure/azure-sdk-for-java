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
 * VPN client root certificate of virtual network gateway.
 */
@JsonFlatten
public class VpnClientRootCertificate extends SubResource {
    /**
     * Gets or sets the certificate public data.
     */
    @JsonProperty(value = "properties.publicCertData")
    private String publicCertData;

    /**
     * Gets or sets Provisioning state of the VPN client root certificate
     * resource Updating/Deleting/Failed.
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
     * @return the VpnClientRootCertificate object itself.
     */
    public VpnClientRootCertificate withPublicCertData(String publicCertData) {
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
     * @return the VpnClientRootCertificate object itself.
     */
    public VpnClientRootCertificate withProvisioningState(String provisioningState) {
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
     * @return the VpnClientRootCertificate object itself.
     */
    public VpnClientRootCertificate withName(String name) {
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
     * @return the VpnClientRootCertificate object itself.
     */
    public VpnClientRootCertificate withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
