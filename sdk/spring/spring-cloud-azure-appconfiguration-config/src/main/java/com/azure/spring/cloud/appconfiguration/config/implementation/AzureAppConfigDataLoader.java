// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.PUSH_REFRESH;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.springframework.boot.bootstrap.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.StringUtils;

import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SettingSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.configuration.WatchedConfigurationSettings;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationKeyValueSelector;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationStoreMonitoring.PushNotification;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagKeyValueSelector;

/**
 * Azure App Configuration data loader implementation for Spring Boot's ConfigDataLoader.
 *
 * @since 6.0.0
 */

public class AzureAppConfigDataLoader implements ConfigDataLoader<AzureAppConfigDataResource> {

    /**
     * Logger instance for this class.
     */
    private static Log logger = new DeferredLog();

    /**
     * The Azure App Configuration data resource being processed.
     */
    private AzureAppConfigDataResource resource;

    /**
     * Factory for creating replica clients to connect to Azure App Configuration.
     */
    private AppConfigurationReplicaClientFactory replicaClientFactory;

    /**
     * Factory for creating Key Vault clients for secret resolution.
     */
    private AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

    /**
     * State holder for managing configuration and feature flag states.
     */
    private final StateHolder storeState = new StateHolder();

    /**
     * Client for handling feature flag operations.
     */
    private FeatureFlagClient featureFlagClient;

    /**
     * Request context for tracking operations and telemetry.
     */
    private Context requestContext;

    /**
     * Application start time for calculating delays.
     */
    private static final Instant START_DATE = Instant.now();

    /**
     * Pre-kill time in seconds for delaying exceptions during startup.
     */
    private static final Integer PREKILL_TIME = 5;

    /**
     * Constructs a new AzureAppConfigDataLoader with the specified logger factory.
     *
     * @param logFactory the deferred log factory for creating loggers
     */
    public AzureAppConfigDataLoader(DeferredLogFactory logFactory) {
        logger = logFactory.getLog(getClass());
    }

    /**
     * Loads configuration data from Azure App Configuration service.
     *
     * @param context the config data loader context
     * @param resource the Azure App Configuration data resource
     * @return ConfigData containing loaded property sources
     * @throws IOException if an I/O error occurs during loading
     * @throws ConfigDataResourceNotFoundException if the configuration resource is not found
     */
    @Override
    public ConfigData load(ConfigDataLoaderContext context, AzureAppConfigDataResource resource)
        throws IOException, ConfigDataResourceNotFoundException {
        this.resource = resource;
        storeState.setNextForcedRefresh(resource.getRefreshInterval());

        initializeFeatureFlagClient(context);

        List<EnumerablePropertySource<?>> sourceList = new ArrayList<>();
        if (resource.isConfigStoreEnabled()) {
            replicaClientFactory = context.getBootstrapContext().get(AppConfigurationReplicaClientFactory.class);
            keyVaultClientFactory = context.getBootstrapContext().get(AppConfigurationKeyVaultClientFactory.class);

            Exception loadException = loadConfiguration(sourceList);
            if (loadException != null) {
                if (resource.isRefresh()) {
                    logger.warn("Azure App Configuration failed during refresh for store: "
                        + resource.getEndpoint() + ". Continuing with existing configuration.");
                } else {
                    logger.error("Azure App Configuration failed to load configuration during startup for store: "
                        + resource.getEndpoint() + ". Application cannot start without required configuration.");
                    failedToGeneratePropertySource(loadException);
                }
            }
        }

        StateHolder.updateState(storeState);
        if (!featureFlagClient.getFeatureFlags().isEmpty()) {
            sourceList.add(new AppConfigurationFeatureManagementPropertySource(featureFlagClient));
        }
        return new ConfigData(sourceList);
    }

