/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.compute.v2020_10_01_preview;

import com.microsoft.azure.management.compute.v2020_10_01_preview.implementation.RoleInstanceInstanceViewInner;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The RoleInstanceProperties model.
 */
public class RoleInstanceProperties {
    /**
     * The networkProfile property.
     */
    @JsonProperty(value = "networkProfile")
    private RoleInstanceNetworkProfile networkProfile;

    /**
     * The instanceView property.
     */
    @JsonProperty(value = "instanceView")
    private RoleInstanceInstanceViewInner instanceView;

    /**
     * Get the networkProfile value.
     *
     * @return the networkProfile value
     */
    public RoleInstanceNetworkProfile networkProfile() {
        return this.networkProfile;
    }

    /**
     * Set the networkProfile value.
     *
     * @param networkProfile the networkProfile value to set
     * @return the RoleInstanceProperties object itself.
     */
    public RoleInstanceProperties withNetworkProfile(RoleInstanceNetworkProfile networkProfile) {
        this.networkProfile = networkProfile;
        return this;
    }

    /**
     * Get the instanceView value.
     *
     * @return the instanceView value
     */
    public RoleInstanceInstanceViewInner instanceView() {
        return this.instanceView;
    }

    /**
     * Set the instanceView value.
     *
     * @param instanceView the instanceView value to set
     * @return the RoleInstanceProperties object itself.
     */
    public RoleInstanceProperties withInstanceView(RoleInstanceInstanceViewInner instanceView) {
        this.instanceView = instanceView;
        return this;
    }

}
