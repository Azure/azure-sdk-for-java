// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

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
import com.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.stores.ClientStore;

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

    private final ClientStore clients;

    private final KeyVaultCredentialProvider keyVaultCredentialProvider;

    private final SecretClientBuilderSetup keyVaultClientProvider;

    private final KeyVaultSecretProvider keyVaultSecretProvider;

    private static final AtomicBoolean configloaded = new AtomicBoolean(false);

    private static final AtomicBoolean startup = new AtomicBoolean(true);

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
        AppConfigurationProviderProperties appProperties, ClientStore clients,
        KeyVaultCredentialProvider keyVaultCredentialProvider, SecretClientBuilderSetup keyVaultClientProvider,
        KeyVaultSecretProvider keyVaultSecretProvider) {
        this.properties = properties;
        this.appProperties = appProperties;
        this.configStores = properties.getStores();
        this.clients = clients;
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
        if (configloaded.get() && !env.getPropertySources().contains(REFRESH_ARGS_PROPERTY_SOURCE)) {
            return null;
        }

        List<String> profiles = Arrays.asList(env.getActiveProfiles());

        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        Collections.reverse(configStores); // Last store has highest precedence

        Iterator<ConfigStore> configStoreIterator = configStores.iterator();
        // Feature Management needs to be set in the last config store.
        while (configStoreIterator.hasNext()) {
            ConfigStore configStore = configStoreIterator.next();

            boolean loadNewPropertySources = startup.get() || StateHolder.getLoadState(configStore.getEndpoint());

            if (configStore.isEnabled() && loadNewPropertySources) {
                addPropertySource(composite, configStore, profiles, !configStoreIterator.hasNext());
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

        configloaded.set(true);
        startup.set(false);

        // Loading configurations worked. Setting attempts to zero.
        StateHolder.clearAttempts();
        return composite;
    }

    /**
     * Adds a new Property Source
     *
     * @param composite PropertySource being added
     * @param store Config Store the PropertySource is being generated from
     * @param applicationName Name of the application
     * @param profiles Active profiles in the Store
     * @param storeContextsMap the Map storing the storeName -> List of contexts map
     * @param initFeatures determines if Feature Management is set in the PropertySource. When generating more than one
     * it needs to be in the last one.
     */
    private void addPropertySource(CompositePropertySource composite, ConfigStore store, List<String> profiles,
        boolean initFeatures) {

        // There is only one Feature Set for all AppConfigurationPropertySources
        FeatureSet featureSet = new FeatureSet();

        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();

        // Reverse in order to add Profile specific properties earlier, and last profile
        // comes first

        try {
            sourceList.addAll(create(store, profiles, initFeatures, featureSet));

            LOGGER.debug("PropertySource context.");
        } catch (Exception e) {
            if (!startup.get()) {
                // Need to check for refresh first, or reset will never happen if fail fast is true.
                LOGGER.error(
                    "Refreshing failed while reading configuration from Azure App Configuration store "
                        + store.getEndpoint() + ".");

                if (properties.getRefreshInterval() != null) {
                 // The next refresh will happen sooner if refresh interval is expired.
                    StateHolder.updateNextRefreshTime(properties.getRefreshInterval(), appProperties);
                }
                ReflectionUtils.rethrowRuntimeException(e);
            } else if (store.isFailFast()) {
                LOGGER.error(
                    "Fail fast is set and there was an error reading configuration from Azure App "
                        + "Configuration store " + store.getEndpoint() + ".");
                ReflectionUtils.rethrowRuntimeException(e);
            } else {
                LOGGER.warn(
                    "Unable to load configuration from Azure AppConfiguration store " + store.getEndpoint() + ".",
                    e);
                StateHolder.setLoadState(store.getEndpoint(), false);
            }
            // If anything breaks we skip out on loading the rest of the store.
            return;
        }
        sourceList.forEach(composite::addPropertySource);
    }

    /**
     * Creates a new set of AppConfigurationProertySources, 1 per Label.
     *
     * @param context Context of the application, part of uniquely define a PropertySource
     * @param store Config Store the PropertySource is being generated from
     * @param storeContextsMap the Map storing the storeName -> List of contexts map
     * @param initFeatures determines if Feature Management is set in the PropertySource. When generating more than one
     * it needs to be in the last one.
     * @return a list of AppConfigurationPropertySources
     */
    private List<AppConfigurationPropertySource> create(ConfigStore store, List<String> profiles, boolean initFeatures,
        FeatureSet featureSet)
        throws Exception {
        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();

        try {
            List<AppConfigurationStoreSelects> selects = store.getSelects();

            for (AppConfigurationStoreSelects selectedKeys : selects) {
                AppConfigurationPropertySource propertySource = new AppConfigurationPropertySource(store, selectedKeys,
                    profiles, properties, clients, appProperties, keyVaultCredentialProvider,
                    keyVaultClientProvider, keyVaultSecretProvider);

                propertySource.initProperties(featureSet);
                if (initFeatures) {
                    propertySource.initFeatures(featureSet);
                }
                sourceList.add(propertySource);
            }

            // Setting new ETag values for Watch
            List<ConfigurationSetting> watchKeysSettings = new ArrayList<>();
            List<ConfigurationSetting> watchKeysFeatures = new ArrayList<>();

            for (AppConfigurationStoreTrigger trigger : store.getMonitoring().getTriggers()) {
                ConfigurationSetting watchKey = clients.getWatchKey(trigger.getKey(), trigger.getLabel(),
                    store.getEndpoint());
                if (watchKey != null) {
                    watchKeysSettings.add(watchKey);
                } else {
                    watchKeysSettings
                        .add(new ConfigurationSetting().setKey(trigger.getKey()).setLabel(trigger.getLabel()));
                }
            }
            if (store.getFeatureFlags().getEnabled()) {
                SettingSelector settingSelector = new SettingSelector()
                    .setKeyFilter(store.getFeatureFlags().getKeyFilter())
                    .setLabelFilter(store.getFeatureFlags().getLabelFilter());

                PagedIterable<ConfigurationSetting> watchKeys = clients.getFeatureFlagWatchKey(settingSelector,
                    store.getEndpoint());

                watchKeys.forEach(watchKey -> watchKeysFeatures.add(NormalizeNull.normalizeNullLabel(watchKey)));

                StateHolder.setStateFeatureFlag(store.getEndpoint(), watchKeysFeatures,
                    store.getMonitoring().getFeatureFlagRefreshInterval());
                StateHolder.setLoadStateFeatureFlag(store.getEndpoint(), true);
            }

            StateHolder.setState(store.getEndpoint(), watchKeysSettings, store.getMonitoring().getRefreshInterval());
            StateHolder.setLoadState(store.getEndpoint(), true);
        } catch (Exception e) {
            delayException();
            throw e;
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
