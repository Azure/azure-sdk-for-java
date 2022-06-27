// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.azure.spring.cloud.config.ConnectionManager;
import com.azure.spring.cloud.config.resource.AppConfigManagedIdentityProperties;

/**
 * Properties for all Azure App Configuration stores that are loaded.
 */
@Validated
@ConfigurationProperties(prefix = AppConfigurationProperties.CONFIG_PREFIX)
@Import({ AppConfigurationProviderProperties.class })
public final class AppConfigurationProperties {

    /**
     * Prefix for client configurations for connecting to configuration stores.
     */
    public static final String CONFIG_PREFIX = "spring.cloud.azure.appconfiguration";

    /**
     * Separator for multiple labels.
     */
    public static final String LABEL_SEPARATOR = ",";

    /**
     * Context for loading configuration keys.
     */
    @NotEmpty
    private String defaultContext = "application";

    private boolean enabled = true;

    private List<ConfigStore> stores = new ArrayList<>();

    /**
     * Alternative to Spring application name, if not configured, fallback to default Spring application name
     **/
    private String name;

    @NestedConfigurationProperty
    private AppConfigManagedIdentityProperties managedIdentity;

    private boolean pushRefresh = true;

    private Duration refreshInterval;

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the stores
     */
    public List<ConfigStore> getStores() {
        return stores;
    }

    /**
     * @param stores the stores to set
     */
    public void setStores(List<ConfigStore> stores) {
        this.stores = stores;
    }

    /**
     * The prefixed used before all keys loaded.
     * @deprecated Use spring.cloud.azure.appconfiguration[0].selects
     * @return null
     */
    @Deprecated
    public String getDefaultContext() {
        return defaultContext;
    }

    /**
     * Overrides the default context of `application`.
     * @deprecated Use spring.cloud.azure.appconfiguration[0].selects
     * @param defaultContext Key Prefix.
     */
    @Deprecated
    public void setDefaultContext(String defaultContext) {
        this.defaultContext = defaultContext;
    }

    /**
     * Used to override the spring.application.name value
     * @deprecated Use spring.cloud.azure.appconfiguration[0].selects
     * @return name
     */
    @Deprecated
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Used to override the spring.application.name value
     * @deprecated Use spring.cloud.azure.appconfiguration[0].selects
     * @param name application name in config key.
     */
    @Deprecated
    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * @return the managedIdentity
     */
    public AppConfigManagedIdentityProperties getManagedIdentity() {
        return managedIdentity;
    }

    /**
     * @param managedIdentity the managedIdentity to set
     */
    public void setManagedIdentity(AppConfigManagedIdentityProperties managedIdentity) {
        this.managedIdentity = managedIdentity;
    }

    /**
     * @return the pushRefresh
     */
    public Boolean getPushRefresh() {
        return pushRefresh;
    }

    /**
     * @param pushRefresh the pushRefresh to set
     */
    public void setPushRefresh(Boolean pushRefresh) {
        this.pushRefresh = pushRefresh;
    }

    /**
     * @return the refreshInterval
     */
    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * @param refreshInterval the refreshInterval to set
     */
    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * Validates at least one store is configured for use and they are valid.
     */
    @PostConstruct
    public void validateAndInit() {
        Assert.notEmpty(this.stores, "At least one config store has to be configured.");

        this.stores.forEach(store -> {
            Assert.isTrue(
                StringUtils.hasText(store.getEndpoint()) || StringUtils.hasText(store.getConnectionString())
                    || store.getEndpoints().size() > 0 || store.getConnectionStrings().size() > 0,
                "Either configuration store name or connection string should be configured.");
            store.validateAndInit();
        });

        Map<String, Boolean> existingEndpoints = new HashMap<>();

        for (ConfigStore store : this.stores) {
            if (StringUtils.hasText(store.getEndpoint())) {
                if (existingEndpoints.containsKey(store.getEndpoint())) {
                    throw new IllegalArgumentException("Duplicate store name exists.");
                }
                existingEndpoints.put(store.getEndpoint(), true);
            }
            if (store.getEndpoints().size() > 0) {
                for (String endpoint : store.getEndpoints()) {
                    if (existingEndpoints.containsKey(endpoint)) {
                        throw new IllegalArgumentException("Duplicate store name exists.");
                    }
                    existingEndpoints.put(endpoint, true);
                }
            }
        }
        if (refreshInterval != null){
            Assert.isTrue(refreshInterval.getSeconds() >= 1, "Minimum refresh interval time is 1 Second.");
        }
    }
}
