/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.mysql.v2020_01_01.implementation;

import com.microsoft.azure.management.mysql.v2020_01_01.PrivateLinkResourceProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.ProxyResource;

/**
 * A private link resource.
 */
public class PrivateLinkResourceInner extends ProxyResource {
    /**
     * The private link resource group id.
     */
    @JsonProperty(value = "properties", access = JsonProperty.Access.WRITE_ONLY)
    private PrivateLinkResourceProperties properties;

    /**
     * Get the private link resource group id.
     *
     * @return the properties value
     */
    public PrivateLinkResourceProperties properties() {
        return this.properties;
    }

}
