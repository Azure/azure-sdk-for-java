// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.microsoft.azure.spring.cloud.config.resource.AppConfigManagedIdentityProperties;
import com.microsoft.azure.spring.cloud.config.stores.ConfigStore;

@Validated
@ConfigurationProperties(prefix = AppConfigurationProperties.CONFIG_PREFIX)
@Import({ AppConfigurationProviderProperties.class })
public class AppConfigurationProperties {
    public static final String CONFIG_PREFIX = "spring.cloud.azure.appconfiguration";

    public static final String LABEL_SEPARATOR = ",";

    private boolean enabled = true;

    private List<ConfigStore> stores = new ArrayList<>();

    @NotEmpty
    private String defaultContext = "application";

    // Alternative to Spring application name, if not configured, fallback to default
    // Spring application name
    @Nullable
    private String name;

    @NestedConfigurationProperty
    private AppConfigManagedIdentityProperties managedIdentity;

    // Profile separator for the key name, e.g., /foo-app_dev/db.connection.key
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_@]+$")
    private String profileSeparator = "_";

    private Duration cacheExpiration = Duration.ofSeconds(30);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<ConfigStore> getStores() {
        return stores;
    }

    public void setStores(List<ConfigStore> stores) {
        this.stores = stores;
    }

    public String getDefaultContext() {
        return defaultContext;
    }

    public void setDefaultContext(String defaultContext) {
        this.defaultContext = defaultContext;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public AppConfigManagedIdentityProperties getManagedIdentity() {
        return managedIdentity;
    }

    public void setManagedIdentity(AppConfigManagedIdentityProperties managedIdentity) {
        this.managedIdentity = managedIdentity;
    }

    public String getProfileSeparator() {
        return profileSeparator;
    }

    public void setProfileSeparator(String profileSeparator) {
        this.profileSeparator = profileSeparator;
    }

    public Duration getCacheExpiration() {
        return cacheExpiration;
    }

    /**
     * The minimum time between checks. The minimum valid cache time is 1s. The default
     * cache time is 30s.
     *
     * @param cacheExpiration minimum time between refresh checks
     */
    public void setCacheExpiration(Duration cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
    }

    @PostConstruct
    public void validateAndInit() {
        Assert.notEmpty(this.stores, "At least one config store has to be configured.");

        this.stores.forEach(store -> {
            Assert.isTrue(StringUtils.hasText(store.getEndpoint()) ||
                    StringUtils.hasText(store.getConnectionString()),
                    "Either configuration store name or connection string should be configured.");
            store.validateAndInit();
        });

        int uniqueStoreSize = (int) this.stores.stream().map(ConfigStore::getEndpoint).distinct().count();
        Assert.isTrue(this.stores.size() == uniqueStoreSize, "Duplicate store name exists.");
        Assert.isTrue(cacheExpiration.getSeconds() >= 1, "Minimum Watch time is 1 Second.");
    }
}
