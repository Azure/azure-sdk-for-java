// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;

import org.springframework.beans.BeanUtils;
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
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.implementation.autofailover.ReplicaLookUp;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class AzureAppConfigDataLocationResolver
    implements ConfigDataLocationResolver<AzureAppConfigDataResource> {

    public static final String PREFIX = "azureAppConfiguration";

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
        // TODO (mametcal) This file never loads
        Boolean hasClientConfigs = StringUtils
            .hasText(context.getBinder().bind("spring.cloud.appconfiguration.version", String.class)
                .orElse(""));
        
        //if (!context.getBootstrapContext().isRegistered(AzureAppConfigDataLoader.class)) {
        //    return false;
        //}

        return (hasEndpoint || hasConnectionString);
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
                new AzureAppConfigDataResource(store, profiles, holder.appProperties));

        }

        return locations;
    }

    protected Holder loadProperties(ConfigDataLocationResolverContext context) {
        Binder binder = context.getBinder();
        BindHandler bindHandler = getBindHandler(context);
        AppConfigurationProperties properties;
        AppConfigurationProviderProperties appProperties;
        Holder holder = new Holder();

        if (context.getBootstrapContext().isRegistered(AppConfigurationProperties.class)) {
            properties = new AppConfigurationProperties();
            BeanUtils.copyProperties(context.getBootstrapContext().get(AppConfigurationProperties.class), properties);
        } else {
            properties = binder.bind(AppConfigurationProperties.CONFIG_PREFIX,
                Bindable.of(AppConfigurationProperties.class), bindHandler).get();
        }

        if (context.getBootstrapContext().isRegistered(AppConfigurationProviderProperties.class)) {
            appProperties = new AppConfigurationProviderProperties();
            BeanUtils.copyProperties(context.getBootstrapContext().get(AppConfigurationProviderProperties.class),
                appProperties);
        } else {
            appProperties = binder.bind(AppConfigurationProviderProperties.CONFIG_PREFIX,
                Bindable.of(AppConfigurationProviderProperties.class), bindHandler)
                .orElseGet(AppConfigurationProviderProperties::new);
        }

        properties.validateAndInit();
        ReplicaLookUp replicaLookup = null;
        try {
            replicaLookup = new ReplicaLookUp(properties);
        } catch (NamingException e) {
            // TODO Auto-generated catch block
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