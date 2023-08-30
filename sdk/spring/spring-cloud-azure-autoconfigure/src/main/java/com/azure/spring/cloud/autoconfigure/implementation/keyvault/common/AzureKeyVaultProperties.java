// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.common;

import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;

/**
 * Azure Key Vault properties.
 *
 * @since 4.0.0
 */
public class AzureKeyVaultProperties extends AbstractAzureHttpConfigurationProperties {

    public static final String PREFIX = "spring.cloud.azure.keyvault";

    /**
     * Azure Key Vault endpoint. For instance, 'https://{your-unique-keyvault-name}.vault.azure.net/'.
     */
    private String endpoint;

    private boolean disableChallengeResourceVerification = false;

    /**
     *
     * @return The Azure Key Vault endpoint.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     *
     * @param endpoint The Azure Key Vault endpoint.
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     *
     * @return The value of the disableChallengeResourceVerification property of the Azure Key Vault Client
     */
    public boolean isDisableChallengeResourceVerification() {
        return disableChallengeResourceVerification;
    }

    /**
     *
     * @param disableChallengeResourceVerification The value of the disableChallengeResourceVerification property of the Azure Key Vault Client
     */
    public void setDisableChallengeResourceVerification(
        boolean disableChallengeResourceVerification) {
        this.disableChallengeResourceVerification = disableChallengeResourceVerification;
    }
}
