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

    private final AppConfigurationKeyVaultClientFactory kvcf;

    private final AppConfigurationReplicaClientFactory rcf;

    private final AppConfigurationReplicaClientsBuilder rcb;

    public AppConfigDataResource(ConfigStore configStore, Profiles profiles, AppConfigurationKeyVaultClientFactory kvcf,
        AppConfigurationReplicaClientFactory rcf, AppConfigurationReplicaClientsBuilder rcb,
        AppConfigurationProviderProperties appProperties) {
        this.configStore = configStore;
        this.profiles = profiles;
        this.kvcf = kvcf;
        this.rcf = rcf;
        this.rcb = rcb;
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
     * @return the kvcf
     */
    public AppConfigurationKeyVaultClientFactory getKvcf() {
        return kvcf;
    }

    /**
     * @return the rcf
     */
    public AppConfigurationReplicaClientFactory getRcf() {
        return rcf;
    }

    /**
     * @return the rcb
     */
    public AppConfigurationReplicaClientsBuilder getRcb() {
        return rcb;
    }

    /**
     * @return the appProperties
     */
    public AppConfigurationProviderProperties getAppProperties() {
        return appProperties;
    }

}
