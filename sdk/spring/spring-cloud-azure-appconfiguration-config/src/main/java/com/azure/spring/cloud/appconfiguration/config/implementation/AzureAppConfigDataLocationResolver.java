// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

/**
 * Resolves Azure App Configuration data locations for Spring Boot's ConfigData API.
 * 
 * @since 6.0.0
 */

public class AzureAppConfigDataLocationResolver
    implements ConfigDataLocationResolver<AzureAppConfigDataResource> {

    private static final Log LOGGER = new DeferredLog();

    /** Prefix used to identify Azure App Configuration locations */
    public static final String PREFIX = "azureAppConfiguration";

    /** Flag to track startup phase for proper resource initialization */
    private static final AtomicBoolean START_UP = new AtomicBoolean(true);

    /**
     * Determines if the given location can be resolved by this resolver.
     * 
     * @param context the resolver context containing binder and bootstrap information
     * @param location the configuration data location to check
     * @return true if this resolver can handle the location, false otherwise
     */
    @Override
    public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
        if (!location.hasPrefix(PREFIX)) {
            return false;
        }

        // Check if the configuration properties for Azure App Configuration are present
        return hasValidStoreConfiguration(context.getBinder());
    }

    /**
     * Checks if the required configuration properties for Azure App Configuration are present.
     * 
     * @param binder the binder to check for properties
     * @return true if at least one of the required properties is present, false otherwise
     */
    private boolean hasValidStoreConfiguration(Binder binder) {
        // Check if any of the required properties for Azure App Configuration stores are present
        String configPrefix = AppConfigurationProperties.CONFIG_PREFIX + ".stores[0].";

        return hasNonEmptyProperty(binder, configPrefix + "endpoint")
            || hasNonEmptyProperty(binder, configPrefix + "connection-string")
            || hasNonEmptyProperty(binder, configPrefix + "endpoints")
            || hasNonEmptyProperty(binder, configPrefix + "connection-strings");
    }

    private boolean hasNonEmptyProperty(Binder binder, String propertyPath) {
        return StringUtils.hasText(binder.bind(propertyPath, String.class).orElse(""));
    }

    /**
     * Resolves configuration data resources for the given location.
     * 
     * @param context the resolver context
     * @param location the configuration data location
     * @return empty list of resources
     */
    @Override
    public List<AzureAppConfigDataResource> resolve(ConfigDataLocationResolverContext context,
        ConfigDataLocation location)
        throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {
        return Collections.emptyList();
    }

    /**
     * Resolves profile-specific configuration data resources.
     * 
     * @param resolverContext the resolver context
     * @param location the configuration data location
     * @param profiles the active Spring profiles
     * @return list of Azure App Configuration data resources
     * @throws ConfigDataLocationNotFoundException if location cannot be found
     */
    @Override
    public List<AzureAppConfigDataResource> resolveProfileSpecific(
        ConfigDataLocationResolverContext resolverContext, ConfigDataLocation location, Profiles profiles)
        throws ConfigDataLocationNotFoundException {

        AppConfigurationProperties properties = loadProperties(resolverContext);
        List<AzureAppConfigDataResource> locations = new ArrayList<>();

        if (properties.getStores() == null || properties.getStores().isEmpty()) {
            throw new ConfigDataLocationNotFoundException(location,
                "No Azure App Configuration stores are configured. Please check your application properties.",
                new IllegalStateException("No stores configured"));
        }

        for (ConfigStore store : properties.getStores()) {
            locations.add(
                new AzureAppConfigDataResource(store, profiles, START_UP.get(), properties.getRefreshInterval()));
        }
        START_UP.set(false);
        return locations;
    }

    /**
     * Loads and validates Azure App Configuration properties from the configuration context.
     * 
     * @param context the configuration data location resolver context
     * @return validated Azure App Configuration properties
     */
    protected AppConfigurationProperties loadProperties(ConfigDataLocationResolverContext context) {
        Binder binder = context.getBinder();
        BindHandler bindHandler = getBindHandler(context);
        AppConfigurationProperties properties = binder.bind(AppConfigurationProperties.CONFIG_PREFIX,
            Bindable.of(AppConfigurationProperties.class), bindHandler).get();

        properties.validateAndInit();
        ReplicaLookUp replicaLookup = null;
        try {
            replicaLookup = new ReplicaLookUp(properties);
            replicaLookup.updateAutoFailoverEndpoints();
            context.getBootstrapContext().registerIfAbsent(ReplicaLookUp.class, InstanceSupplier.of(replicaLookup));
        } catch (NamingException e) {
            LOGGER.info("Failed to find DNS Entry for config store while looking for replicas.");
        }

        AzureAppConfigurationBootstrapRegistrar.register(context, binder, properties, replicaLookup);

        return properties;
    }

    private BindHandler getBindHandler(ConfigDataLocationResolverContext context) {
        return context.getBootstrapContext().getOrElse(BindHandler.class, null);
    }

}
