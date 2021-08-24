// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

/**
 * Interface to be implemented that enables returning of Secrets instead of requesting them from Azure Key Vaults.
 */
public interface KeyVaultSecretProvider {

    String getSecret(String uri);

}
