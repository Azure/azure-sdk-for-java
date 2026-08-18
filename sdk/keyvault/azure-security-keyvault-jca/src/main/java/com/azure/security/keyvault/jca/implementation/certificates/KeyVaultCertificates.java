// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.certificates;

import com.azure.security.keyvault.jca.KeyVaultJcaPropertyNames;
import com.azure.security.keyvault.jca.KeyVaultLoadStoreParameter;
import com.azure.security.keyvault.jca.implementation.CertificateVersion;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Caches certificate material loaded from Azure Key Vault.
 */
public final class KeyVaultCertificates implements AzureCertificates {
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
     * Stores the resolved certificate version by alias.
     */
    private final Map<String, CertificateVersion> certificateVersions = new HashMap<>();

    /**
     * Stores certificate version resolutions currently in progress by alias.
     */
    private final Map<String, CompletableFuture<Void>> inFlightCertificateVersionResolutions = new HashMap<>();

    /**
     * Stores certificate loads currently in progress by alias.
     */
    private final Map<String, CompletableFuture<Void>> inFlightCertificateLoads = new HashMap<>();

    /**
     * Stores certificate chain loads currently in progress by alias.
     */
    private final Map<String, CompletableFuture<Void>> inFlightCertificateChainLoads = new HashMap<>();

    /**
     * Stores certificate key loads currently in progress by alias.
     */
    private final Map<String, CompletableFuture<Void>> inFlightCertificateKeyLoads = new HashMap<>();

    /**
     * Identifies the current material cache generation.
     */
    private long cacheGeneration;

    /**
     * Stores the last time refresh certificates and alias.
     */
    private Date lastRefreshTime;

    private KeyVaultClient keyVaultClient;

    private long certificatesRefreshIntervalInMs;

    private List<Pattern> includeAliasPatterns;

    private List<Pattern> excludeAliasPatterns;

    /**
     * Creates a Key Vault certificate cache using the specified configuration.
     *
     * @param parameter The Key Vault load-store configuration.
     */
    public KeyVaultCertificates(KeyVaultLoadStoreParameter parameter) {
        updateKeyVaultClient(parameter);
    }

    private void updateCertificateConfiguration(KeyVaultLoadStoreParameter parameter) {
        Objects.requireNonNull(parameter, "'parameter' cannot be null.");
        Set<String> normalizedFilterPatterns = normalizeFilterPatterns(parameter.getCertificateAliasFilterPatterns());
        List<Pattern> updatedIncludeAliasPatterns = getAliasPatterns(normalizedFilterPatterns, false);
        List<Pattern> updatedExcludeAliasPatterns = getAliasPatterns(normalizedFilterPatterns, true);

        certificatesRefreshIntervalInMs = parameter.getCertificatesRefreshIntervalInMs();
        includeAliasPatterns = updatedIncludeAliasPatterns;
        excludeAliasPatterns = updatedExcludeAliasPatterns;
    }

