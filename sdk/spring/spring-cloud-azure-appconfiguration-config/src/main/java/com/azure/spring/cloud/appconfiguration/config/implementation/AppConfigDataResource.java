// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import org.springframework.boot.context.config.ConfigDataResource;
import org.springframework.boot.context.config.Profiles;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class AppConfigDataResource extends ConfigDataResource {

    private final ConfigStore configStore;

    private final Profiles profiles;

    private final AppConfigurationProviderProperties appProperties;

    public AppConfigDataResource(ConfigStore configStore, Profiles profiles,
        AppConfigurationProviderProperties appProperties) {
        this.configStore = configStore;
        this.profiles = profiles;
        this.appProperties = appProperties;
    }

    /**
     * @return the profiles
     */
    public Profiles getProfiles() {
        return profiles;
    }

    /**
     * @return the configStore
     */
    public ConfigStore getConfigStore() {
        return configStore;
    }

    /**
     * @return the appProperties
     */
    public AppConfigurationProviderProperties getAppProperties() {
        return appProperties;
    }

}