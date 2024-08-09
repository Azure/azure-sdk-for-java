// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;

public class AzureAppConfigDataLoader implements ConfigDataLoader<AzureAppConfigDataResource> {

    private static Log LOGGER = new DeferredLog();

    private Duration refreshInterval;

    private AzureAppConfigDataResource resource;

    private AppConfigurationReplicaClientFactory replicaClientFactory;

    private AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

    private StateHolder storeState = new StateHolder();

    private FeatureFlagClient featureFlagClient;

    public AzureAppConfigDataLoader(DeferredLogFactory logFactory) {
        LOGGER = logFactory.getLog(getClass());
    }

    @Override
    public ConfigData load(ConfigDataLoaderContext context, AzureAppConfigDataResource resource)
        throws IOException, ConfigDataResourceNotFoundException {
        this.resource = resource;
        storeState.setNextForcedRefresh(refreshInterval);

        if (context.getBootstrapContext().isRegistered(FeatureFlagClient.class)) {
            this.featureFlagClient = context.getBootstrapContext().get(FeatureFlagClient.class);
        } else {
            this.featureFlagClient = new FeatureFlagClient();
            context.getBootstrapContext().registerIfAbsent(FeatureFlagClient.class, InstanceSupplier.from(() -> this.featureFlagClient));
        }
        

        List<EnumerablePropertySource<?>> sourceList = new ArrayList<>();

        if (resource.isConfigStoreEnabled()) {
            replicaClientFactory = context.getBootstrapContext()
                .get(AppConfigurationReplicaClientFactory.class);
            keyVaultClientFactory = context.getBootstrapContext()
                .get(AppConfigurationKeyVaultClientFactory.class);
            // There is only one Feature Set for all AppConfigurationPropertySources

            List<AppConfigurationReplicaClient> clients = replicaClientFactory
                .getAvailableClients(resource.getEndpoint(), true);

            boolean reloadFailed = false;

            // Feature Management needs to be set in the last config store.

            for (AppConfigurationReplicaClient client : clients) {
                sourceList = new ArrayList<>();

                if (reloadFailed
                    && !AppConfigurationRefreshUtil.refreshStoreCheck(client,
                        replicaClientFactory.findOriginForEndpoint(client.getEndpoint()))) {
                    // This store doesn't have any changes where to refresh store did. Skipping Checking next.
                    continue;
                }

                // Reverse in order to add Profile specific properties earlier, and last profile comes first
                try {
                    sourceList.addAll(createSettings(client));
                    List<FeatureFlags> featureFlags = createFeatureFlags(client);

                    LOGGER.debug("PropertySource context.");
                    AppConfigurationStoreMonitoring monitoring = resource.getMonitoring();

                    storeState.setStateFeatureFlag(resource.getEndpoint(), featureFlags,
                        monitoring.getFeatureFlagRefreshInterval());

                    if (monitoring.isEnabled()) {
                        // Setting new ETag values for Watch
                        List<ConfigurationSetting> watchKeysSettings = monitoring.getTriggers().stream()
                            .map(trigger -> client.getWatchKey(trigger.getKey(), trigger.getLabel())).toList();

                        storeState.setState(resource.getEndpoint(), watchKeysSettings, monitoring.getRefreshInterval());
                    }
                    storeState.setLoadState(resource.getEndpoint(), true);
                } catch (AppConfigurationStatusException e) {
                    reloadFailed = true;
                    replicaClientFactory.backoffClientClient(resource.getEndpoint(), client.getEndpoint());
                } catch (Exception e) {
                    failedToGeneratePropertySource(e);

                    // Not a retiable error
                    break;
                }
                if (sourceList.size() > 0) {
                    break;
                }
            }
        }

        StateHolder.updateState(storeState);
        sourceList.add(new AppConfigurationFeatureManagementPropertySource(featureFlagClient));
        return new ConfigData(sourceList);
    }

    private void failedToGeneratePropertySource(Exception e) {
        LOGGER.error("Fail fast is set and there was an error reading configuration from Azure App "
            + "Configuration store " + resource.getEndpoint() + ".");
        delayException();
        throw new RuntimeException("Failed to generate property sources for " + resource.getEndpoint(), e);
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
    private List<AppConfigurationPropertySource> createSettings(AppConfigurationReplicaClient client) throws Exception {
        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();
        List<AppConfigurationKeyValueSelector> selects = resource.getSelects();

        // TODO (mametcal): figure out profiles
        List<String> profiles = List.of();

        for (AppConfigurationKeyValueSelector selectedKeys : selects) {
            AppConfigurationPropertySource propertySource = null;

            if (StringUtils.hasText(selectedKeys.getSnapshotName())) {
                propertySource = new AppConfigurationSnapshotPropertySource(
                    selectedKeys.getSnapshotName() + "/" + resource.getEndpoint(), client, keyVaultClientFactory,
                    selectedKeys.getSnapshotName(), featureFlagClient);
            } else {
                propertySource = new AppConfigurationApplicationSettingPropertySource(
                    selectedKeys.getKeyFilter() + resource.getEndpoint() + "/", client, keyVaultClientFactory,
                    selectedKeys.getKeyFilter(), selectedKeys.getLabelFilter(profiles));
            }
            propertySource.initProperties(resource.getTrimKeyPrefix());
            sourceList.add(propertySource);

        }

        return sourceList;
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
    private List<FeatureFlags> createFeatureFlags(AppConfigurationReplicaClient client) throws Exception {
        List<FeatureFlags> featureFlagWatchKeys = new ArrayList<>();
        List<String> profiles = resource.getProfiles().getActive();
        for (FeatureFlagKeyValueSelector selectedKeys : resource.getFeatureFlagSelects()) {
            List<FeatureFlags> storesFeatureFlags = featureFlagClient.loadFeatureFlags(client,
                selectedKeys.getKeyFilter(), selectedKeys.getLabelFilter(profiles));
            storesFeatureFlags.forEach(featureFlags -> featureFlags.setResource(resource));
            featureFlagWatchKeys.addAll(storesFeatureFlags);
        }

        return featureFlagWatchKeys;
    }
    
    private void delayException() {
        Instant currentDate = Instant.now();
        Instant preKillTIme = resource.getAppProperties().getStartDate()
            .plusSeconds(resource.getAppProperties().getPrekillTime());
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