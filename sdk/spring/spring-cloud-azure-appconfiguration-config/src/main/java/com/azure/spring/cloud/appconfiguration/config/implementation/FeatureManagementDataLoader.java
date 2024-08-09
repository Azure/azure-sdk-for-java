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

import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;

public class FeatureManagementDataLoader implements ConfigDataLoader<FeatureManagementDataResource> {

    private static Log LOGGER = new DeferredLog();

    private Duration refreshInterval;

    private FeatureManagementDataResource resource;

    private AppConfigurationReplicaClientFactory replicaClientFactory;

    private StateHolder storeState = new StateHolder();

    private FeatureFlagClient featureFlagClient;

    public FeatureManagementDataLoader(DeferredLogFactory logFactory) {
        LOGGER = logFactory.getLog(getClass());
    }

    @Override
    public ConfigData load(ConfigDataLoaderContext context, FeatureManagementDataResource resource)
        throws IOException, ConfigDataResourceNotFoundException {
        
        if (context.getBootstrapContext().isRegistered(FeatureFlagClient.class)) {
            this.featureFlagClient = context.getBootstrapContext().get(FeatureFlagClient.class);
        } else {
            this.featureFlagClient = new FeatureFlagClient();
            context.getBootstrapContext().registerIfAbsent(FeatureFlagClient.class, InstanceSupplier.from(() -> this.featureFlagClient));
        }
        
        
        this.resource = resource;
        storeState.setNextForcedRefresh(refreshInterval);

        if (resource.isEnabled()) {
            replicaClientFactory = context.getBootstrapContext()
                .get(AppConfigurationReplicaClientFactory.class);
            // There is only one Feature Set for all AppConfigurationPropertySources

            List<AppConfigurationReplicaClient> clients = replicaClientFactory
                .getAvailableClients(resource.getEndpoint(), true);

            List<AppConfigurationPropertySource> sourceList = new ArrayList<>();

            // Feature Management needs to be set in the last config store.

            for (AppConfigurationReplicaClient client : clients) {
                sourceList = new ArrayList<>();

               // if (!AppConfigurationRefreshUtil.refreshStoreFeatureFlagCheck(true, client)) {
                    // This store doesn't have any changes where to refresh store did. Skipping Checking next.
                   // continue;
               // }

                // Reverse in order to add Profile specific properties earlier, and last profile comes first
                try {
                    List<FeatureFlags> featureFlags = createFeatureFlags(client);

                    AppConfigurationStoreMonitoring monitoring = resource.getMonitoring();

                    storeState.setStateFeatureFlag(resource.getEndpoint(), featureFlags,
                        monitoring.getFeatureFlagRefreshInterval());

                    storeState.setLoadState(resource.getEndpoint(), true);
                } catch (AppConfigurationStatusException e) {
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

        return new ConfigData(List.of(new AppConfigurationFeatureManagementPropertySource(featureFlagClient)));
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