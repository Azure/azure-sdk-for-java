// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.PUSH_REFRESH;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
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

import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
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
    private StateHolder storeState = new StateHolder();

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
        if (context.getBootstrapContext().isRegistered(FeatureFlagClient.class)) {
            featureFlagClient = context.getBootstrapContext().get(FeatureFlagClient.class);
        } else {
            featureFlagClient = new FeatureFlagClient();
            context.getBootstrapContext().registerIfAbsent(FeatureFlagClient.class,
                InstanceSupplier.from(() -> featureFlagClient));
        }
        // Reset telemetry usage for refresh
        featureFlagClient.resetTelemetry();
        List<EnumerablePropertySource<?>> sourceList = new ArrayList<>();
        if (resource.isConfigStoreEnabled()) {
            replicaClientFactory = context.getBootstrapContext()
                .get(AppConfigurationReplicaClientFactory.class);
            keyVaultClientFactory = context.getBootstrapContext()
                .get(AppConfigurationKeyVaultClientFactory.class);

            List<AppConfigurationReplicaClient> clients = replicaClientFactory
                .getAvailableClients(resource.getEndpoint(), true);

            boolean reloadFailed = false;
            boolean pushRefresh = false;
            Exception lastException = null;
            PushNotification notification = resource.getMonitoring().getPushNotification();
            if ((notification.getPrimaryToken() != null
                && StringUtils.hasText(notification.getPrimaryToken().getName()))
                || (notification.getSecondaryToken() != null
                    && StringUtils.hasText(notification.getPrimaryToken().getName()))) {
                pushRefresh = true;
            }
            // Feature Management needs to be set in the last config store.
            requestContext = new Context("refresh", resource.isRefresh()).addData(PUSH_REFRESH, pushRefresh);

            Iterator<AppConfigurationReplicaClient> clientIterator = clients.iterator();

            while (clientIterator.hasNext()) {
                AppConfigurationReplicaClient client = clientIterator.next();

                if (reloadFailed
                    && !AppConfigurationRefreshUtil.refreshStoreCheck(client,
                        replicaClientFactory.findOriginForEndpoint(client.getEndpoint()), requestContext)) {
                    // This store doesn't have any changes where to refresh store did. Skipping Checking next.
                    continue;
                }

                // Reverse in order to add Profile specific properties earlier, and last profile comes first
                try {
                    sourceList.addAll(createSettings(client));
                    List<FeatureFlags> featureFlags = createFeatureFlags(client);

                    logger.debug("PropertySource context.");
                    AppConfigurationStoreMonitoring monitoring = resource.getMonitoring();

                    storeState.setStateFeatureFlag(resource.getEndpoint(), featureFlags,
                        monitoring.getFeatureFlagRefreshInterval());

                    if (monitoring.isEnabled()) {
                        // Setting new ETag values for Watch
                        List<ConfigurationSetting> watchKeysSettings = monitoring.getTriggers().stream()
                            .map(trigger -> client.getWatchKey(trigger.getKey(), trigger.getLabel(),
                                requestContext))
                            .toList();

                        storeState.setState(resource.getEndpoint(), watchKeysSettings, monitoring.getRefreshInterval());
                    }
                    storeState.setLoadState(resource.getEndpoint(), true); // Success - configuration loaded, exit loop
                    lastException = null;
                    // Break out of the loop since we have successfully loaded configuration
                    break;
                } catch (AppConfigurationStatusException e) {
                    reloadFailed = true;
                    replicaClientFactory.backoffClient(resource.getEndpoint(), client.getEndpoint());
                    lastException = e;
                    // Log the specific replica failure with context
                    logReplicaFailure(client, "status exception", clientIterator.hasNext(), e);
                } catch (Exception e) {
                    // Store the exception to potentially use if all replicas fail
                    lastException = e; // Log the specific replica failure with context
                    logReplicaFailure(client, "exception", clientIterator.hasNext(), e);
                }
            } // Check if all replicas failed
            if (lastException != null && !resource.isRefresh()) {
                // During startup, if all replicas failed, fail the application
                logger.error("Azure App Configuration failed to load configuration during startup for store: "
                    + resource.getEndpoint() + ". Application cannot start without required configuration.");
                failedToGeneratePropertySource(lastException);
            } else if (lastException != null && resource.isRefresh()) {
                // During refresh, log warning but don't fail the application
                logger.warn("Azure App Configuration failed during refresh for store: "
                    + resource.getEndpoint() + ". Continuing with existing configuration.");
            }
        }

        StateHolder.updateState(storeState);
        if (featureFlagClient.getFeatureFlags().size() > 0) {
            // Don't add feature flags if there are none, otherwise the local file can't load them.
            sourceList.add(new AppConfigurationFeatureManagementPropertySource(featureFlagClient));
        }
        return new ConfigData(sourceList);
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
            propertySource.initProperties(resource.getTrimKeyPrefix(), requestContext);
            sourceList.add(propertySource);
        }
        return sourceList;
    }

    /**
     * Creates a list of feature flags from Azure App Configuration.
     *
     * @param client client for connecting to App Configuration
     * @return a list of FeatureFlags
     * @throws Exception creating feature flags failed
     */
    private List<FeatureFlags> createFeatureFlags(AppConfigurationReplicaClient client)
        throws Exception {
        List<FeatureFlags> featureFlagWatchKeys = new ArrayList<>();
        List<String> profiles = resource.getProfiles().getActive();

        for (FeatureFlagKeyValueSelector selectedKeys : resource.getFeatureFlagSelects()) {
            List<FeatureFlags> storesFeatureFlags = featureFlagClient.loadFeatureFlags(client,
                selectedKeys.getKeyFilter(), selectedKeys.getLabelFilter(profiles), requestContext);
            featureFlagWatchKeys.addAll(storesFeatureFlags);
        }

        return featureFlagWatchKeys;
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
