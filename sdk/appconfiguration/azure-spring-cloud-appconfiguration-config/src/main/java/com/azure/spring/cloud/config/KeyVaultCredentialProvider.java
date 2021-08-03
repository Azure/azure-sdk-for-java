// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import com.azure.core.credential.TokenCredential;

/**
 * Interface to be implemented that enables returning of a TokenCredential for authentication with an Azure Key Vaults.
 */
public interface KeyVaultCredentialProvider {

    TokenCredential getKeyVaultCredential(String uri);

}
