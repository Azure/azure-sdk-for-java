// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AppConfigDataLoader implements ConfigDataLoader<AppConfigDataResource> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigDataLoader.class);

    private static final String PROPERTY_SOURCE_NAME = "azure-config-store";

    private static final String REFRESH_ARGS_PROPERTY_SOURCE = "refreshArgs";

    private Duration refreshInterval;

    private ConfigurableBootstrapContext bootstrapContext;

    /**
     * Loads all Azure App Configuration Property Sources configured.
     * 
     * @param properties Configurations for stores to be loaded.
     * @param appProperties Configurations for the library.
     * @param clientFactory factory for creating clients for connecting to Azure App Configuration.
     * @param keyVaultClientFactory factory for creating clients for connecting to Azure Key Vault
     */
    public AppConfigDataLoader(ConfigurableBootstrapContext bootstrapContext) {
        this.bootstrapContext = bootstrapContext;
        // this.refreshInterval = refreshInterval;
        // this.appProperties = appProperties;
        // this.clientFactory = clientFactory;
        // this.keyVaultClientFactory = keyVaultClientFactory;

        // BackoffTimeCalculator.setDefaults(appProperties.getDefaultMaxBackoff(),
        // appProperties.getDefaultMinBackoff());
    }

    @Override
    public ConfigData load(ConfigDataLoaderContext context, AppConfigDataResource resource)
        throws IOException, ConfigDataResourceNotFoundException {

        List<String> profiles = resource.getProfiles().getActive();
        ConfigStore configStore = resource.getConfigStore();

        StateHolder newState = new StateHolder();
        newState.setNextForcedRefresh(refreshInterval);

        List<AppConfigurationPropertySource> sources = new ArrayList<>();

        if (configStore.isEnabled()) {
            // There is only one Feature Set for all AppConfigurationPropertySources

            List<AppConfigurationReplicaClient> clients = resource.getRcf()
                .getAvailableClients(configStore.getEndpoint(), true);

            boolean generatedPropertySources = false;

            List<AppConfigurationPropertySource> sourceList = new ArrayList<>();
            boolean reloadFailed = false;

            for (AppConfigurationReplicaClient client : clients) {
                sourceList = new ArrayList<>();

                if (reloadFailed
                    && !AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(client, resource.getRcf(),
                        configStore.getFeatureFlags(), profiles)) {
                    // This store doesn't have any changes where to refresh store did. Skipping Checking next.
                    continue;
                }

                // Reverse in order to add Profile specific properties earlier, and last profile comes first
                try {
                    sources.addAll(create(client, configStore, profiles, resource));
                    sourceList.addAll(sources);

                    LOGGER.debug("PropertySource context.");
                    //setupMonitoring(configStore, client, sources, newState);

                    generatedPropertySources = true;
                } catch (AppConfigurationStatusException e) {
                    reloadFailed = true;
                    resource.getRcf().backoffClientClient(configStore.getEndpoint(), client.getEndpoint());
                } catch (Exception e) {
                    newState = failedToGeneratePropertySource(configStore, newState, e, resource);

                    // Not a retiable error
                    break;
                }
                if (generatedPropertySources) {
                    break;
                }
            }

            if (!generatedPropertySources && configStore.isFailFast()) {
                String message = "Failed to generate property sources for " + configStore.getEndpoint();

                // Refresh failed for a config store ending attempt
                failedToGeneratePropertySource(configStore, newState, new RuntimeException(message), resource);
            }

        } else if (!configStore.isEnabled()) {
            LOGGER.info("Not loading configurations from {} as it is not enabled.", configStore.getEndpoint());
        } else {
            LOGGER.warn("Not loading configurations from {} as it failed on startup.", configStore.getEndpoint());
        }

        StateHolder.updateState(newState);

        ConfigData data = new ConfigData(sources);
        return data;
    }

    private void setupMonitoring(ConfigStore configStore, AppConfigurationReplicaClient client,
        List<AppConfigurationPropertySource> sources, StateHolder newState) {
        AppConfigurationStoreMonitoring monitoring = configStore.getMonitoring();

        if (configStore.getFeatureFlags().getEnabled()) {
            List<ConfigurationSetting> watchKeysFeatures = getFeatureFlagWatchKeys(configStore, sources);
            newState.setStateFeatureFlag(configStore.getEndpoint(), watchKeysFeatures,
                monitoring.getFeatureFlagRefreshInterval());
        }

        if (monitoring.isEnabled()) {
            // Setting new ETag values for Watch
            List<ConfigurationSetting> watchKeysSettings = getWatchKeys(client, monitoring.getTriggers());

            newState.setState(configStore.getEndpoint(), watchKeysSettings, monitoring.getRefreshInterval());
        }
        newState.setLoadState(configStore.getEndpoint(), true, configStore.isFailFast());
        newState.setLoadStateFeatureFlag(configStore.getEndpoint(), configStore.getFeatureFlags().getEnabled(),
            configStore.isFailFast());
    }

    private List<ConfigurationSetting> getWatchKeys(AppConfigurationReplicaClient client,
        List<AppConfigurationStoreTrigger> triggers) {
        List<ConfigurationSetting> watchKeysSettings = new ArrayList<>();
        for (AppConfigurationStoreTrigger trigger : triggers) {
            ConfigurationSetting watchKey = client.getWatchKey(trigger.getKey(), trigger.getLabel());
            if (watchKey != null) {
                watchKeysSettings.add(watchKey);
            } else {
                watchKeysSettings
                    .add(new ConfigurationSetting().setKey(trigger.getKey())
                        .setLabel(trigger.getLabel()));
            }
        }
        return watchKeysSettings;
    }

    private List<ConfigurationSetting> getFeatureFlagWatchKeys(ConfigStore configStore,
        List<AppConfigurationPropertySource> sources) {
        List<ConfigurationSetting> watchKeysFeatures = new ArrayList<>();
        if (configStore.getFeatureFlags().getEnabled()) {
            for (AppConfigurationPropertySource propertySource : sources) {
                if (propertySource instanceof AppConfigurationFeatureManagementPropertySource) {
                    watchKeysFeatures.addAll(
                        ((AppConfigurationFeatureManagementPropertySource) propertySource).getFeatureFlagSettings());
                }
            }
        }
        return watchKeysFeatures;
    }

    private StateHolder failedToGeneratePropertySource(ConfigStore configStore, StateHolder newState, Exception e, AppConfigDataResource resource) {
        String message = "Failed to generate property sources for " + configStore.getEndpoint();
        if (configStore.isFailFast()) {
            LOGGER.error("Fail fast is set and there was an error reading configuration from Azure App "
                + "Configuration store " + configStore.getEndpoint() + ".");
            delayException(resource);
            throw new RuntimeException(message, e);
        } else {
            LOGGER.warn(
                "Unable to load configuration from Azure AppConfiguration store " + configStore.getEndpoint() + ".", e);
            newState.setLoadState(configStore.getEndpoint(), false, configStore.isFailFast());
            newState.setLoadStateFeatureFlag(configStore.getEndpoint(), false, configStore.isFailFast());
        }
        return newState;
    }

    /**
     * Creates a new set of AppConfigurationPropertySources, 1 per Label.
     *
     * @param client client for connecting to App Configuration
     * @param store Config Store the PropertySource is being generated from
     * @param profiles active profiles to be used as labels. it needs to be in the last one.
     * @return a list of AppConfigurationPropertySources
     * @throws Exception creating a property source failed
     */
    private List<AppConfigurationPropertySource> create(AppConfigurationReplicaClient client, ConfigStore store,
        List<String> profiles, AppConfigDataResource resource) throws Exception {
        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();
        List<AppConfigurationKeyValueSelector> selects = store.getSelects();

        if (store.getFeatureFlags().getEnabled()) {
            for (FeatureFlagKeyValueSelector selectedKeys : store.getFeatureFlags().getSelects()) {
                AppConfigurationFeatureManagementPropertySource propertySource = new AppConfigurationFeatureManagementPropertySource(
                    store.getEndpoint(), client, selectedKeys.getKeyFilter(),
                    selectedKeys.getLabelFilter(profiles));

                propertySource.initProperties();
                sourceList.add(propertySource);
            }
        }

        for (AppConfigurationKeyValueSelector selectedKeys : selects) {
            AppConfigurationApplicationSettingPropertySource propertySource = new AppConfigurationApplicationSettingPropertySource(
                store.getEndpoint(), client, resource.getKvcf(), selectedKeys.getKeyFilter(),
                selectedKeys.getLabelFilter(profiles), resource.getAppProperties().getMaxRetryTime());
            propertySource.initProperties();
            sourceList.add(propertySource);
        }

        return sourceList;
    }

    private void delayException(AppConfigDataResource resource) {
        Instant currentDate = Instant.now();
        Instant preKillTIme = resource.getAppProperties().getStartDate().plusSeconds(resource.getAppProperties().getPrekillTime());
        if (currentDate.isBefore(preKillTIme)) {
            long diffInMillies = Math.abs(preKillTIme.toEpochMilli() - currentDate.toEpochMilli());
            try {
                Thread.sleep(diffInMillies);
            } catch (InterruptedException e) {
                LOGGER.error("Failed to wait before fast fail.");
            }
        }
    }
}
