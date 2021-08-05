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

    public static final String PREFIX = "azure.keyvault";
    public static final String DELIMITER = ".";

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public String getCertificatePassword() {
        return certificatePassword;
    }

    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Long getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(Long refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public List<String> getSecretKeys() {
        return secretKeys;
    }

    public void setSecretKeys(List<String> secretKeys) {
        this.secretKeys = secretKeys;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getCaseSensitiveKeys() {
        return caseSensitiveKeys;
    }

    public void setCaseSensitiveKeys(String caseSensitiveKeys) {
        this.caseSensitiveKeys = caseSensitiveKeys;
    }

    @Deprecated
    @DeprecatedConfigurationProperty(
        reason = "Deprecate the telemetry endpoint and use HTTP header User Agent instead.")
    public String getAllowTelemetry() {
        return allowTelemetry;
    }

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
        CASE_SENSITIVE_KEYS("case-sensitive-keys"),
        CERTIFICATE_PASSWORD("certificate-password"),
        AUTHORITY_HOST("authority-host"),
        SECRET_SERVICE_VERSION("secret-service-version"),
        CERTIFICATE_PATH("certificate-path"),
        CLIENT_ID("client-id"),
        CLIENT_KEY("client-key"),
        ENABLED("enabled"),
        ORDER("order"),
        REFRESH_INTERVAL("refresh-interval"),
        SECRET_KEYS("secret-keys"),
        TENANT_ID("tenant-id"),
        URI("uri");

        private final String name;

        String getName() {
            return name;
        }

        Property(String name) {
            this.name = name;
        }
    }

    public static String getPropertyName(Property property) {
        return String.join(DELIMITER, PREFIX, property.getName());
    }

    public static String getPropertyName(String normalizedName, Property property) {
        return Stream.of(PREFIX, normalizedName, property.getName())
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.joining(DELIMITER));
    }
}
