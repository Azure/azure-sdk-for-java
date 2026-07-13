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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.logging.Level.WARNING;

/**
 * Store certificates loaded from KeyVault.
 */
public final class KeyVaultCertificates implements AzureCertificates {
    private static final Logger LOGGER = Logger.getLogger(KeyVaultCertificates.class.getName());

    /**
     * Stores the list of aliases.
     */
    private List<String> aliases = new ArrayList<>();

    /**
     * Stores aliases whose certificate has already been loaded.
     */
    private final Set<String> loadedCertificateAliases = new HashSet<>();

    /**
     * Stores aliases whose certificate chain has already been loaded.
     */
    private final Set<String> loadedCertificateChainAliases = new HashSet<>();

    /**
     * Stores aliases whose private key has already been loaded.
     */
    private final Set<String> loadedCertificateKeyAliases = new HashSet<>();

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

    private final Set<String> certificateFilterPatterns;

    private final List<Pattern> includeAliasPatterns;

    private final List<Pattern> excludeAliasPatterns;

    public KeyVaultCertificates(long refreshInterval, String keyVaultUri, String tenantId, String clientId,
        String clientSecret, String managedIdentity, String accessToken, boolean disableChallengeResourceVerification) {
        this(refreshInterval, keyVaultUri, tenantId, clientId, clientSecret, managedIdentity, accessToken,
            disableChallengeResourceVerification, Collections.emptySet());
    }

