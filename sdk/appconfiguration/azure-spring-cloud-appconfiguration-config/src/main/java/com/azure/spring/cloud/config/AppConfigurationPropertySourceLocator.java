// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreTrigger;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.stores.ClientStore;

/**
 * Locates Azure App Configuration Property Sources.
 */
public class AppConfigurationPropertySourceLocator implements PropertySourceLocator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigurationPropertySourceLocator.class);

    private static final String SPRING_APP_NAME_PROP = "spring.application.name";

    private static final String PROPERTY_SOURCE_NAME = "azure-config-store";

    private static final String REFRESH_ARGS_PROPERTY_SOURCE = "refreshArgs";

    private static final String PATH_SPLITTER = "/";

    private final AppConfigurationProperties properties;

    private final List<ConfigStore> configStores;

    private final Map<String, List<String>> storeContextsMap = new ConcurrentHashMap<>();

    private final AppConfigurationProviderProperties appProperties;

    private final ClientStore clients;

    private final KeyVaultCredentialProvider keyVaultCredentialProvider;

    private final SecretClientBuilderSetup keyVaultClientProvider;

    private static AtomicBoolean configloaded = new AtomicBoolean(false);

    private static AtomicBoolean startup = new AtomicBoolean(true);

    public AppConfigurationPropertySourceLocator(AppConfigurationProperties properties,
        AppConfigurationProviderProperties appProperties, ClientStore clients,
        KeyVaultCredentialProvider keyVaultCredentialProvider, SecretClientBuilderSetup keyVaultClientProvider) {
        this.properties = properties;
        this.appProperties = appProperties;
        this.configStores = properties.getStores();
        this.clients = clients;
        this.keyVaultCredentialProvider = keyVaultCredentialProvider;
        this.keyVaultClientProvider = keyVaultClientProvider;
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

        String applicationName = this.properties.getName();
        if (!StringUtils.hasText(applicationName)) {
            applicationName = env.getProperty(SPRING_APP_NAME_PROP);
        }

        List<String> profiles = Arrays.asList(env.getActiveProfiles());

        CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);
        Collections.reverse(configStores); // Last store has highest precedence

        Iterator<ConfigStore> configStoreIterator = configStores.iterator();
        // Feature Management needs to be set in the last config store.
        while (configStoreIterator.hasNext()) {
            ConfigStore configStore = configStoreIterator.next();

            Boolean loadNewPropertySources = startup.get() || StateHolder.getLoadState(configStore.getEndpoint());

            if (configStore.isEnabled() && loadNewPropertySources) {
                addPropertySource(composite, configStore, applicationName, profiles, storeContextsMap,
                    !configStoreIterator.hasNext());
            } else if (!configStore.isEnabled() && loadNewPropertySources) {
                LOGGER.info("Not loading configurations from {} as it is not enabled.", configStore.getEndpoint());
            } else {
                LOGGER.warn("Not loading configurations from {} as it failed on startup.", configStore.getEndpoint());
            }
        }
        configloaded.set(true);
        startup.set(false);
        return composite;
    }

    public Map<String, List<String>> getStoreContextsMap() {
        return this.storeContextsMap;
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
    private void addPropertySource(CompositePropertySource composite, ConfigStore store, String applicationName,
        List<String> profiles, Map<String, List<String>> storeContextsMap, boolean initFeatures) {

        List<String> contexts = new ArrayList<>();
        contexts.addAll(generateContexts(this.properties.getDefaultContext(), store));
        contexts.addAll(generateContexts(applicationName, store));

        // There is only one Feature Set for all AppConfigurationPropertySources
        FeatureSet featureSet = new FeatureSet();

        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();

        // Reverse in order to add Profile specific properties earlier, and last profile
        // comes first
        Collections.reverse(contexts);
        for (String sourceContext : contexts) {
            try {
                sourceList.addAll(create(sourceContext, store, storeContextsMap, profiles, initFeatures, featureSet));

                LOGGER.debug("PropertySource context [{}] is added.", sourceContext);
            } catch (Exception e) {
                if (store.isFailFast() || !startup.get()) {
                    LOGGER.error(
                        "Fail fast is set and there was an error reading configuration from Azure App "
                            + "Configuration store " + store.getEndpoint()
                            + ". The configuration starting with " + sourceContext + " failed to load.");
                    ReflectionUtils.rethrowRuntimeException(e);
                } else {
                    LOGGER.warn(
                        "Unable to load configuration from Azure AppConfiguration store " + store.getEndpoint()
                            + ". The configurations starting with " + sourceContext + "failed to load.",
                        e);
                    StateHolder.setLoadState(store.getEndpoint(), false);
                }
                // If anything breaks we skip out on loading the rest of the store.
                return;
            }
        }
        sourceList.forEach(composite::addPropertySource);
    }

    private List<String> generateContexts(String applicationName, ConfigStore configStore) {
        List<String> result = new ArrayList<>();
        if (!StringUtils.hasText(applicationName)) {
            return result; // Ignore null or empty application name
        }

        result.add(PATH_SPLITTER + applicationName + PATH_SPLITTER);

        return result;
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
    private List<AppConfigurationPropertySource> create(String context, ConfigStore store,
        Map<String, List<String>> storeContextsMap, List<String> profiles, boolean initFeatures, FeatureSet featureSet)
        throws Exception {
        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();

        try {
            String[] labels = store.getLabels(profiles);

            for (String label : labels) {
                putStoreContext(store.getEndpoint(), context, storeContextsMap);
                AppConfigurationPropertySource propertySource = new AppConfigurationPropertySource(context, store,
                    label, properties, clients, appProperties, keyVaultCredentialProvider, keyVaultClientProvider);

                propertySource.initProperties(featureSet);
                if (initFeatures) {
                    propertySource.initFeatures(featureSet);
                }
                sourceList.add(propertySource);
            }

            // Setting new ETag values for Watch
            List<ConfigurationSetting> watchKeysSettings = new ArrayList<ConfigurationSetting>();
            List<ConfigurationSetting> watchKeysFeatures = new ArrayList<ConfigurationSetting>();

            for (AppConfigurationStoreTrigger trigger : store.getMonitoring().getTriggers()) {
                SettingSelector settingSelector = new SettingSelector().setKeyFilter(trigger.getKey())
                    .setLabelFilter(trigger.getLabel());

                ConfigurationSetting watchKey = clients.getWatchKey(settingSelector,
                    store.getEndpoint());
                watchKeysSettings.add(watchKey);
            }
            if (store.getFeatureFlags().getEnabled()) {
                SettingSelector settingSelector = new SettingSelector()
                    .setKeyFilter(store.getFeatureFlags().getKeyFilter())
                    .setLabelFilter(store.getFeatureFlags().getLabelFilter());

                ConfigurationSetting watchKey = clients.getWatchKey(settingSelector,
                    store.getEndpoint());
                watchKey.setKey(store.getFeatureFlags().getKeyFilter());
                watchKeysFeatures.add(watchKey);
                StateHolder.setStateFeatureFlag(store.getEndpoint(), watchKeysFeatures,
                    store.getMonitoring().getFeatureFlagRefreshInterval());
                StateHolder.setLoadStateFeatureFlag(store.getEndpoint(), true);
            }

            StateHolder.setState(store.getEndpoint(), watchKeysSettings, store.getMonitoring().getRefreshInterval());
            StateHolder.setLoadState(store.getEndpoint(), true);
        } catch (RuntimeException e) {
            delayException();
            throw e;
        } catch (Exception e) {
            delayException();
            throw e;
        }

        return sourceList;
    }

    /**
     * Put certain context to the store contexts map
     *
     * @param storeName the name of the configuration store
     * @param context the context text for the PropertySource, e.g., "/application"
     * @param storeContextsMap the Map storing the storeName -> List of contexts map
     */
    private void putStoreContext(String storeName, String context,
        @NonNull Map<String, List<String>> storeContextsMap) {
        if (!StringUtils.hasText(context) || !StringUtils.hasText(storeName)) {
            return;
        }

        List<String> contexts = storeContextsMap.get(storeName);
        if (contexts == null) {
            contexts = new ArrayList<String>();
        }
        contexts.add(context);

        storeContextsMap.put(storeName, contexts);
    }

    private void delayException() {
        Date currentDate = new Date();
        Date maxRetryDate = DateUtils.addSeconds(appProperties.getStartDate(),
            appProperties.getPrekillTime());
        if (currentDate.before(maxRetryDate)) {
            long diffInMillies = Math.abs(maxRetryDate.getTime() - currentDate.getTime());
            try {
                Thread.sleep(diffInMillies);
            } catch (InterruptedException e) {
                LOGGER.error("Failed to wait before fast fail.");
            }
        }
    }
}