    private Set<String> normalizeFilterPatterns(Set<String> filterPatterns) {
        return Optional.ofNullable(filterPatterns)
            .orElse(Collections.emptySet())
            .stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(pattern -> !pattern.isEmpty())
            .collect(Collectors.toCollection(HashSet::new));
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
            throw new IllegalArgumentException(
                "Invalid certificate alias filter regex pattern: " + regexPattern
                    + ". If configured via system property, check '"
                    + KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATE_ALIAS_FILTER_PATTERN + "' and '"
                    + KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATE_ALIAS_FILTER_PATTERN + ".<suffix>'.",
                exception);
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
     * Updates the Key Vault client and its certificate cache configuration.
     *
     * @param parameter The Key Vault load-store configuration.
     */
    public synchronized void updateKeyVaultClient(KeyVaultLoadStoreParameter parameter) {
        Objects.requireNonNull(parameter, "'parameter' cannot be null.");
        KeyVaultClient updatedKeyVaultClient = parameter.getUri() == null ? null : new KeyVaultClient(parameter);

        updateCertificateConfiguration(parameter);
        setKeyVaultClient(updatedKeyVaultClient);
        clearCachedState();
    }

    private synchronized void clearCachedState() {
        aliases = new ArrayList<>();
        invalidateCachedMaterials();
        lastRefreshTime = null;
    }

    private void invalidateCachedMaterials() {
        cacheGeneration++;
        loadedCertificateAliases.clear();
        loadedCertificateChainAliases.clear();
        loadedCertificateKeyAliases.clear();
        certificateKeys.clear();
        certificates.clear();
        certificateChains.clear();
        certificateVersions.clear();
        completeAndClear(inFlightCertificateVersionResolutions);
        completeAndClear(inFlightCertificateLoads);
        completeAndClear(inFlightCertificateChainLoads);
        completeAndClear(inFlightCertificateKeyLoads);
    }

    private void completeAndClear(Map<String, CompletableFuture<Void>> inFlightLoads) {
        inFlightLoads.values().forEach(load -> load.complete(null));
        inFlightLoads.clear();
    }

    synchronized boolean certificatesNeedRefresh() {
        if (getKeyVaultClient() == null) {
            return false;
        }
        if (lastRefreshTime == null) {
            return true;
        }

        return certificatesRefreshIntervalInMs > 0
            && lastRefreshTime.getTime() + certificatesRefreshIntervalInMs < new Date().getTime();
    }

    /**
     * Get certificate aliases.
     *
     * @return Certificate aliases.
     */
    @Override
    public List<String> getAliases() {
        refreshCertificatesIfNeeded();
        synchronized (this) {
            return new ArrayList<>(aliases);
        }
    }

    /**
     * Get certificates.
     *
     * @return Certificates.
     */
    @Override
    public Map<String, Certificate> getCertificates() {
        refreshCertificatesIfNeeded();
        synchronized (this) {
            return new HashMap<>(certificates);
        }
    }

    /**
     * Get certificate chains.
     * @return certificate chains
     */
    @Override
    public Map<String, Certificate[]> getCertificateChains() {
        refreshCertificatesIfNeeded();
        synchronized (this) {
            return copyCertificateChains(certificateChains);
        }
    }

    /**
     * Get certificate keys.
     *
     * @return Certificate keys.
     */
    @Override
    public Map<String, Key> getCertificateKeys() {
        refreshCertificatesIfNeeded();
        synchronized (this) {
            return new HashMap<>(certificateKeys);
        }
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
            Certificate[] chain = certificateChains.get(alias);
            return chain == null ? null : chain.clone();
        }
    }

    private Map<String, Certificate[]> copyCertificateChains(Map<String, Certificate[]> source) {
        Map<String, Certificate[]> copiedChains = new HashMap<>();
        source.forEach((alias, chain) -> copiedChains.put(alias, chain == null ? null : chain.clone()));
        return copiedChains;
    }

    private void refreshCertificatesIfNeeded() {
        if (certificatesNeedRefresh()) { // Avoid acquiring the lock as much as possible.
            refreshCertificates(false);
        }
    }

    /**
     * Refresh aliases and invalidate cached certificate details.
     */
    public void refreshCertificates() {
        refreshCertificates(true);
    }

    private void refreshCertificates(boolean forceRefresh) {
        // Listing aliases keeps the lock so concurrent refreshes cannot apply their results out of order.
        synchronized (this) {
            if (keyVaultClient == null) {
                clearCachedState();
                return;
            }

            if (!forceRefresh && !certificatesNeedRefresh()) {
                return;
            }

            // Discover aliases from Key Vault and apply include/exclude regex filters.
            aliases = Optional.ofNullable(keyVaultClient.getAliases())
                .orElse(Collections.emptyList())
                .stream()
                .filter(this::shouldIncludeAlias)
                .sorted()
                .collect(Collectors.toCollection(ArrayList::new));

            invalidateCachedMaterials();
            lastRefreshTime = new Date();
        }
    }

