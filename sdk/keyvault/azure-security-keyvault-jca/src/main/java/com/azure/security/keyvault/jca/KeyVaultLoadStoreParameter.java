// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import java.security.KeyStore;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Azure Key Vault configuration used to load the KeyStoreSpi.
 *
 * <p>Create an instance explicitly for programmatic configuration, or use {@link #fromSystemProperties()} to capture
 * the supported system properties in a configuration snapshot.</p>
 *
 * @see KeyStore.LoadStoreParameter
 */
public final class KeyVaultLoadStoreParameter implements KeyStore.LoadStoreParameter {
    private static final String DEFAULT_CERT_PATH_WELL_KNOWN = "/etc/certs/well-known/";

    private static final String DEFAULT_CERT_PATH_CUSTOM = "/etc/certs/custom/";

    /**
     * Stores the Key Vault URI.
     */
    private final String keyVaultUri;

    /**
     * Stores the tenant id.
     */
    private final String tenantId;

    /**
     * Stores the client id.
     */
    private final String clientId;

    /**
     * Stores the client secret.
     */
    private final String clientSecret;

    /**
     * Stores the user-assigned Managed Identity.
     */
    private final String managedIdentity;

    /**
     * Stores the access token.
     */
    private String accessToken;

    /**
     * Stores a flag indicating if challenge resource verification shall be disabled.
     */
    private boolean disableChallengeResourceVerification = false;

    /**
     * Stores the certificate refresh interval in milliseconds.
     */
    private long certificatesRefreshIntervalInMs;

    /**
     * Stores a flag indicating whether AIA certificate downloads are disabled.
     */
    private boolean disableAiaDownload;

    /**
     * Stores the well-known certificate path.
     */
    private String certPathWellKnown = DEFAULT_CERT_PATH_WELL_KNOWN;

    /**
     * Stores the custom certificate path.
     */
    private String certPathCustom = DEFAULT_CERT_PATH_CUSTOM;

    /**
     * Stores whether certificates are refreshed when an untrusted certificate is encountered.
     */
    private boolean refreshCertificatesWhenHaveUnTrustCertificate;

    /**
     * Stores certificate alias filter patterns.
     */
    private Set<String> certificateAliasFilterPatterns = Collections.emptySet();

    /**
     * Creates a load-store parameter that captures all supported system properties and their defaults.
     *
     * @return The load-store parameter snapshot.
     */
    public static KeyVaultLoadStoreParameter fromSystemProperties() {
        KeyVaultLoadStoreParameter parameter
            = new KeyVaultLoadStoreParameter(System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI),
                System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_TENANT_ID),
                System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_CLIENT_ID),
                System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_CLIENT_SECRET),
                System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_MANAGED_IDENTITY))
                    .setAccessToken(System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_ACCESS_TOKEN));

        if (Boolean.parseBoolean(
            System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_DISABLE_CHALLENGE_RESOURCE_VERIFICATION))) {
            parameter.disableChallengeResourceVerification();
        }

        String refreshInterval
            = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL_IN_MS);
        if (refreshInterval == null) {
            refreshInterval = System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL);
        }
        if (refreshInterval != null) {
            parameter.setCertificatesRefreshIntervalInMs(Long.parseLong(refreshInterval));
        }

        if (Boolean.parseBoolean(System.getProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD))) {
            parameter.disableAiaDownload();
        }

        parameter.setCertPathWellKnown(
            System.getProperty(KeyVaultJcaPropertyNames.CERT_PATH_WELL_KNOWN, DEFAULT_CERT_PATH_WELL_KNOWN));
        parameter
            .setCertPathCustom(System.getProperty(KeyVaultJcaPropertyNames.CERT_PATH_CUSTOM, DEFAULT_CERT_PATH_CUSTOM));
        parameter.setRefreshCertificatesWhenHaveUnTrustCertificate(Boolean.parseBoolean(System
            .getProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_REFRESH_CERTIFICATES_WHEN_HAVE_UNTRUST_CERTIFICATE)));
        parameter.setCertificateAliasFilterPatterns(getCertificateAliasFilterPatternsFromSystemProperties());

        return parameter;
    }

    private static Set<String> getCertificateAliasFilterPatternsFromSystemProperties() {
        Properties properties = System.getProperties();
        String propertyName = KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATE_ALIAS_FILTER_PATTERN;
        String suffixedPropertyPrefix = propertyName + ".";

        return properties.stringPropertyNames()
            .stream()
            .filter(name -> name.equals(propertyName) || name.startsWith(suffixedPropertyPrefix))
            .map(properties::getProperty)
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(pattern -> !pattern.isEmpty())
            .collect(Collectors.toSet());
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri) {
        this(keyVaultUri, null, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param managedIdentity The managed identity.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri, String managedIdentity) {
        this(keyVaultUri, null, null, null, managedIdentity);
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param tenantId The tenant id.
     * @param clientId The client id.
     * @param clientSecret The client secret.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri, String tenantId, String clientId, String clientSecret) {
        this(keyVaultUri, tenantId, clientId, clientSecret, null);
    }

    /**
     * Constructor.
     *
     * @param keyVaultUri The Azure Key Vault URI.
     * @param tenantId The tenant id.
     * @param clientId The client id.
     * @param clientSecret The client secret.
     * @param managedIdentity The managed identity.
     */
    public KeyVaultLoadStoreParameter(String keyVaultUri, String tenantId, String clientId, String clientSecret,
        String managedIdentity) {
        this.keyVaultUri = keyVaultUri;
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.managedIdentity = managedIdentity;
    }

    /**
     * Get the protection parameter.
     *
     * @return {@code null}.
     */
    @Override
    public KeyStore.ProtectionParameter getProtectionParameter() {
        return null;
    }

    /**
     * Get the client id.
     *
     * @return The client id.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Get the client secret.
     *
     * @return The client secret.
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Get the Managed Identity.
     *
     * @return The Managed Identity.
     */
    public String getManagedIdentity() {
        return managedIdentity;
    }

    /**
     * Set the access token.
     *
     * @param accessToken The access token.
     *
     * @return The KeyVaultLoadStoreParameter.
     */
    public KeyVaultLoadStoreParameter setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Get the access token.
     *
     * @return The access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Get the tenant id.
     *
     * @return the tenant id.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Get the Azure Key Vault URI.
     *
     * @return The Azure Key Vault URI.
     */
    public String getUri() {
        return keyVaultUri;
    }

    /**
     * Get a value indicating a check verifying if the authentication challenge resource matches the Key Vault or
     * Managed HSM domain will be performed. This verification is performed by default.
     *
     * @return A value indicating if challenge resource verification is disabled.
     */
    boolean isChallengeResourceVerificationDisabled() {
        return disableChallengeResourceVerification;
    }

    /**
     * Gets whether challenge resource verification is disabled.
     *
     * @return {@code true} if challenge resource verification is disabled; otherwise, {@code false}.
     */
    public boolean isDisableChallengeResourceVerification() {
        return disableChallengeResourceVerification;
    }

    /**
     * Disables verifying if the authentication challenge resource matches the Key Vault or Managed HSM domain. This
     * verification is performed by default.
     */
    public void disableChallengeResourceVerification() {
        disableChallengeResourceVerification = true;
    }

    /**
     * Gets the certificate refresh interval in milliseconds.
     *
     * @return The certificate refresh interval in milliseconds.
     */
    public long getCertificatesRefreshIntervalInMs() {
        return certificatesRefreshIntervalInMs;
    }

    /**
     * Sets the certificate refresh interval in milliseconds.
     *
     * @param certificatesRefreshIntervalInMs The certificate refresh interval in milliseconds.
     * @return The KeyVaultLoadStoreParameter.
     */
    public KeyVaultLoadStoreParameter setCertificatesRefreshIntervalInMs(long certificatesRefreshIntervalInMs) {
        this.certificatesRefreshIntervalInMs = certificatesRefreshIntervalInMs;
        return this;
    }

    /**
     * Gets whether AIA certificate downloads are disabled.
     *
     * @return {@code true} if AIA certificate downloads are disabled; otherwise, {@code false}.
     */
    public boolean isAiaDownloadDisabled() {
        return disableAiaDownload;
    }

    /**
     * Disables AIA certificate downloads.
     */
    public void disableAiaDownload() {
        disableAiaDownload = true;
    }

    /**
     * Gets the well-known certificate path.
     *
     * @return The well-known certificate path.
     */
    public String getCertPathWellKnown() {
        return certPathWellKnown;
    }

    /**
     * Sets the well-known certificate path.
     *
     * @param certPathWellKnown The well-known certificate path.
     * @return The KeyVaultLoadStoreParameter.
     */
    public KeyVaultLoadStoreParameter setCertPathWellKnown(String certPathWellKnown) {
        this.certPathWellKnown = certPathWellKnown;
        return this;
    }

    /**
     * Gets the custom certificate path.
     *
     * @return The custom certificate path.
     */
    public String getCertPathCustom() {
        return certPathCustom;
    }

    /**
     * Sets the custom certificate path.
     *
     * @param certPathCustom The custom certificate path.
     * @return The KeyVaultLoadStoreParameter.
     */
    public KeyVaultLoadStoreParameter setCertPathCustom(String certPathCustom) {
        this.certPathCustom = certPathCustom;
        return this;
    }

    /**
     * Gets whether certificates are refreshed when an untrusted certificate is encountered.
     *
     * @return {@code true} if certificates are refreshed; otherwise, {@code false}.
     */
    public boolean isRefreshCertificatesWhenHaveUnTrustCertificate() {
        return refreshCertificatesWhenHaveUnTrustCertificate;
    }

    /**
     * Sets whether certificates are refreshed when an untrusted certificate is encountered.
     *
     * @param refreshCertificatesWhenHaveUnTrustCertificate Whether certificates are refreshed.
     * @return The KeyVaultLoadStoreParameter.
     */
    public KeyVaultLoadStoreParameter
        setRefreshCertificatesWhenHaveUnTrustCertificate(boolean refreshCertificatesWhenHaveUnTrustCertificate) {
        this.refreshCertificatesWhenHaveUnTrustCertificate = refreshCertificatesWhenHaveUnTrustCertificate;
        return this;
    }

    /**
     * Gets the certificate alias filter patterns.
     *
     * @return A copy of the certificate alias filter patterns.
     */
    public Set<String> getCertificateAliasFilterPatterns() {
        return new HashSet<>(certificateAliasFilterPatterns);
    }

    /**
     * Sets the certificate alias filter patterns.
     *
     * @param certificateAliasFilterPatterns The certificate alias filter patterns.
     * @return The KeyVaultLoadStoreParameter.
     */
    public KeyVaultLoadStoreParameter setCertificateAliasFilterPatterns(Set<String> certificateAliasFilterPatterns) {
        this.certificateAliasFilterPatterns = certificateAliasFilterPatterns == null
            ? Collections.emptySet()
            : new HashSet<>(certificateAliasFilterPatterns);
        return this;
    }
}