    public KeyVaultCertificates(long refreshInterval, String keyVaultUri, String tenantId, String clientId,
        String clientSecret, String managedIdentity, String accessToken, boolean disableChallengeResourceVerification,
        Set<String> certificateFilterPatterns) {

        this.refreshInterval = refreshInterval;
        this.certificateFilterPatterns
            = new HashSet<>(Optional.ofNullable(certificateFilterPatterns).orElse(Collections.emptySet()));
        this.includeAliasPatterns = getAliasPatterns(this.certificateFilterPatterns, false);
        this.excludeAliasPatterns = getAliasPatterns(this.certificateFilterPatterns, true);

        updateKeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity, accessToken,
            disableChallengeResourceVerification);
    }

    public KeyVaultCertificates(long refreshInterval, KeyVaultClient keyVaultClient) {
        this(refreshInterval, keyVaultClient, Collections.emptySet());
    }

    public KeyVaultCertificates(long refreshInterval, KeyVaultClient keyVaultClient,
        Set<String> certificateFilterPatterns) {
        this.refreshInterval = refreshInterval;
        setKeyVaultClient(keyVaultClient);
        this.certificateFilterPatterns
            = new HashSet<>(Optional.ofNullable(certificateFilterPatterns).orElse(Collections.emptySet()));
        this.includeAliasPatterns = getAliasPatterns(this.certificateFilterPatterns, false);
        this.excludeAliasPatterns = getAliasPatterns(this.certificateFilterPatterns, true);
    }

    private List<Pattern> getAliasPatterns(Set<String> filterPatterns, boolean excludePatterns) {
        return Optional.ofNullable(filterPatterns)
            .orElse(Collections.emptySet())
            .stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(pattern -> !pattern.isEmpty())
            .filter(pattern -> pattern.startsWith("!") == excludePatterns)
            .map(pattern -> {
                if (excludePatterns) {
                    return pattern.substring(1);
                }
                if (pattern.startsWith("+")) {
                    return pattern.substring(1);
                }
                return pattern;
            })
            .filter(pattern -> !pattern.isEmpty())
            .map(this::compileRegexPattern)
            .collect(Collectors.toList());
    }

    private Pattern compileRegexPattern(String regexPattern) {
        try {
            return Pattern.compile(regexPattern);
        } catch (PatternSyntaxException exception) {
            throw new IllegalArgumentException("Invalid certificate filter regex pattern: " + regexPattern, exception);
        }
    }

    private boolean shouldIncludeAlias(String alias) {
        if (alias == null) {
            return false;
        }

        boolean included = includeAliasPatterns.isEmpty()
            || includeAliasPatterns.stream().anyMatch(pattern -> pattern.matcher(alias).matches());
        if (!included) {
            return false;
        }

        return excludeAliasPatterns.stream().noneMatch(pattern -> pattern.matcher(alias).matches());
    }

    private synchronized KeyVaultClient getKeyVaultClient() {
        return keyVaultClient;
    }

    private synchronized void setKeyVaultClient(KeyVaultClient keyVaultClient) {
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
     * @param accessToken Access token.
     * @param disableChallengeResourceVerification Indicates if the challenge resource verification should be disabled.
     */
    public synchronized void updateKeyVaultClient(String keyVaultUri, String tenantId, String clientId,
        String clientSecret, String managedIdentity, String accessToken, boolean disableChallengeResourceVerification) {

        if (keyVaultUri != null) {
            setKeyVaultClient(new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, managedIdentity,
                accessToken, disableChallengeResourceVerification));
        } else {
            setKeyVaultClient(null);
        }

        clearCachedState();
    }

    private synchronized void clearCachedState() {
        aliases = new ArrayList<>();
        loadedCertificateAliases.clear();
        loadedCertificateChainAliases.clear();
        loadedCertificateKeyAliases.clear();
        certificateKeys.clear();
        certificates.clear();
        certificateChains.clear();
        lastRefreshTime = null;
    }

    synchronized boolean certificatesNeedRefresh() {
        if (getKeyVaultClient() == null) {
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

    /**
     * Get key by alias.
     *
     * @param alias The alias.
     * @return The key, or {@code null}.
     */
    public Key getCertificateKey(String alias) {
        loadCertificateKeyIfNeeded(alias);
        synchronized (this) {
            return certificateKeys.get(alias);
        }
    }

    /**
     * Get certificate by alias.
     *
     * @param alias The alias.
     * @return The certificate, or {@code null}.
     */
    public Certificate getCertificate(String alias) {
        loadCertificateIfNeeded(alias);
        synchronized (this) {
            return certificates.get(alias);
        }
    }

    /**
     * Get certificate chain by alias.
     *
     * @param alias The alias.
     * @return The certificate chain, or {@code null}.
     */
    public Certificate[] getCertificateChain(String alias) {
        loadCertificateChainIfNeeded(alias);
        synchronized (this) {
            return certificateChains.get(alias);
        }
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
     * Refresh aliases and invalidate cached certificate details.
     */
    public synchronized void refreshCertificates() {
        KeyVaultClient currentKeyVaultClient = getKeyVaultClient();
        if (currentKeyVaultClient == null) {
            clearCachedState();
            return;
        }

        // Discover aliases from Key Vault and apply include/exclude regex filters.
        aliases = Optional.ofNullable(currentKeyVaultClient.getAliases())
            .orElse(Collections.emptyList())
            .stream()
            .filter(this::shouldIncludeAlias)
            .sorted()
            .collect(Collectors.toCollection(ArrayList::new));

        loadedCertificateAliases.clear();
        loadedCertificateChainAliases.clear();
        loadedCertificateKeyAliases.clear();
        certificateKeys.clear();
        certificates.clear();
        certificateChains.clear();

        lastRefreshTime = new Date();
    }

    private void loadCertificateIfNeeded(String alias) {
        refreshCertificatesIfNeeded();
        KeyVaultClient currentKeyVaultClient = getKeyVaultClient();

        if (alias == null || currentKeyVaultClient == null) {
            return;
        }

        synchronized (this) {
            if (loadedCertificateAliases.contains(alias)
                || !Optional.ofNullable(aliases).orElse(Collections.emptyList()).contains(alias)) {
                return;
            }
        }

        try {
            Certificate loadedCertificate = currentKeyVaultClient.getCertificate(alias);
            synchronized (this) {
                if (loadedCertificateAliases.contains(alias)
                    || !Optional.ofNullable(aliases).orElse(Collections.emptyList()).contains(alias)) {
                    return;
                }

                if (loadedCertificate != null) {
                    certificates.put(alias, loadedCertificate);
                    loadedCertificateAliases.add(alias);
                }
            }
        } catch (RuntimeException exception) {
            LOGGER.log(WARNING, exception, () -> "Failed to load certificate for alias: " + alias);
        }
    }

    private void loadCertificateChainIfNeeded(String alias) {
        refreshCertificatesIfNeeded();
        KeyVaultClient currentKeyVaultClient = getKeyVaultClient();

        if (alias == null || currentKeyVaultClient == null) {
            return;
        }

        synchronized (this) {
            if (loadedCertificateChainAliases.contains(alias)
                || !Optional.ofNullable(aliases).orElse(Collections.emptyList()).contains(alias)) {
                return;
            }
        }

        try {
            Certificate[] loadedCertificateChain = currentKeyVaultClient.getCertificateChain(alias);
            synchronized (this) {
                if (loadedCertificateChainAliases.contains(alias)
                    || !Optional.ofNullable(aliases).orElse(Collections.emptyList()).contains(alias)) {
                    return;
                }

                if (loadedCertificateChain != null && loadedCertificateChain.length > 0) {
                    certificateChains.put(alias, loadedCertificateChain);
                    loadedCertificateChainAliases.add(alias);
                }
            }
        } catch (RuntimeException exception) {
            LOGGER.log(WARNING, exception, () -> "Failed to load certificate chain for alias: " + alias);
        }
    }

    private void loadCertificateKeyIfNeeded(String alias) {
        refreshCertificatesIfNeeded();
        KeyVaultClient currentKeyVaultClient = getKeyVaultClient();

        if (alias == null || currentKeyVaultClient == null) {
            return;
        }

        synchronized (this) {
            if (loadedCertificateKeyAliases.contains(alias)
                || !Optional.ofNullable(aliases).orElse(Collections.emptyList()).contains(alias)) {
                return;
            }
        }

        try {
            Key loadedKey = currentKeyVaultClient.getKey(alias, null);
            synchronized (this) {
                if (loadedCertificateKeyAliases.contains(alias)
                    || !Optional.ofNullable(aliases).orElse(Collections.emptyList()).contains(alias)) {
                    return;
                }

                if (loadedKey != null) {
                    certificateKeys.put(alias, loadedKey);
                    loadedCertificateKeyAliases.add(alias);
                }
            }
        } catch (RuntimeException exception) {
            LOGGER.log(WARNING, exception, () -> "Failed to load certificate key for alias: " + alias);
        }
    }

    /**
     * Get latest alias by certificate.
     *
     * @param certificate Certificate to get alias with.
     *
     * @return Certificate alias if it exists.
     */
    public String refreshAndGetAliasByCertificate(Certificate certificate) {
        if (certificate == null) {
            return null;
        }

        refreshCertificates();

        List<String> aliasesSnapshot;
        synchronized (this) {
            aliasesSnapshot = new ArrayList<>(Optional.ofNullable(aliases).orElse(Collections.emptyList()));
        }

        aliasesSnapshot.forEach(this::loadCertificateIfNeeded);

        Map<String, Certificate> certificatesSnapshot;
        synchronized (this) {
            certificatesSnapshot = new HashMap<>(certificates);
        }

        return certificatesSnapshot.entrySet()
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
    public synchronized void deleteEntry(String alias) {
        if (aliases != null) {
            aliases.remove(alias);
        }
        loadedCertificateAliases.remove(alias);
        loadedCertificateChainAliases.remove(alias);
        loadedCertificateKeyAliases.remove(alias);
        certificates.remove(alias);
        certificateChains.remove(alias);
        certificateKeys.remove(alias);
    }
}
