// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.keyvault.certificates;

import com.azure.security.keyvault.certificates.CertificateServiceVersion;
import com.azure.spring.service.keyvault.KeyVaultProperties;

/**
 * Properties for Azure Key Vault Certificate.
 */
public interface CertificateClientProperties extends KeyVaultProperties {

    CertificateServiceVersion getServiceVersion();

}
