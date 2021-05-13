// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Date;
import java.util.Collections;
import java.util.Objects;

/**
 * Store certificates in portal
 */
public class KeyVaultCertificatesInfo {

    /**
     * Stores the list of aliases.
     */
    private List<String> aliases;

    /**
     * Stores the certificates by alias.
     */
    private final Map<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the certificate keys by alias.
     */
    private final HashMap<String, Key> certificateKeys = new HashMap<>();

    /**
     * Stores the last time refresh certificates and alias
     */
    private Date lastRefreshTime;

    /**
     * Stores the overall refresh time.
     */
    private static Date overallRefreshTime = new Date();

    private final long refreshInterval = Optional.ofNullable(System.getProperty("azure.keyvault.jca.certificates-refresh-interval"))
        .map(Long::valueOf)
        .orElse(0L);

    boolean certificatesNeedRefresh() {
        if (overallRefreshTime.after(lastRefreshTime)) {
            return true;
        }
        if (refreshInterval > 0) {
            return lastRefreshTime.getTime() + refreshInterval < new Date().getTime();
        }
        return false;
    }

    List<String> getAliases(KeyVaultClient keyVaultClient) {
        if (lastRefreshTime == null || certificatesNeedRefresh()) {
            refreshCertificates(keyVaultClient);
        }
        return aliases;
    }

    Map<String, Certificate> getCertificates(KeyVaultClient keyVaultClient) {
        if (lastRefreshTime == null || certificatesNeedRefresh()) {
            refreshCertificates(keyVaultClient);
        }
        return certificates;
    }

    Map<String, Key> getCertificateKeys(KeyVaultClient keyVaultClient) {
        if (lastRefreshTime == null || certificatesNeedRefresh()) {
            refreshCertificates(keyVaultClient);
        }
        return certificateKeys;
    }

    void refreshCertificates(KeyVaultClient keyVaultClient) {
        aliases = keyVaultClient.getAliases();
        certificateKeys.clear();
        certificates.clear();
        Optional.ofNullable(aliases)
            .orElse(Collections.emptyList())
            .forEach(alias -> {
                Key key = keyVaultClient.getKey(alias, null);
                Certificate certificate = keyVaultClient.getCertificate(alias);
                if (!Objects.isNull(key) && !Objects.isNull(certificate)) {
                    certificateKeys.put(alias, key);
                    certificates.put(alias, certificate);
                }
            });
        lastRefreshTime = new Date();
    }

    /**
     *  get latest alias by certificate which in portal
     * @param certificate certificate getted
     * @param keyVaultClient keyVaultClient in keyStore
     * @return certificate's alias if exist.
     */
    public String getAliasByCertInTime(Certificate certificate, KeyVaultClient keyVaultClient) {
        refreshCertificates(keyVaultClient);
        String key = "";
        for (Map.Entry<String, Certificate> entry : certificates.entrySet()) {
            if (certificate.equals(entry.getValue())) {
                key = entry.getKey();
            }
        }
        refreshCertsInfo();
        return key;
    }

    /**
     * overall refresh certificates's info
     */
    public static void refreshCertsInfo() {
        overallRefreshTime = new Date();
    }

}
