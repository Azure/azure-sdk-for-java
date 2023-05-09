// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config;

/**
 * Interface to be implemented that enables returning of Secrets instead of requesting them from Azure Key Vaults.
 */
public interface KeyVaultSecretProvider {

    /**
     * Returns a secret value for a given endpoint
     * @param endpoint Key Vault Reference
     * @return String value of the secret
     */
    String getSecret(String endpoint);

}
