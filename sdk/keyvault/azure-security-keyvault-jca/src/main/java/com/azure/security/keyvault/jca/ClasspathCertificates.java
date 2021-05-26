// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Store certificates loaded from classpath.
 */
public class ClasspathCertificates implements AzureCertificates {

    /**
     * Store certificates' alias.
     */
    private final List<String> aliases = new ArrayList<>();

    /**
     * Stores the certificates by alias.
     */
    private final Map<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the certificate keys by alias.
     */
    private final Map<String, Key> certificateKeys = new HashMap<>();

    /**
     * Get certificate aliases.
     * @return certificate aliases
     */
    @Override
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Get certificates.
     * @return certificates
     */
    @Override
    public Map<String, Certificate> getCertificates() {
        return certificates;
    }

    /**
     * Get certificate keys.
     * @return certificate keys
     */
    @Override
    public Map<String, Key> getCertificateKeys() {
        return certificateKeys;
    }

    /**
     * Remove alias if exist.
     * @param alias certificate alias
     */
    public void removeAlias(String alias) {
        aliases.remove(alias);
    }

    /**
     * Remove certificate if exist.
     * @param alias certificate alias
     */
    public void removeCertificate(String alias) {
        certificates.remove(alias);
    }

    /**
     * Add certificate.
     * @param alias certificate alias
     * @param certificate certificate
     */
    public void setCertificateEntry(String alias, Certificate certificate) {
        if (!aliases.contains(alias)) {
            aliases.add(alias);
            certificates.put(alias, certificate);
        }
    }

    /**
     * Delete certificate info by alias if exits
     * @param alias certificate alias
     */
    @Override
    public void deleteEntry(String alias) {
        aliases.remove(alias);
        certificates.remove(alias);
        certificateKeys.remove(alias);
    }

}
