// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.List;
import java.util.Map;

/**
 * Store Azure Certificates
 */
public interface AzureCertificates {

    /**
     * Get certificate aliases.
     * @return certificate aliases
     */
    List<String> getAliases();

    /**
     * Get certificates.
     * @return certificates
     */
    Map<String, Certificate> getCertificates();

    /**
     * Get certificate keys.
     * @return certificate keys
     */
    Map<String, Key> getCertificateKeys();

    /**
     * Delete certificate info by alias if exits
     * @param alias certificate alias
     */
    void deleteEntry(String alias);
}
