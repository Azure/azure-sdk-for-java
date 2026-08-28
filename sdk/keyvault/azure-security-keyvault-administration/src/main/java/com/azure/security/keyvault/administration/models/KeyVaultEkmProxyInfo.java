// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

/**
 * EKM proxy information returned when checking an External Key Manager (EKM) connection.
 */
@Immutable
public final class KeyVaultEkmProxyInfo {
    private final String apiVersion;
    private final String proxyVendor;
    private final String proxyName;
    private final String ekmVendor;
    private final String ekmProduct;

    /**
     * Creates a new {@link KeyVaultEkmProxyInfo} with the specified details.
     *
     * @param apiVersion The highest version of proxy interface API supported by the EKM proxy.
     * @param proxyVendor The name of the proxy vendor.
     * @param proxyName The name of the proxy product and its version.
     * @param ekmVendor The name of the EKM vendor.
     * @param ekmProduct The name of the EKM product and its version.
     */
    public KeyVaultEkmProxyInfo(String apiVersion, String proxyVendor, String proxyName, String ekmVendor,
        String ekmProduct) {
        this.apiVersion = apiVersion;
        this.proxyVendor = proxyVendor;
        this.proxyName = proxyName;
        this.ekmVendor = ekmVendor;
        this.ekmProduct = ekmProduct;
    }

    /**
     * Get the highest version of proxy interface API supported by the EKM proxy.
     *
     * @return The API version.
     */
    public String getApiVersion() {
        return this.apiVersion;
    }

    /**
     * Get the name of the proxy vendor.
     *
     * @return The proxy vendor.
     */
    public String getProxyVendor() {
        return this.proxyVendor;
    }

    /**
     * Get the name of the proxy product and its version.
     *
     * @return The proxy name.
     */
    public String getProxyName() {
        return this.proxyName;
    }

    /**
     * Get the name of the EKM vendor.
     *
     * @return The EKM vendor.
     */
    public String getEkmVendor() {
        return this.ekmVendor;
    }

    /**
     * Get the name of the EKM product and its version.
     *
     * @return The EKM product.
     */
    public String getEkmProduct() {
        return this.ekmProduct;
    }
}
