// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.Key;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Store certificates loaded from KeyVault.
 */
public class KeyVaultCertificates implements AzureCertificates {

    /**
     * Stores the list of aliases.
     */
    private List<String> aliases = new ArrayList<>();

    /**
     * Stores the certificates by alias.
     */
    private final Map<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the certificate keys by alias.
     */
    private final Map<String, Key> certificateKeys = new HashMap<>();

    /**
     * Stores the last time refresh certificates and alias
     */
    private Date lastRefreshTime;

    private KeyVaultClient keyVaultClient;

    private final long refreshInterval;

    KeyVaultCertificates(long refreshInterval,
                         String keyVaultUri,
                         String tenantId,
                         String clientId,
                         String clientSecret,
                         String managedIdentity) {
        this.refreshInterval = refreshInterval;
        updateKeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity);
    }

    KeyVaultCertificates(long refreshInterval, KeyVaultClient keyVaultClient) {
        this.refreshInterval = refreshInterval;
        this.keyVaultClient = keyVaultClient;
    }

    /**
     * Update KeyVaultClient
     *
     * @param keyVaultUri keyVault uri
     * @param tenantId tenant id
     * @param clientId client id
     * @param clientSecret client secret
     * @param managedIdentity managed identity
     */
    public void updateKeyVaultClient(String keyVaultUri,
                                     String tenantId,
                                     String clientId,
                                     String clientSecret,
                                     String managedIdentity) {
        if (keyVaultUri != null) {
            keyVaultClient = new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity);
        } else {
            keyVaultClient = null;
        }
    }

    boolean certificatesNeedRefresh() {
        if (keyVaultClient == null) {
            return false;
        }
        if (lastRefreshTime == null) {
            return true;
        }
        return refreshInterval > 0 && lastRefreshTime.getTime() + refreshInterval < new Date().getTime();
    }

    /**
     * Get certificate aliases.
     *
     * @return certificate aliases
     */
    @Override
    public List<String> getAliases() {
        refreshCertificatesIfNeeded();
        return aliases;
    }

    /**
     * Get certificates.
     *
     * @return certificates
     */
    @Override
    public Map<String, Certificate> getCertificates() {
        refreshCertificatesIfNeeded();
        return certificates;
    }

    /**
     * Get certificates.
     *
     * @return certificate keys
     */
    @Override
    public Map<String, Key> getCertificateKeys() {
        refreshCertificatesIfNeeded();
        return certificateKeys;
    }

    private void refreshCertificatesIfNeeded() {
        if (certificatesNeedRefresh()) { // Avoid acquiring the lock as much as possible.
            synchronized (this) {
                if (certificatesNeedRefresh()) { // After obtaining the lock, avoid doing too many operations.
                    refreshCertificates();
                }
            }
        }
    }

    /**
     * Refresh certificates. Including certificates, aliases, certificate keys.
     *
     */
    public synchronized void refreshCertificates() {
        // When refreshing certificates, the update of the 3 variables should be an atomic operation.
        aliases = keyVaultClient.getAliases();
        certificateKeys.clear();
        certificates.clear();
        Optional.ofNullable(aliases)
                .orElse(Collections.emptyList())
                .forEach(alias -> {
                    Key key = keyVaultClient.getKey(alias, null);
                    if (!Objects.isNull(key)) {
                        certificateKeys.put(alias, key);
                    }
                    Certificate certificate = keyVaultClient.getCertificate(alias);
                    if (!Objects.isNull(certificate)) {
                        certificates.put(alias, certificate);
                    }
                });
        lastRefreshTime = new Date();
    }

    /**
     * Get latest alias by certificate which in portal
     *
     * @param certificate certificate got
     * @return certificate' alias if exist.
     */
    String refreshAndGetAliasByCertificate(Certificate certificate) {
        refreshCertificates();
        return getCertificates().entrySet()
                                .stream()
                                .filter(entry -> certificate.equals(entry.getValue()))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElse(null);

    }

    /**
     * Delete certificate info by alias if exits
     *
     * @param alias deleted certificate
     */
    @Override
    public void deleteEntry(String alias) {
        if (aliases != null) {
            aliases.remove(alias);
        }
        certificates.remove(alias);
        certificateKeys.remove(alias);
    }

}
