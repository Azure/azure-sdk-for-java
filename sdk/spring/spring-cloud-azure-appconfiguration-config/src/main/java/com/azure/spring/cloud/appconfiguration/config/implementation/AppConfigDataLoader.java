// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;

public class AppConfigDataLoader implements ConfigDataLoader<AppConfigDataResource> {

    private Log logger = new DeferredLog();

    private Duration refreshInterval;
    
    private AppConfigDataResource resource;
    
    private AppConfigurationReplicaClientFactory replicaClientFactory;
    
    private AppConfigurationKeyVaultClientFactory keyVaultClientFactory;
    
    private StateHolder storeState = new StateHolder();
    
    private List<AppConfigurationPropertySource> sources = new ArrayList<>();

    public AppConfigDataLoader(DeferredLogFactory logFactory) {
        this.logger = logFactory.getLog(getClass());
    }

    @Override
    public ConfigData load(ConfigDataLoaderContext context, AppConfigDataResource resource)
        throws IOException, ConfigDataResourceNotFoundException {
        this.resource = resource;
        storeState.setNextForcedRefresh(refreshInterval);

        List<AppConfigurationPropertySource> sources = new ArrayList<>();

        if (resource.isConfigStoreEnabled()) {
            replicaClientFactory = context.getBootstrapContext()
                .get(AppConfigurationReplicaClientFactory.class);
            keyVaultClientFactory = context.getBootstrapContext()
                .get(AppConfigurationKeyVaultClientFactory.class);
            // There is only one Feature Set for all AppConfigurationPropertySources

            List<AppConfigurationReplicaClient> clients = replicaClientFactory
                .getAvailableClients(resource.getEndpoint(), true);

            boolean reloadFailed = false;

            for (AppConfigurationReplicaClient client : clients) {
                sources = new ArrayList<>();

                // TODO (mametcal) This should only trigger on refresh
                if (reloadFailed
                    && !AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(client, replicaClientFactory,
                        resource)) {
                    // This store doesn't have any changes where to refresh store did. Skipping Checking next.
                    continue;
                }

                // Reverse in order to add Profile specific properties earlier, and last profile comes first
                try {
                    sources.addAll(create(client));

                    logger.debug("PropertySource context.");
                    setupMonitoring(client);
                } catch (AppConfigurationStatusException e) {
                    reloadFailed = true;
                    replicaClientFactory.backoffClientClient(resource.getEndpoint(), client.getEndpoint());
                } catch (Exception e) {
                    storeState = failedToGeneratePropertySource(e);

                    // Not a retiable error
                    break;
                }
                if (sources.size() > 0) {
                    break;
                }
            }

            if (sources.size() == 0) {
                String message = "Failed to generate property sources for " + resource.getEndpoint();

                // Refresh failed for a config store ending attempt
                failedToGeneratePropertySource(new RuntimeException(message));
            }

        } else if (!resource.isConfigStoreEnabled()) {
            logger.info(
                String.format("Not loading configurations from {} as it is not enabled.", resource.getEndpoint()));
        } else {
            logger.warn(String.format("Not loading configurations from {} as it failed on startup.",
                resource.getEndpoint()));
        }

        StateHolder.updateState(storeState);

        return new ConfigData(sources);
    }

    private void setupMonitoring(AppConfigurationReplicaClient client) {
        AppConfigurationStoreMonitoring monitoring = resource.getMonitoring();

        if (resource.isFeatureFlagsEnabled()) {
            List<ConfigurationSetting> watchKeysFeatures = getFeatureFlagWatchKeys();
            storeState.setStateFeatureFlag(resource.getEndpoint(), watchKeysFeatures,
                monitoring.getFeatureFlagRefreshInterval());
        }

        if (monitoring.isEnabled()) {
            // Setting new ETag values for Watch
            List<ConfigurationSetting> watchKeysSettings = getWatchKeys(client);

            storeState.setState(resource.getEndpoint(), watchKeysSettings, monitoring.getRefreshInterval());
        }
        storeState.setLoadState(resource.getEndpoint(), true);
        storeState.setLoadStateFeatureFlag(resource.getEndpoint(), resource.isFeatureFlagsEnabled());
    }

    private List<ConfigurationSetting> getWatchKeys(AppConfigurationReplicaClient client) {
        List<ConfigurationSetting> watchKeysSettings = new ArrayList<>();
        for (AppConfigurationStoreTrigger trigger : resource.getMonitoring().getTriggers()) {
            ConfigurationSetting watchKey = client.getWatchKey(trigger.getKey(), trigger.getLabel());
            if (watchKey != null) {
                watchKeysSettings.add(watchKey);
            } else {
                watchKeysSettings.add(new ConfigurationSetting().setKey(trigger.getKey()).setLabel(trigger.getLabel()));
            }
        }
        return watchKeysSettings;
    }

    private List<ConfigurationSetting> getFeatureFlagWatchKeys() {
        List<ConfigurationSetting> watchKeysFeatures = new ArrayList<>();
        if (resource.isFeatureFlagsEnabled()) {
            for (AppConfigurationPropertySource propertySource : sources) {
                if (propertySource instanceof AppConfigurationFeatureManagementPropertySource) {
                    watchKeysFeatures.addAll(
                        ((AppConfigurationFeatureManagementPropertySource) propertySource).getFeatureFlagSettings());
                }
            }
        }
        return watchKeysFeatures;
    }

    private StateHolder failedToGeneratePropertySource(Exception e) {
        logger.error("Fail fast is set and there was an error reading configuration from Azure App "
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
    private List<AppConfigurationPropertySource> create(AppConfigurationReplicaClient client) throws Exception {
        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();
        List<AppConfigurationKeyValueSelector> selects = resource.getSelects();

        if (resource.isFeatureFlagsEnabled()) {
            for (FeatureFlagKeyValueSelector selectedKeys : resource.getFeatureFlagSelects()) {
                AppConfigurationFeatureManagementPropertySource propertySource = new AppConfigurationFeatureManagementPropertySource(
                    resource.getEndpoint(), client,
                    selectedKeys.getKeyFilter(), selectedKeys.getLabelFilter(resource.getProfiles().getActive()));

                propertySource.initProperties(null);
                sourceList.add(propertySource);
            }
        }

        for (AppConfigurationKeyValueSelector selectedKeys : selects) {
            AppConfigurationPropertySource propertySource = null;

            if (StringUtils.hasText(selectedKeys.getSnapshotName())) {
                propertySource = new AppConfigurationSnapshotPropertySource(
                    selectedKeys.getSnapshotName() + "/" + resource.getEndpoint(), client, keyVaultClientFactory,
                    selectedKeys.getSnapshotName());
            } else {
                propertySource = new AppConfigurationApplicationSettingPropertySource(
                    selectedKeys.getKeyFilter() + resource.getEndpoint() + "/", client, keyVaultClientFactory,
                    selectedKeys.getKeyFilter(), selectedKeys.getLabelFilter(resource.getProfiles().getActive()));
            }
            propertySource.initProperties(resource.getTrimKeyPrefix());
            sourceList.add(propertySource);

        }

        return sourceList;
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
                logger.error("Failed to wait before fast fail.");
            }
        }
    }
}