    /**
     * Initializes or retrieves the feature flag client from the bootstrap context.
     *
     * @param context the config data loader context
     */
    private void initializeFeatureFlagClient(ConfigDataLoaderContext context) {
        if (context.getBootstrapContext().isRegistered(FeatureFlagClient.class)) {
            featureFlagClient = context.getBootstrapContext().get(FeatureFlagClient.class);
        } else {
            featureFlagClient = new FeatureFlagClient();
            context.getBootstrapContext().registerIfAbsent(FeatureFlagClient.class,
                InstanceSupplier.from(() -> featureFlagClient));
        }
        featureFlagClient.resetTelemetry();
    }

    /**
     * Loads configuration from Azure App Configuration with replica failover support.
     *
     * @param sourceList the list to populate with property sources
     * @return the exception if loading failed, null on success
     */
    private Exception loadConfiguration(List<EnumerablePropertySource<?>> sourceList) {
        PushNotification notification = resource.getMonitoring().getPushNotification();
        boolean pushRefresh = (notification.getPrimaryToken() != null
            && StringUtils.hasText(notification.getPrimaryToken().getName()))
            || (notification.getSecondaryToken() != null
                && StringUtils.hasText(notification.getSecondaryToken().getName()));

        requestContext = new Context("refresh", resource.isRefresh()).addData(PUSH_REFRESH, pushRefresh);

        Instant deadline = Instant.now().plusSeconds(resource.getStartupTimeout().getSeconds());
        Exception lastException = null;

        while (Instant.now().isBefore(deadline)) {
            replicaClientFactory.findActiveClients(resource.getEndpoint());
            lastException = attemptLoadFromClients(sourceList);

            if (lastException == null) {
                return null; // Success
            }

            // All clients failed, wait until next client is available or minimum delay
            if (Instant.now().isBefore(deadline)) {
                long waitTime = replicaClientFactory.getMillisUntilNextClientAvailable(resource.getEndpoint());
                
                // Don't wait longer than remaining time until deadline
                long remainingTime = deadline.toEpochMilli() - Instant.now().toEpochMilli();
                waitTime = Math.min(waitTime, remainingTime);

                if (waitTime > 0) {
                    logger.debug("All replicas in backoff for store: " + resource.getEndpoint() 
                        + ". Waiting " + waitTime + "ms before retry.");
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return lastException;
                    }
                }
            }
        }

