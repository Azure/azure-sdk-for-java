package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.Ordered;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;

public class AppConfigurationDataLocationResolver
    implements ConfigDataLocationResolver<AppConfigDataResource>, Ordered {

    public static final String PREFIX = "azureAppConfiguration";

    @Override
    public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
        if (!location.hasPrefix(PREFIX)) {
            return false;
        }
        Boolean properties = context.getBinder()
            .bind(AppConfigurationProperties.CONFIG_PREFIX + ".enabled", Boolean.class)
            .orElse(false);
        // TODO (mametcal) Need to figure out how to get library configuration loaded.
        // Boolean appProperties = context.getBinder().bind(AppConfigurationProviderProperties.CONFIG_PREFIX +
        // ".version", Boolean.class)
        // .orElse(false);
        return properties && true;
    }

    @Override
    public List<AppConfigDataResource> resolve(ConfigDataLocationResolverContext context, ConfigDataLocation location)
        throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {
        return Collections.emptyList();
    }

    @Override
    public int getOrder() {
        return -1;
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

        AppConfigurationBootstrapRegistrar.register(context, binder, properties, appProperties);

        holder.properties = properties;
        holder.appProperties = appProperties;

        return holder;
    }

    @Override
    public List<AppConfigDataResource> resolveProfileSpecific(
        ConfigDataLocationResolverContext resolverContext, ConfigDataLocation location, Profiles profiles)
        throws ConfigDataLocationNotFoundException {

        Holder holder = loadProperties(resolverContext);
        List<AppConfigDataResource> locations = new ArrayList<>();
        for (ConfigStore store : holder.properties.getStores()) {
            locations.add(
                new AppConfigDataResource(store, profiles, holder.appProperties));

        }

        return locations;
    }

    private BindHandler getBindHandler(ConfigDataLocationResolverContext context) {
        return context.getBootstrapContext().getOrElse(BindHandler.class, null);
    }

    private class Holder {
        AppConfigurationProperties properties;

        AppConfigurationProviderProperties appProperties;
    }

}
