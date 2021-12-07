// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.api;

/**
 * An interface meant to be implemented by configuration properties POJOs that store information about Azure
 * credentials.
 *
 * @author Warren Zhu
 */
public interface CredentialSupplier {

    /**
     * Gets the client ID.
     *
     * @return The client ID.
     */
    String getClientId();

    /**
     * Gets the client secret.
     *
     * @return The client secret.
     */
    String getClientSecret();

    /**
     * Whether MSI is enabled.
     *
     * @return Whether MSI is enabled.
     */
    boolean isMsiEnabled();

    /**
     * Gets the tenant ID.
     *
     * @return The tenant ID.
     */
    String getTenantId();

    /**
     * Gets the subscription ID.
     *
     * @return The subscription ID.
     */
    String getSubscriptionId();
}
