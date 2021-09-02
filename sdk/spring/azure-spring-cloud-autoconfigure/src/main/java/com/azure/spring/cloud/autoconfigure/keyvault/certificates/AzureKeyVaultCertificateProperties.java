// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.certificates;

import com.azure.spring.cloud.autoconfigure.keyvault.AzureKeyVaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for Azure Key Vault Certificate.
 */
@ConfigurationProperties(prefix = "spring.cloud.azure.keyvault.certificate")
public class AzureKeyVaultCertificateProperties extends AzureKeyVaultProperties {

}
