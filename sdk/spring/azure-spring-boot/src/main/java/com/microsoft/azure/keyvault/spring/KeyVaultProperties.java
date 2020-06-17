// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.spring;

import java.util.List;

import com.microsoft.azure.utils.Constants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = KeyVaultProperties.PREFIX)
@Data
public class KeyVaultProperties {

    public static final String PREFIX = "azure.keyvault";
    public static final String DELIMITER = ".";

    private String clientId;
    private String clientKey;
    private String tenantId;
    private String certificatePath;
    private Boolean enabled;
    private String uri;
    private Long refreshInterval = Constants.DEFAULT_REFRESH_INTERVAL_MS;
    private List<String> secretKeys;
    /**
     * The constant used to define the order of the key vaults you are
     * delivering (comma delimited, e.g 'my-vault, my-vault-2').
     */
    private String order;

    /**
     * Defines the constant for the property that enables/disables case sensitive keys.
     */
    private String caseSensitiveKeys;
    private String allowTelemetry;


    public enum Property {
        CLIENT_ID("client-id"),
        CLIENT_KEY("client-key"),
        TENANT_ID("tenant-id"),
        CERTIFICATE_PATH("certificate-path"),
        CERTIFICATE_PASSWORD("certificate-password"),
        ENABLED("enabled"),
        URI("uri"),
        REFRESH_INTERVAL("refresh-interval"),
        SECRET_KEYS("secret-keys"),
        ORDER("order"),
        CASE_SENSITIVE_KEYS("case-sensitive-keys"),
        ALLOW_TELEMETRY("allow-telemetry");

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
        return String.join(DELIMITER, PREFIX + normalizedName, property.getName());
    }

    public static void main(String[] args) {
        KeyVaultProperties keyVaultProperties = new KeyVaultProperties();
        System.out.println(keyVaultProperties.toString());
    }
}