        return lastException;
    }

    /**
     * Attempts to load configuration from available clients.
     *
     * @param sourceList the list to populate with property sources
     * @return the exception if all clients failed, null on success
     */
    private Exception attemptLoadFromClients(List<EnumerablePropertySource<?>> sourceList) {
        boolean reloadFailed = false;
        Exception lastException = null;
        AppConfigurationReplicaClient client = replicaClientFactory.getNextActiveClient(resource.getEndpoint(), true);

        while (client != null) {
            final AppConfigurationReplicaClient currentClient = client;

            if (reloadFailed && !AppConfigurationRefreshUtil.refreshStoreCheck(currentClient,
                replicaClientFactory.findOriginForEndpoint(currentClient.getEndpoint()), requestContext)) {
                client = replicaClientFactory.getNextActiveClient(resource.getEndpoint(), false);
                continue;
            }

            try {
                loadFromClient(currentClient, sourceList);
                return null; // Success
            } catch (AppConfigurationStatusException e) {
                reloadFailed = true;
                lastException = e;
                client = handleReplicaFailure(currentClient, "status exception", e);
            } catch (Exception e) {
                lastException = e;
                client = handleReplicaFailure(currentClient, "exception", e);
            }
        }

        return lastException;
    }

    /**
     * Loads configuration and feature flags from a specific replica client.
     *
     * @param client the replica client to load from
     * @param sourceList the list to populate with property sources
     * @throws Exception if loading fails
     */
    private void loadFromClient(AppConfigurationReplicaClient client, List<EnumerablePropertySource<?>> sourceList)
        throws Exception {
        sourceList.addAll(createSettings(client));
        List<WatchedConfigurationSettings> featureFlags = createFeatureFlags(client);

        AppConfigurationStoreMonitoring monitoring = resource.getMonitoring();

        storeState.setStateFeatureFlag(resource.getEndpoint(), featureFlags,
            monitoring.getFeatureFlagRefreshInterval());

        if (monitoring.isEnabled()) {
            setupMonitoringState(client, monitoring);
        }

        storeState.setLoadState(resource.getEndpoint(), true);
    }

    /**
     * Sets up the monitoring state based on the configuration.
     *
     * @param client the replica client
     * @param monitoring the monitoring configuration
     * @throws Exception if setting up monitoring fails
     */
    private void setupMonitoringState(AppConfigurationReplicaClient client, AppConfigurationStoreMonitoring monitoring)
        throws Exception {
        if (monitoring.getTriggers().isEmpty()) {
            // Use watched configuration settings for refresh
            List<WatchedConfigurationSettings> watchedConfigurationSettingsList = getWatchedConfigurationSettings(
                client);
            storeState.setState(resource.getEndpoint(), Collections.emptyList(),
                watchedConfigurationSettingsList, monitoring.getRefreshInterval());
        } else {
            // Use traditional watch key monitoring
            List<ConfigurationSetting> watchKeysSettings = monitoring.getTriggers().stream()
                .map(trigger -> client.getWatchKey(trigger.getKey(), trigger.getLabel(), requestContext))
                .toList();

            storeState.setState(resource.getEndpoint(), watchKeysSettings, monitoring.getRefreshInterval());
        }
    }

    /**
     * Handles a replica failure by backing off the client and getting the next available replica.
     *
     * @param client the failed client
     * @param exceptionType a description of the exception type
     * @param exception the exception that occurred
     * @return the next available client, or null if none available
     */
    private AppConfigurationReplicaClient handleReplicaFailure(AppConfigurationReplicaClient client,
        String exceptionType, Exception exception) {
        replicaClientFactory.backoffClient(resource.getEndpoint(), client.getEndpoint());
        AppConfigurationReplicaClient nextClient = replicaClientFactory.getNextActiveClient(resource.getEndpoint(),
            false);
        logReplicaFailure(client, exceptionType, nextClient != null, exception);
        return nextClient;
    }

    /**
     * Handles failed property source generation when all replicas fail during application startup.
     *
     * @param e the exception that caused the failure
     * @throws RuntimeException always thrown to indicate the startup failure
     */
    private void failedToGeneratePropertySource(Exception e) {
        logger.error("Configuration loading failed during application startup from Azure App Configuration store "
            + resource.getEndpoint() + ". Application cannot start without required configuration.");
        delayException();
        throw new RuntimeException("Failed to generate property sources for " + resource.getEndpoint(), e);
    }

    /**
     * Creates a new set of AppConfigurationPropertySources, 1 per Label.
     *
     * @param client client for connecting to App Configuration
     * @return a list of AppConfigurationPropertySources
     * @throws Exception creating a property source failed
     */
    private List<AppConfigurationPropertySource> createSettings(AppConfigurationReplicaClient client)
        throws Exception {
        List<AppConfigurationPropertySource> sourceList = new ArrayList<>();
        List<AppConfigurationKeyValueSelector> selects = resource.getSelects();
        List<String> profiles = resource.getProfiles().getActive();

        for (AppConfigurationKeyValueSelector selectedKeys : selects) {
            AppConfigurationPropertySource propertySource;

            if (StringUtils.hasText(selectedKeys.getSnapshotName())) {
                propertySource = new AppConfigurationSnapshotPropertySource(
                    selectedKeys.getSnapshotName() + "/" + resource.getEndpoint(), client, keyVaultClientFactory,
                    selectedKeys.getSnapshotName(), featureFlagClient);
            } else {
                propertySource = new AppConfigurationApplicationSettingPropertySource(
                    selectedKeys.getKeyFilter() + resource.getEndpoint() + "/", client, keyVaultClientFactory,
                    selectedKeys.getKeyFilter(), selectedKeys.getLabelFilter(profiles));
            }
            propertySource.initProperties(resource.getTrimKeyPrefix(), requestContext);
            sourceList.add(propertySource);
        }
        return sourceList;
    }

    /**
     * Creates a list of feature flags from Azure App Configuration.
     *
     * @param client client for connecting to App Configuration
     * @return a list of WatchedConfigurationSettings
     * @throws Exception creating feature flags failed
     */
    private List<WatchedConfigurationSettings> createFeatureFlags(AppConfigurationReplicaClient client)
        throws Exception {
        List<WatchedConfigurationSettings> featureFlagWatchKeys = new ArrayList<>();
        List<String> profiles = resource.getProfiles().getActive();

        for (FeatureFlagKeyValueSelector selectedKeys : resource.getFeatureFlagSelects()) {
            List<WatchedConfigurationSettings> storesFeatureFlags = featureFlagClient.loadFeatureFlags(client,
                selectedKeys.getKeyFilter(), selectedKeys.getLabelFilter(profiles), requestContext);
            featureFlagWatchKeys.addAll(storesFeatureFlags);
        }

        return featureFlagWatchKeys;
    }

    /**
     * Creates a list of watched configuration settings for configuration settings from Azure App Configuration. This is
     * used for collection-based refresh monitoring as an alternative to individual watch keys.
     *
     * @param client client for connecting to App Configuration
     * @return a list of WatchedConfigurationSettings for configuration settings
     * @throws Exception creating watched configuration settings failed
     */
    private List<WatchedConfigurationSettings> getWatchedConfigurationSettings(AppConfigurationReplicaClient client)
        throws Exception {
        List<WatchedConfigurationSettings> watchedConfigurationSettingsList = new ArrayList<>();
        List<AppConfigurationKeyValueSelector> selects = resource.getSelects();
        List<String> profiles = resource.getProfiles().getActive();

        for (AppConfigurationKeyValueSelector selectedKeys : selects) {
            // Skip snapshots - they don't support watched configuration settings
            if (StringUtils.hasText(selectedKeys.getSnapshotName())) {
                continue;
            }

            // Create watched configuration settings for each label
            for (String label : selectedKeys.getLabelFilter(profiles)) {
                SettingSelector settingSelector = new SettingSelector()
                    .setKeyFilter(selectedKeys.getKeyFilter() + "*")
                    .setLabelFilter(label);

                WatchedConfigurationSettings watchedConfigurationSettings = client.loadWatchedSettings(settingSelector,
                    requestContext);
                watchedConfigurationSettingsList.add(watchedConfigurationSettings);
            }
        }

        return watchedConfigurationSettingsList;
    }

    /**
     * Logs a replica failure with contextual information about the failure scenario and available replicas.
     *
     * @param client the replica client that failed
     * @param exceptionType a brief description of the exception type (e.g., "status exception", "exception")
     * @param hasMoreReplicas whether there are additional replicas available to try
     * @param exception the exception that caused the failure
     */
    private void logReplicaFailure(AppConfigurationReplicaClient client, String exceptionType,
        boolean hasMoreReplicas, Exception exception) {
        String scenario = resource.isRefresh() ? "refresh" : "startup";
        String nextAction = hasMoreReplicas ? "Trying next replica." : "No more replicas available.";

        logger.warn("Azure App Configuration replica " + client.getEndpoint()
            + " failed during " + scenario + " with " + exceptionType + ". "
            + nextAction + " Store: " + resource.getEndpoint(), exception);
    }

    /**
     * Introduces a delay before throwing exceptions during startup to prevent fast crash loops.
     */
    private void delayException() {
        Instant currentDate = Instant.now();
        Instant preKillTime = START_DATE.plusSeconds(PREKILL_TIME);
        if (currentDate.isBefore(preKillTime)) {
            long diffInMillies = Math.abs(preKillTime.toEpochMilli() - currentDate.toEpochMilli());
            try {
                Thread.sleep(diffInMillies);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }
    }
}
