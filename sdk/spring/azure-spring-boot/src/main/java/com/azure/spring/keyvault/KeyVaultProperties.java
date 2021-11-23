// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import com.azure.spring.utils.Constants;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;

/**
 * KeyVaultProperties
 */
@ConfigurationProperties(value = KeyVaultProperties.PREFIX)
public class KeyVaultProperties {

    /**
     * The prefix
     */
    public static final String PREFIX = "azure.keyvault";

    /**
     * The delimiter
     */
    public static final String DELIMITER = ".";

    /**
     * Gets the client ID.
     *
     * @return the client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Sets the client ID.
     *
     * @param clientId the client ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Gets the client key.
     *
     * @return the client key
     */
    public String getClientKey() {
        return clientKey;
    }

    /**
     * Sets the client key.
     *
     * @param clientKey the client key
     */
    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    /**
     * Gets the tenant ID.
     *
     * @return the tenant ID
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Sets the tenant ID.
     *
     * @param tenantId the tenant ID
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Gets the certificate path.
     *
     * @return the certificate path
     */
    public String getCertificatePath() {
        return certificatePath;
    }

    /**
     * Sets the certificate path.
     *
     * @param certificatePath the certificate path
     */
    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    /**
     * Gets the certificate password.
     *
     * @return the certificate password
     */
    public String getCertificatePassword() {
        return certificatePassword;
    }

    /**
     * Sets the certificate password.
     *
     * @param certificatePassword the certificate password
     */
    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    /**
     * Whether enabled.
     *
     * @return whether enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Sets whether enabled.
     *
     * @param enabled whether enabled
     */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the URI.
     *
     * @return the URI
     */
    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI.
     *
     * @param uri the URI
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Gets the refresh interval.
     *
     * @return the refresh interval
     */
    public Long getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Sets the refresh interval.
     *
     * @param refreshInterval the refresh interval
     */
    public void setRefreshInterval(Long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * Gets the list of secret keys.
     *
     * @return the list of secret keys
     */
    public List<String> getSecretKeys() {
        return secretKeys;
    }

    /**
     * Sets the list of secret keys.
     *
     * @param secretKeys the list of secret keys
     */
    public void setSecretKeys(List<String> secretKeys) {
        this.secretKeys = secretKeys;
    }

    /**
     * Gets the order.
     *
     * @return the order
     */
    public String getOrder() {
        return order;
    }

    /**
     * Sets the order.
     *
     * @param order the order
     */
    public void setOrder(String order) {
        this.order = order;
    }

    /**
     * Gets the case-sensitive keys.
     *
     * @return the case-sensitive keys
     */
    public String getCaseSensitiveKeys() {
        return caseSensitiveKeys;
    }

    /**
     * Sets the case-sensitive keys.
     *
     * @param caseSensitiveKeys the case-sensitive keys
     */
    public void setCaseSensitiveKeys(String caseSensitiveKeys) {
        this.caseSensitiveKeys = caseSensitiveKeys;
    }

    /**
     * Whether telemetry is allowed.
     *
     * @return whether telemetry is allowed
     * @deprecated Determined by HTTP header User-Agent instead
     */
    @Deprecated
    @DeprecatedConfigurationProperty(
        reason = "Deprecate the telemetry endpoint and use HTTP header User Agent instead.")
    public String getAllowTelemetry() {
        return allowTelemetry;
    }

    /**
     * Sets whether telemetry is allowed.
     *
     * @param allowTelemetry whether telemetry is allowed
     */
    public void setAllowTelemetry(String allowTelemetry) {
        this.allowTelemetry = allowTelemetry;
    }

    private Boolean enabled;
    private List<String> secretKeys;
    private Long refreshInterval = Constants.DEFAULT_REFRESH_INTERVAL_MS;
    private String allowTelemetry;
    /**
     * Defines the constant for the property that enables/disables case sensitive keys.
     */
    private String caseSensitiveKeys;
    private String certificatePassword;
    private String certificatePath;
    private String clientId;
    private String clientKey;
    /**
     * The constant used to define the order of the key vaults you are
     * delivering (comma delimited, e.g 'my-vault, my-vault-2').
     */
    private String order;
    private String tenantId;
    private String uri;

    /**
     * enum Property
     */
    public enum Property {
        /**
         * Case sensitive keys
         */
        CASE_SENSITIVE_KEYS("case-sensitive-keys"),

        /**
         * Certificate password
         */
        CERTIFICATE_PASSWORD("certificate-password"),

        /**
         * Authority host
         */
        AUTHORITY_HOST("authority-host"),

        /**
         * Secret service version
         */
        SECRET_SERVICE_VERSION("secret-service-version"),

        /**
         * Certificate path
         */
        CERTIFICATE_PATH("certificate-path"),

        /**
         * Client ID
         */
        CLIENT_ID("client-id"),

        /**
         * Client key
         */
        CLIENT_KEY("client-key"),

        /**
         * Enabled
         */
        ENABLED("enabled"),

        /**
         * Order
         */
        ORDER("order"),

        /**
         * Refresh interval
         */
        REFRESH_INTERVAL("refresh-interval"),

        /**
         * Secret keys
         */
        SECRET_KEYS("secret-keys"),

        /**
         * Tenant ID
         */
        TENANT_ID("tenant-id"),

        /**
         * URI
         */
        URI("uri");

        private final String name;

        String getName() {
            return name;
        }

        Property(String name) {
            this.name = name;
        }
    }

    /**
     * Gets the property name.
     *
     * @param property the property
     * @return the property name
     */
    public static String getPropertyName(Property property) {
        return String.join(DELIMITER, PREFIX, property.getName());
    }

    /**
     * Gets the property name.
     *
     * @param normalizedName the normalized name
     * @param property the property
     * @return the property name
     */
    public static String getPropertyName(String normalizedName, Property property) {
        return Stream.of(PREFIX, normalizedName, property.getName())
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(DELIMITER));
    }
}
