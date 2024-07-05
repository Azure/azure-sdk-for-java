// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration.BOOTSTRAP_PROPERTY_SOURCE_NAME;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;

/**
 * Locates Azure App Configuration Property Sources.
 */
public final class AppConfigurationPropertySourceLocator implements PropertySourceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPropertySourceLocator.class);

    private static final String PROPERTY_SOURCE_NAME = "azure-config-store";

    private static final String REFRESH_ARGS_PROPERTY_SOURCE = "refreshArgs";

    private final List<ConfigStore> configStores;

    private final AppConfigurationProviderProperties appProperties;

    private final AppConfigurationReplicaClientFactory clientFactory;

    private final AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

    private final FeatureFlagClient featureFlagClient;

    private final ReplicaLookUp replicaLookUp;

    private Duration refreshInterval;

    static final AtomicBoolean STARTUP = new AtomicBoolean(true);

    /**
     * Loads all Azure App Configuration Property Sources configured.
     * 
     * @param properties Configurations for stores to be loaded.
     * @param appProperties Configurations for the library.
     * @param clientFactory factory for creating clients for connecting to Azure App Configuration.
     * @param keyVaultClientFactory factory for creating clients for connecting to Azure Key Vault
     * @param featureFlagLoader service for loadingFeatureFlags.
     */
    public AppConfigurationPropertySourceLocator(AppConfigurationProviderProperties appProperties,
        AppConfigurationReplicaClientFactory clientFactory, AppConfigurationKeyVaultClientFactory keyVaultClientFactory,
        Duration refreshInterval, List<ConfigStore> configStores, ReplicaLookUp replicaLookUp,
        FeatureFlagClient featureFlagLoader) {
        this.refreshInterval = refreshInterval;
        this.appProperties = appProperties;
        this.configStores = configStores;
        this.clientFactory = clientFactory;
        this.keyVaultClientFactory = keyVaultClientFactory;
        this.replicaLookUp = replicaLookUp;
        this.featureFlagClient = featureFlagLoader;

        BackoffTimeCalculator.setDefaults(appProperties.getDefaultMaxBackoff(), appProperties.getDefaultMinBackoff());
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return null;
        }
        replicaLookUp.updateAutoFailoverEndpoints();

        ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
        boolean currentlyLoaded = env.getPropertySources().stream().anyMatch(source -> {
            String storeName = configStores.get(0).getEndpoint();
            if (configStores.get(0).getSelects().size() == 0) {
                return false;
            }
            AppConfigurationKeyValueSelector selectedKey = configStores.get(0).getSelects().get(0);
            return source.getName()
                .startsWith(BOOTSTRAP_PROPERTY_SOURCE_NAME + "-" + selectedKey.getKeyFilter() + storeName + "/");
        });
        if (currentlyLoaded && !env.getPropertySources().contains(REFRESH_ARGS_PROPERTY_SOURCE)) {
            return null;
        }

        List<String> profiles = Arrays.asList(env.getActiveProfiles());

        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        Collections.reverse(configStores); // Last store has the highest precedence

        StateHolder newState = new StateHolder();
        newState.setNextForcedRefresh(refreshInterval);

        // Feature Management needs to be set in the last config store.
        for (ConfigStore configStore : configStores) {
            boolean loadNewPropertySources = STARTUP.get() || StateHolder.getLoadState(configStore.getEndpoint());

            if (configStore.isEnabled() && loadNewPropertySources) {
                // There is only one Feature Set for all AppConfigurationPropertySources

                List<AppConfigurationReplicaClient> clients = clientFactory
                    .getAvailableClients(configStore.getEndpoint(), true);

                boolean generatedPropertySources = false;

                List<AppConfigurationPropertySource> sourceList = new ArrayList<>();
                boolean reloadFailed = false;

                for (AppConfigurationReplicaClient client : clients) {
                    sourceList = new ArrayList<>();

                    if (!STARTUP.get() && reloadFailed && !AppConfigurationRefreshUtil
                        .checkStoreAfterRefreshFailed(client, clientFactory, configStore.getFeatureFlags())) {
                        // This store doesn't have any changes where to refresh store did. Skipping Checking next.
                        continue;
                    }

                    // Reverse in order to add Profile specific properties earlier, and last profile comes first
                    try {
                        List<AppConfigurationPropertySource> sources = createSettings(client, configStore, profiles);
                        List<FeatureFlags> featureFlags = createFeatureFlags(client, configStore, profiles);
                        sourceList.addAll(sources);

                        LOGGER.debug("PropertySource context.");
                        setupMonitoring(configStore, client, sources, newState, featureFlags);

                        generatedPropertySources = true;
                    } catch (AppConfigurationStatusException e) {
                        reloadFailed = true;
                        clientFactory.backoffClientClient(configStore.getEndpoint(), client.getEndpoint());
                    } catch (Exception e) {
                        newState = failedToGeneratePropertySource(configStore, newState, e);

                        // Not a retiable error
                        break;
                    }
                    if (generatedPropertySources) {
                        break;
                    }
                }

                if (generatedPropertySources) {
                    // Updating list of propertySources
                    sourceList.forEach(composite::addPropertySource);
                } else if (!STARTUP.get() || (configStore.isFailFast() && STARTUP.get())) {
                    String message = "Failed to generate property sources for " + configStore.getEndpoint();

                    // Refresh failed for a config store ending attempt
                    failedToGeneratePropertySource(configStore, newState, new RuntimeException(message));
                }

                if (featureFlagClient.getProperties().size() > 0) {
                    // This can be true if feature flags are enabled or if a Snapshot contained feature flags
                    AppConfigurationFeatureManagementPropertySource acfmps = new AppConfigurationFeatureManagementPropertySource(
                        featureFlagClient);
                    composite.addPropertySource(acfmps);
                }

            } else if (!configStore.isEnabled() && loadNewPropertySources) {
                LOGGER.info("Not loading configurations from {} as it is not enabled.", configStore.getEndpoint());
            } else {
                LOGGER.warn("Not loading configurations from {} as it failed on startup.", configStore.getEndpoint());
            }
        }

        StateHolder.updateState(newState);
        STARTUP.set(false);

        return composite;
    }

    private void setupMonitoring(ConfigStore configStore, AppConfigurationReplicaClient client,
        List<AppConfigurationPropertySource> sources, StateHolder newState, List<FeatureFlags> featureFlags) {
        AppConfigurationStoreMonitoring monitoring = configStore.getMonitoring();

        if (configStore.getFeatureFlags().getEnabled()) {
            newState.setStateFeatureFlag(configStore.getEndpoint(), featureFlags,
                monitoring.getFeatureFlagRefreshInterval());
        }

        if (monitoring.isEnabled()) {
            // Setting new ETag values for Watch
            List<ConfigurationSetting> watchKeysSettings = monitoring.getTriggers().stream()
                .map(trigger -> client.getWatchKey(trigger.getKey(), trigger.getLabel())).toList();

            newState.setState(configStore.getEndpoint(), watchKeysSettings, monitoring.getRefreshInterval());
        }
        newState.setLoadState(configStore.getEndpoint(), true, configStore.isFailFast());
    }

    private StateHolder failedToGeneratePropertySource(ConfigStore configStore, StateHolder newState, Exception e) {
        String message = "Failed to generate property sources for " + configStore.getEndpoint();
        if (!STARTUP.get()) {
            // Need to check for refresh first, or reset will never happen if fail fast is true.
            LOGGER.error("Refreshing failed while reading configuration from Azure App Configuration store "
                + configStore.getEndpoint() + ".");

            if (refreshInterval != null) {
                // The next refresh will happen sooner if refresh interval is expired.
                newState.updateNextRefreshTime(refreshInterval, appProperties.getDefaultMinBackoff());
            }
            throw new RuntimeException(message, e);
        } else if (configStore.isFailFast()) {
            LOGGER.error("Fail fast is set and there was an error reading configuration from Azure App "
                + "Configuration store " + configStore.getEndpoint() + ".");
            delayException();
            throw new RuntimeException(message, e);
        } else {
            LOGGER.warn(
                "Unable to load configuration from Azure AppConfiguration store " + configStore.getEndpoint() + ".", e);
            newState.setLoadState(configStore.getEndpoint(), false, configStore.isFailFast());
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
    private List<AppConfigurationPropertySource> createSettings(AppConfigurationReplicaClient client, ConfigStore store,
        List<String> profiles) throws Exception {
        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();
        List<AppConfigurationKeyValueSelector> selects = store.getSelects();

        for (AppConfigurationKeyValueSelector selectedKeys : selects) {
            AppConfigurationPropertySource propertySource = null;

            if (StringUtils.hasText(selectedKeys.getSnapshotName())) {
                propertySource = new AppConfigurationSnapshotPropertySource(
                    selectedKeys.getSnapshotName() + "/" + store.getEndpoint(), client, keyVaultClientFactory,
                    selectedKeys.getSnapshotName(), featureFlagClient);
            } else {
                propertySource = new AppConfigurationApplicationSettingPropertySource(
                    selectedKeys.getKeyFilter() + store.getEndpoint() + "/", client, keyVaultClientFactory,
                    selectedKeys.getKeyFilter(), selectedKeys.getLabelFilter(profiles));
            }
            propertySource.initProperties(store.getTrimKeyPrefix());
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
    private List<FeatureFlags> createFeatureFlags(AppConfigurationReplicaClient client, ConfigStore store,
        List<String> profiles) throws Exception {
        List<FeatureFlags> featureFlagWatchKeys = new ArrayList<>();
        if (store.getFeatureFlags().getEnabled()) {
            for (FeatureFlagKeyValueSelector selectedKeys : store.getFeatureFlags().getSelects()) {
                List<FeatureFlags> storesFeatureFlags = featureFlagClient.loadFeatureFlags(client,
                    selectedKeys.getKeyFilter(), selectedKeys.getLabelFilter(profiles));
                storesFeatureFlags.forEach(featureFlags -> featureFlags.setConfigStore(store));
                featureFlagWatchKeys.addAll(storesFeatureFlags);
            }
        }
        return featureFlagWatchKeys;
    }

    private void delayException() {
        Instant currentDate = Instant.now();
        Instant preKillTIme = appProperties.getStartDate().plusSeconds(appProperties.getPrekillTime());
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
