// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.keyvault.config;

import com.microsoft.azure.spring.cloud.keyvault.config.auth.Credentials;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

/**
 * Configuration properties for the Azure Key Vault integration with Spring Cloud Config.
 */
@ConfigurationProperties(KeyVaultConfigProperties.CONFIG_PREFIX)
@Validated
public class KeyVaultConfigProperties {
    public static final String CONFIG_PREFIX = "spring.cloud.azure.keyvault.config";
    public static final String ENABLED = CONFIG_PREFIX + ".enabled";

    @NestedConfigurationProperty
    @NotNull
    private Credentials credentials;

    private boolean enabled = true;

    private boolean failFast = true;

    private String name;

    private String activeProfile;

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isFailFast() {
        return failFast;
    }

    public void setFailFast(boolean failFast) {
        this.failFast = failFast;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }
}
