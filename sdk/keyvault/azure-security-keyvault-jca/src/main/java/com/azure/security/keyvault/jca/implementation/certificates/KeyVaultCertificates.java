// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import com.azure.security.keyvault.jca.implementation.KeyVaultClient;

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
public final class KeyVaultCertificates implements AzureCertificates {
    /**
     * Stores the list of aliases.
     */
    private List<String> aliases = new ArrayList<>();

    /**
     * Stores the certificates by alias.
     */
    private final Map<String, Certificate> certificates = new HashMap<>();

    /**
     * Stores the certificate chains by alias.
     */
    private final Map<String, Certificate[]> certificateChains = new HashMap<>();

    /**
     * Stores the certificate keys by alias.
     */
    private final Map<String, Key> certificateKeys = new HashMap<>();

    /**
     * Stores the last time refresh certificates and alias.
     */
    private Date lastRefreshTime;

    private KeyVaultClient keyVaultClient;

    private final long refreshInterval;

    public KeyVaultCertificates(long refreshInterval, String keyVaultUri, String tenantId, String clientId,
        String clientSecret, String managedIdentity, boolean disableChallengeResourceVerification) {

        this.refreshInterval = refreshInterval;

        updateKeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity, disableChallengeResourceVerification);
    }

    public KeyVaultCertificates(long refreshInterval, KeyVaultClient keyVaultClient) {
        this.refreshInterval = refreshInterval;
        this.keyVaultClient = keyVaultClient;
    }

    /**
     * Update KeyVaultClient.
     *
     * @param keyVaultUri Key Vault URI.
     * @param tenantId Tenant ID.
     * @param clientId Client ID.
     * @param clientSecret Client secret.
     * @param managedIdentity Managed identity.
     * @param disableChallengeResourceVerification Indicates if the challenge resource verification should be disabled.
     */
    public void updateKeyVaultClient(String keyVaultUri, String tenantId, String clientId, String clientSecret,
        String managedIdentity, boolean disableChallengeResourceVerification) {

        if (keyVaultUri != null) {
            keyVaultClient = new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity,
                disableChallengeResourceVerification);
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
     * @return Certificate aliases.
     */
    @Override
    public List<String> getAliases() {
        refreshCertificatesIfNeeded();

        return aliases;
    }

    /**
     * Get certificates.
     *
     * @return Certificates.
     */
    @Override
    public Map<String, Certificate> getCertificates() {
        refreshCertificatesIfNeeded();
        return certificates;
    }

    /**
     * Get certificate chains.
     * @return certificate chains
     */
    @Override
    public Map<String, Certificate[]> getCertificateChains() {
        refreshCertificatesIfNeeded();
        return certificateChains;
    }

    /**
     * Get certificate keys.
     *
     * @return Certificate keys.
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
     * Refresh certificates. Including certificates, aliases, certificate keys, certificate chains.
     */
    public synchronized void refreshCertificates() {
        // When refreshing certificates, the update of the 3 variables should be an atomic operation.
        aliases = keyVaultClient.getAliases();
        certificateKeys.clear();
        certificates.clear();
        certificateChains.clear();

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
                Certificate[] certificateChain = keyVaultClient.getCertificateChain(alias);
                if (!Objects.isNull(certificateChain)) {
                    certificateChains.put(alias, certificateChain);
                }
            });

        lastRefreshTime = new Date();
    }

    /**
     * Get latest alias by certificate.
     *
     * @param certificate Certificate to get alias with.
     *
     * @return Certificate alias if it exists.
     */
    public String refreshAndGetAliasByCertificate(Certificate certificate) {
        refreshCertificates();
        return getCertificates().entrySet()
            .stream()
            .filter(entry -> certificate.equals(entry.getValue()))
            .findFirst()
            .map(Map.Entry::getKey)
            .orElse(null);

    }

    /**
     * Delete certificate info by alias if exists.
     *
     * @param alias Deleted certificate.
     */
    @Override
    public void deleteEntry(String alias) {
        if (aliases != null) {
            aliases.remove(alias);
        }
        certificates.remove(alias);
        certificateChains.remove(alias);
        certificateKeys.remove(alias);
    }
}
