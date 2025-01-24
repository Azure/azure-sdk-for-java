// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

@EnableConfigurationProperties(AppConfigurationProviderProperties.class)
public class AzureAppConfigDataLocationResolver
    implements ConfigDataLocationResolver<AzureAppConfigDataResource> {

    private static final Log LOGGER = new DeferredLog();

    public static final String PREFIX = "azureAppConfiguration";

    private static final AtomicBoolean START_UP = new AtomicBoolean(true);

    @Override
    public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
        if (!location.hasPrefix(PREFIX)) {
            return false;
        }
        Boolean hasEndpoint = StringUtils.hasText(context.getBinder()
            .bind(AppConfigurationProperties.CONFIG_PREFIX + ".stores[0].endpoint", String.class)
            .orElse(""));
        Boolean hasConnectionString = StringUtils.hasText(context.getBinder()
            .bind(AppConfigurationProperties.CONFIG_PREFIX + ".stores[0].connection-string", String.class)
            .orElse(""));
        Boolean hasEndpoints = StringUtils.hasText(context.getBinder()
            .bind(AppConfigurationProperties.CONFIG_PREFIX + ".stores[0].endpoints", String.class)
            .orElse(""));
        Boolean hasConnectionStrings = StringUtils.hasText(context.getBinder()
            .bind(AppConfigurationProperties.CONFIG_PREFIX + ".stores[0].connection-strings", String.class)
            .orElse(""));

        return (hasEndpoint || hasConnectionString || hasEndpoints || hasConnectionStrings);
    }

    @Override
    public List<AzureAppConfigDataResource> resolve(ConfigDataLocationResolverContext context,
        ConfigDataLocation location)
        throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {
        return Collections.emptyList();
    }

    @Override
    public List<AzureAppConfigDataResource> resolveProfileSpecific(
        ConfigDataLocationResolverContext resolverContext, ConfigDataLocation location, Profiles profiles)
        throws ConfigDataLocationNotFoundException {

        Holder holder = loadProperties(resolverContext);
        List<AzureAppConfigDataResource> locations = new ArrayList<>();

        for (ConfigStore store : holder.properties.getStores()) {
            locations.add(
                new AzureAppConfigDataResource(store, profiles, holder.appProperties, START_UP.get(),
                    holder.properties.getRefreshInterval()));
        }
        START_UP.set(false);
        return locations;
    }

    protected Holder loadProperties(ConfigDataLocationResolverContext context) {
        Binder binder = context.getBinder();
        BindHandler bindHandler = getBindHandler(context);
        AppConfigurationProperties properties;
        AppConfigurationProviderProperties appProperties;
        Holder holder = new Holder();

        properties = binder.bind(AppConfigurationProperties.CONFIG_PREFIX,
            Bindable.of(AppConfigurationProperties.class), bindHandler).get();

        appProperties = binder.bind(AppConfigurationProviderProperties.CONFIG_PREFIX,
            Bindable.of(AppConfigurationProviderProperties.class), bindHandler)
            .orElseGet(AppConfigurationProviderProperties::new);

        properties.validateAndInit();
        ReplicaLookUp replicaLookup = null;
        try {
            replicaLookup = new ReplicaLookUp(properties);
            context.getBootstrapContext().registerIfAbsent(ReplicaLookUp.class, InstanceSupplier.of(replicaLookup));
        } catch (NamingException e) {
            LOGGER.info("Failed to find DNS Entry for config store while looking for replicas.");
        }

        AzureAppConfigurationBootstrapRegistrar.register(context, binder, properties, appProperties, replicaLookup);

        holder.properties = properties;
        holder.appProperties = appProperties;

        return holder;
    }

    private BindHandler getBindHandler(ConfigDataLocationResolverContext context) {
        return context.getBootstrapContext().getOrElse(BindHandler.class, null);
    }

    private class Holder {
        AppConfigurationProperties properties;

        AppConfigurationProviderProperties appProperties;
    }

}
