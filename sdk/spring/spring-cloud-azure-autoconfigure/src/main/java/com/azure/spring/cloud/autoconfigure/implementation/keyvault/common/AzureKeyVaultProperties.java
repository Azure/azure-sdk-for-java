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

    /**
     * Whether to enable the Azure Key Vault challenge resource verification, default: true.
     * Calls the disableChallengeResourceVerification method of the Azure Key Vault Client Builder when set to false.
     */
    private boolean challengeResourceVerificationEnabled = true;

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
     * @return Whether we should keep the challenge resource verification for the Azure Key Vault Client
     */
    public boolean isChallengeResourceVerificationEnabled() {
        return challengeResourceVerificationEnabled;
    }

    /**
     *
     * @param challengeResourceVerificationEnabled Whether we should keep Azure Key Vault challenge resource verification enabled
     */
    public void setChallengeResourceVerificationEnabled(
        boolean challengeResourceVerificationEnabled) {
        this.challengeResourceVerificationEnabled = challengeResourceVerificationEnabled;
    }
}
