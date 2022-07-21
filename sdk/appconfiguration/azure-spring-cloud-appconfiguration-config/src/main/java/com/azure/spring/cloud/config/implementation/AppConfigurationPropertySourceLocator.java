// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.ReflectionUtils;

import com.azure.core.http.rest.PagedIterable;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.ClientFactory;
import com.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.azure.spring.cloud.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.config.NormalizeNull;
import com.azure.spring.cloud.config.SecretClientBuilderSetup;
import com.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.config.properties.ConfigStore;

/**
 * Locates Azure App Configuration Property Sources.
 */
public final class AppConfigurationPropertySourceLocator implements PropertySourceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPropertySourceLocator.class);

    private static final String PROPERTY_SOURCE_NAME = "azure-config-store";

    private static final String REFRESH_ARGS_PROPERTY_SOURCE = "refreshArgs";

    private final AppConfigurationProperties properties;

    private final List<ConfigStore> configStores;

    private final AppConfigurationProviderProperties appProperties;

    private final ClientFactory clientFactory;

    private final KeyVaultCredentialProvider keyVaultCredentialProvider;

    private final SecretClientBuilderSetup keyVaultClientProvider;

    private final KeyVaultSecretProvider keyVaultSecretProvider;

    private static final AtomicBoolean CONFIG_LOADED = new AtomicBoolean(false);

    static final AtomicBoolean STARTUP = new AtomicBoolean(true);

    /**
     * Loads all Azure App Configuration Property Sources configured.
     * @param properties Configurations for stores to be loaded.
     * @param appProperties Configurations for the library.
     * @param clients Clients for connecting to Azure App Configuration.
     * @param keyVaultCredentialProvider optional provider for Key Vault Credentials
     * @param keyVaultClientProvider optional provider for modifying the Key Vault Client
     * @param keyVaultSecretProvider optional provider for loading secrets instead of connecting to Key Vault
     */
    public AppConfigurationPropertySourceLocator(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, ClientFactory clientFactory,
        KeyVaultCredentialProvider keyVaultCredentialProvider, SecretClientBuilderSetup keyVaultClientProvider,
        KeyVaultSecretProvider keyVaultSecretProvider) {
        this.properties = properties;
        this.appProperties = appProperties;
        this.configStores = properties.getStores();
        this.clientFactory = clientFactory;
        this.keyVaultCredentialProvider = keyVaultCredentialProvider;
        this.keyVaultClientProvider = keyVaultClientProvider;
        this.keyVaultSecretProvider = keyVaultSecretProvider;
    }

    @Override
    public PropertySource<?> locate(Environment environment) {
        if (!(environment instanceof ConfigurableEnvironment)) {
            return null;
        }

        ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
        if (CONFIG_LOADED.get() && !env.getPropertySources().contains(REFRESH_ARGS_PROPERTY_SOURCE)) {
            return null;
        }

        List<String> profiles = Arrays.asList(env.getActiveProfiles());

        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        Collections.reverse(configStores); // Last store has the highest precedence

        StateHolder newState = new StateHolder();

        Iterator<ConfigStore> configStoreIterator = configStores.iterator();
        // Feature Management needs to be set in the last config store.
        while (configStoreIterator.hasNext()) {
            ConfigStore configStore = configStoreIterator.next();

            boolean loadNewPropertySources = STARTUP.get() || StateHolder.getLoadState(configStore.getEndpoint());

            if (configStore.isEnabled() && loadNewPropertySources) {
                // There is only one Feature Set for all AppConfigurationPropertySources
                FeatureSet featureSet = new FeatureSet();

                List<ConfigurationClientWrapper> clients = clientFactory.getAvailableClients(configStore.getEndpoint());

                String currentClientEndpoint = clientFactory.getCurrentConfigStoreClient(configStore.getEndpoint());

                List<ConfigurationClientWrapper> clientsToUse = new ArrayList<>();

                // Need to only use clients after the others, we need to assume the others failed during a refresh
                // check, or were in a failed state when the check started.
                for (int i = 0; i < clients.size(); i++) {
                    if (clientsToUse.size() > 0) {
                        clientsToUse.add(clients.get(i));
                    } else {
                        if (clients.get(i).getEndpoint().equals(currentClientEndpoint)) {
                            clientsToUse.add(clients.get(i));
                        }
                    }
                }

                boolean generatedPropertySources = true;

                List<AppConfigurationPropertySource> sourceList = new ArrayList<>();
                boolean reloadFailed = false;

                for (ConfigurationClientWrapper client : clientsToUse) {
                    sourceList = new ArrayList<>();

                    if (reloadFailed && !AppConfigurationRefreshUtil.checkStoreAfterRefreshFailed(configStore, client,
                        clientFactory)) {
                        // This store doesn't have any changes where to refresh store did. Skipping Checking next.
                        continue;
                    }

                    // Reverse in order to add Profile specific properties earlier, and last profile
                    // comes first
                    try {
                        sourceList
                            .addAll(create(client, configStore, profiles, !configStoreIterator.hasNext(), featureSet));

                        LOGGER.debug("PropertySource context.");

                        // Setting new ETag values for Watch
                        List<ConfigurationSetting> watchKeysSettings = new ArrayList<>();
                        List<ConfigurationSetting> watchKeysFeatures = new ArrayList<>();

                        for (AppConfigurationStoreTrigger trigger : configStore.getMonitoring().getTriggers()) {
                            ConfigurationSetting watchKey = client.getWatchKey(trigger.getKey(), trigger.getLabel());
                            if (watchKey != null) {
                                watchKeysSettings.add(watchKey);
                            } else {
                                watchKeysSettings
                                    .add(new ConfigurationSetting().setKey(trigger.getKey())
                                        .setLabel(trigger.getLabel()));
                            }
                        }

                        if (configStore.getFeatureFlags().getEnabled()) {
                            SettingSelector settingSelector = new SettingSelector()
                                .setKeyFilter(configStore.getFeatureFlags().getKeyFilter())
                                .setLabelFilter(configStore.getFeatureFlags().getLabelFilter());

                            PagedIterable<ConfigurationSetting> watchKeys = client.listSettings(settingSelector);

                            watchKeys
                                .forEach(watchKey -> watchKeysFeatures.add(NormalizeNull.normalizeNullLabel(watchKey)));

                            newState.setStateFeatureFlag(configStore.getEndpoint(), watchKeysFeatures,
                                configStore.getMonitoring().getFeatureFlagRefreshInterval());
                            newState.setLoadStateFeatureFlag(configStore.getEndpoint(), true);
                        }

                        newState.setState(configStore.getEndpoint(), watchKeysSettings,
                            configStore.getMonitoring().getRefreshInterval());
                        newState.setLoadState(configStore.getEndpoint(), true);
                    } catch (AppConfigurationStatusException e) {
                        reloadFailed = true;
                    } catch (Exception e) {
                        newState = failedToGeneratePropertySource(configStore, newState, e, false);

                        // If anything breaks we skip out on loading the rest of the store.
                        generatedPropertySources = false;
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
                    failedToGeneratePropertySource(configStore, newState, new RuntimeException(message), true);
                }

            } else if (!configStore.isEnabled() && loadNewPropertySources) {
                LOGGER.info("Not loading configurations from {} as it is not enabled.", configStore.getEndpoint());
            } else {
                LOGGER.warn("Not loading configurations from {} as it failed on startup.", configStore.getEndpoint());
            }
        }

        // If this configuration is set, a forced refresh will happen on the refresh interval.
        if (properties.getRefreshInterval() != null) {
            StateHolder.setNextForcedRefresh(properties.getRefreshInterval());
        }

        StateHolder.updateState(newState);
        CONFIG_LOADED.set(true);
        STARTUP.set(false);

        return composite;
    }

    private StateHolder failedToGeneratePropertySource(ConfigStore configStore, StateHolder newState, Exception e,
        Boolean allFailed) {
        if (!STARTUP.get()) {
            // Need to check for refresh first, or reset will never happen if fail fast is true.
            LOGGER.error(
                "Refreshing failed while reading configuration from Azure App Configuration store "
                    + configStore.getEndpoint() + ".");

            if (properties.getRefreshInterval() != null) {
                // The next refresh will happen sooner if refresh interval is expired.
                StateHolder.updateNextRefreshTime(properties.getRefreshInterval(), appProperties);
            }
            ReflectionUtils.rethrowRuntimeException(e);
        } else if (allFailed && configStore.isFailFast()) {
            LOGGER.error(
                "Fail fast is set and there was an error reading configuration from Azure App "
                    + "Configuration store " + configStore.getEndpoint() + ".");
            delayException();
            ReflectionUtils.rethrowRuntimeException(e);
        } else if (allFailed) {
            LOGGER.warn(
                "Unable to load configuration from Azure AppConfiguration store "
                    + configStore.getEndpoint() + ".",
                e);
            newState.setLoadState(configStore.getEndpoint(), false);
        }
        return newState;
    }

    /**
     * Creates a new set of AppConfigurationPropertySources, 1 per Label.
     *
     * @param store Config Store the PropertySource is being generated from
     * @param initFeatures determines if Feature Management is set in the PropertySource. When generating more than one
     * it needs to be in the last one.
     * @return a list of AppConfigurationPropertySources
     */
    private List<AppConfigurationPropertySource> create(ConfigurationClientWrapper client, ConfigStore store,
        List<String> profiles, boolean initFeatures, FeatureSet featureSet) throws Exception {
        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();

        List<AppConfigurationStoreSelects> selects = store.getSelects();

        for (AppConfigurationStoreSelects selectedKeys : selects) {
            AppConfigurationPropertySource propertySource = new AppConfigurationPropertySource(store, selectedKeys,
                profiles, properties, client, appProperties, keyVaultCredentialProvider,
                keyVaultClientProvider, keyVaultSecretProvider);

            propertySource.initProperties(featureSet);
            if (initFeatures) {
                propertySource.initFeatures(featureSet);
            }
            sourceList.add(propertySource);
        }

        return sourceList;
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