    private void loadCertificateIfNeeded(String alias) {
        loadMaterialIfNeeded(alias, loadedCertificateAliases, certificates, inFlightCertificateLoads,
            (client, version) -> client.getCertificateForVersion(version));
    }

    private void loadCertificateChainIfNeeded(String alias) {
        loadMaterialIfNeeded(alias, loadedCertificateChainAliases, certificateChains, inFlightCertificateChainLoads,
            (client, version) -> client.getCertificateChainForVersion(version));
    }

    private void loadCertificateKeyIfNeeded(String alias) {
        loadMaterialIfNeeded(alias, loadedCertificateKeyAliases, certificateKeys, inFlightCertificateKeyLoads,
            (client, version) -> client.getKeyForVersion(version, null));
    }

    private <T> void loadMaterialIfNeeded(String alias, Set<String> loadedAliases, Map<String, T> loadedMaterials,
        Map<String, CompletableFuture<Void>> inFlightLoads,
        BiFunction<KeyVaultClient, CertificateVersion, T> loadMaterial) {

        refreshCertificatesIfNeeded();
        if (alias == null) {
            return;
        }

        while (true) {
            CertificateVersion certificateVersion = resolveCertificateVersionIfNeeded(alias);
            if (certificateVersion == null) {
                return;
            }

            KeyVaultClient loadClient;
            long loadGeneration;
            CompletableFuture<Void> inFlightLoad;
            boolean loadOwner;

            synchronized (this) {
                loadClient = keyVaultClient;
                if (loadClient == null || loadedAliases.contains(alias) || !aliases.contains(alias)) {
                    return;
                }
                if (certificateVersions.get(alias) != certificateVersion) {
                    continue;
                }

                loadGeneration = cacheGeneration;
                inFlightLoad = inFlightLoads.get(alias);
                loadOwner = inFlightLoad == null;
                if (loadOwner) {
                    inFlightLoad = new CompletableFuture<>();
                    inFlightLoads.put(alias, inFlightLoad);
                }
            }

            if (!loadOwner) {
                awaitInFlightOperation(inFlightLoad);
                synchronized (this) {
                    if (loadedAliases.contains(alias) || keyVaultClient == null || !aliases.contains(alias)) {
                        return;
                    }
                    if (loadClient == keyVaultClient && loadGeneration == cacheGeneration) {
                        return;
                    }
                }
                continue;
            }

            T loadedMaterial = null;
            RuntimeException materialLoadFailure = null;
            try {
                loadedMaterial = loadMaterial.apply(loadClient, certificateVersion);
            } catch (RuntimeException exception) {
                // Release the single-flight state before rethrowing the original failure.
                materialLoadFailure = exception;
            }

            boolean retryWithCurrentGeneration;
            boolean propagateMaterialLoadFailure;
            synchronized (this) {
                boolean currentRequest = loadClient == keyVaultClient
                    && loadGeneration == cacheGeneration
                    && certificateVersions.get(alias) == certificateVersion
                    && aliases.contains(alias);
                if (currentRequest
                    && materialLoadFailure == null
                    && !loadedAliases.contains(alias)
                    && loadedMaterial != null) {
                    loadedMaterials.put(alias, loadedMaterial);
                    loadedAliases.add(alias);
                }

                if (inFlightLoads.get(alias) == inFlightLoad) {
                    inFlightLoads.remove(alias);
                }
                propagateMaterialLoadFailure = currentRequest && materialLoadFailure != null;
                if (propagateMaterialLoadFailure) {
                    inFlightLoad.completeExceptionally(materialLoadFailure);
                } else {
                    inFlightLoad.complete(null);
                }

                retryWithCurrentGeneration = !currentRequest
                    && keyVaultClient != null
                    && aliases.contains(alias)
                    && !loadedAliases.contains(alias);
            }

            if (retryWithCurrentGeneration) {
                continue;
            }
            if (propagateMaterialLoadFailure) {
                throw materialLoadFailure;
            }
            return;
        }
    }

