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
 * Store side load certificates information.
 */
public class ClasspathCertificates {

    /**
     * Stores the side load aliases.
     */
    private final List<String> aliases = new ArrayList<>();

    /**
     * Stores the sideLoad certificates by alias.
     */
    private final Map<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the certificate keys by alias.
     */
    private final HashMap<String, Key> certificateKeys = new HashMap<>();

    /**
     * Get side load certificates' aliases.
     * @return certificates' alias
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Get side load certificates.
     * @return side load certificatess
     */
    public Map<String, Certificate> getCertificates() {
        return certificates;
    }

    /**
     * Get side load certificate keys.
     * @return side load certificate keys
     */
    public HashMap<String, Key> getCertificateKeys() {
        return certificateKeys;
    }

    /**
     * Remove side load alias if exist.
     * @param alias certificate alias
     */
    public void removeAlias(String alias) {
        aliases.remove(alias);
    }

    /**
     * Remove certificate in side load if exist.
     * @param alias certificate alias
     */
    public void removeCertificate(String alias) {
        certificates.remove(alias);
    }

    /**
     * Remove certificate key in side load if exist.
     * @param alias certificate alias
     */
    public void removeCertificateKey(String alias) {
        certificateKeys.remove(alias);
    }

    /**
     * Add certificates to side load.
     * @param alias certificate alias
     * @param certificate certificate
     */
    public void setCertificateEntry(String alias, Certificate certificate) {
        if (!aliases.contains(alias)) {
            aliases.add(alias);
            certificates.put(alias, certificate);
        }
    }
}
