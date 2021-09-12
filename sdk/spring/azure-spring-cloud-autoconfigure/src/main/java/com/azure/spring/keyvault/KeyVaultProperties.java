// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * KeyVaultProperties
 */
@ConfigurationProperties(value = KeyVaultProperties.PREFIX)
public class KeyVaultProperties {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultProperties.class);

    // TODO the prefix?
    public static final String PREFIX = "spring.cloud.azure.keyvault.propertysource";
    public static final String DELIMITER = ".";

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


    private Boolean enabled;
    private List<String> secretKeys;
    private Long refreshInterval = KeyVaultEnvironmentPostProcessorHelper.DEFAULT_REFRESH_INTERVAL_MS;
    /**
     * Defines the constant for the property that enables/disables case sensitive keys.
     */
    private String caseSensitiveKeys;

    /**
     * The constant used to define the order of the key vaults you are
     * delivering (comma delimited, e.g 'my-vault, my-vault-2').
     */
    private String order;

    private String uri;

    /**
     * enum Property
     */
    public enum Property {
        AUTHORITY_HOST("environment.authority-host"),
        CLIENT_ID("credential.client-id"),
        CLIENT_SECRET("credential.client-secret"),
        CERTIFICATE_PATH("credential.client-certificate-path"),
        CERTIFICATE_PASSWORD("credential.client-certificate-password"),
        TENANT_ID("credential.tenant-id"),
        SECRET_SERVICE_VERSION("secret-service-version"),
        CASE_SENSITIVE_KEYS("case-sensitive-keys"),
        CLIENT_KEY("client-key"),
        ENABLED("enabled"),
        ORDER("order"),
        REFRESH_INTERVAL("refresh-interval"),
        SECRET_KEYS("secret-keys"),
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