    private CertificateVersion resolveCertificateVersionIfNeeded(String alias) {
        while (true) {
            KeyVaultClient resolvingClient;
            long resolutionGeneration;
            CompletableFuture<Void> inFlightResolution;
            boolean resolutionOwner;

            synchronized (this) {
                resolvingClient = keyVaultClient;
                if (resolvingClient == null || !aliases.contains(alias)) {
                    return null;
                }

                CertificateVersion certificateVersion = certificateVersions.get(alias);
                if (certificateVersion != null) {
                    return certificateVersion;
                }

                resolutionGeneration = cacheGeneration;
                inFlightResolution = inFlightCertificateVersionResolutions.get(alias);
                resolutionOwner = inFlightResolution == null;
                if (resolutionOwner) {
                    inFlightResolution = new CompletableFuture<>();
                    inFlightCertificateVersionResolutions.put(alias, inFlightResolution);
                }
            }

            if (!resolutionOwner) {
                awaitInFlightOperation(inFlightResolution);
                synchronized (this) {
                    CertificateVersion certificateVersion = certificateVersions.get(alias);
                    if (certificateVersion != null) {
                        return certificateVersion;
                    }
                    if (resolvingClient == keyVaultClient && resolutionGeneration == cacheGeneration) {
                        return null;
                    }
                }
                continue;
            }

            CertificateVersion certificateVersion = null;
            RuntimeException resolutionFailure = null;
            try {
                certificateVersion = resolvingClient.resolveCertificateVersion(alias);
            } catch (RuntimeException exception) {
                // Release the single-flight state before rethrowing the original failure.
                resolutionFailure = exception;
            }

            boolean retryWithCurrentGeneration;
            boolean propagateResolutionFailure;
            synchronized (this) {
                boolean currentResolution = resolvingClient == keyVaultClient
                    && resolutionGeneration == cacheGeneration
                    && aliases.contains(alias);
                if (currentResolution && resolutionFailure == null && certificateVersion != null) {
                    certificateVersions.put(alias, certificateVersion);
                }

                if (inFlightCertificateVersionResolutions.get(alias) == inFlightResolution) {
                    inFlightCertificateVersionResolutions.remove(alias);
                }
                propagateResolutionFailure = currentResolution && resolutionFailure != null;
                if (propagateResolutionFailure) {
                    inFlightResolution.completeExceptionally(resolutionFailure);
                } else {
                    inFlightResolution.complete(null);
                }

                CertificateVersion currentVersion = certificateVersions.get(alias);
                if (currentVersion != null) {
                    return currentVersion;
                }

                retryWithCurrentGeneration = !currentResolution && keyVaultClient != null && aliases.contains(alias);
            }

            if (retryWithCurrentGeneration) {
                continue;
            }
            if (propagateResolutionFailure) {
                throw resolutionFailure;
            }
            return null;
        }
    }

    private static void awaitInFlightOperation(CompletableFuture<Void> inFlightOperation) {
        try {
            inFlightOperation.join();
        } catch (CompletionException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw exception;
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
            aliasesSnapshot = new ArrayList<>(aliases);
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
        certificateVersions.remove(alias);
        completeAndRemove(inFlightCertificateVersionResolutions, alias);
        completeAndRemove(inFlightCertificateLoads, alias);
        completeAndRemove(inFlightCertificateChainLoads, alias);
        completeAndRemove(inFlightCertificateKeyLoads, alias);
    }

    private void completeAndRemove(Map<String, CompletableFuture<Void>> inFlightLoads, String alias) {
        CompletableFuture<Void> inFlightLoad = inFlightLoads.remove(alias);
        if (inFlightLoad != null) {
            inFlightLoad.complete(null);
        }
    }
}
