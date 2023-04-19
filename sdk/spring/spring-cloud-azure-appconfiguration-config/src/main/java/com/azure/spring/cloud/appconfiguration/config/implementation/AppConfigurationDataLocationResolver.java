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
import org.springframework.util.StringUtils;

import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import com.azure.spring.cloud.autoconfigure.implementation.appconfiguration.AzureAppConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureHttpConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier;
import com.azure.spring.cloud.service.implementation.appconfiguration.ConfigurationClientBuilderFactory;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;

public class AppConfigurationDataLocationResolver
    implements ConfigDataLocationResolver<AppConfigDataResource>, Ordered {

    @Override
    public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
        Boolean properties = context.getBinder().bind(AppConfigurationProperties.CONFIG_PREFIX + ".enabled", Boolean.class)
            .orElse(false);
        //Boolean appProperties = context.getBinder().bind(AppConfigurationProviderProperties.CONFIG_PREFIX + ".version", Boolean.class)
        //    .orElse(false);
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
        AppConfigurationProperties properties = null;
        AppConfigurationProviderProperties appProperties = null;
        Holder holder = new Holder();

        if (context.getBootstrapContext().isRegistered(AppConfigurationProperties.class)) {
            holder.properties = new AppConfigurationProperties();
            BeanUtils.copyProperties(context.getBootstrapContext().get(AppConfigurationProperties.class), properties);
        } else {
            holder.properties = binder.bind(AppConfigurationProperties.CONFIG_PREFIX,
                Bindable.of(AppConfigurationProperties.class), bindHandler).get();
        }
        
        if (context.getBootstrapContext().isRegistered(AppConfigurationProviderProperties.class)) {
            holder.appProperties = new AppConfigurationProviderProperties();
            BeanUtils.copyProperties(context.getBootstrapContext().get(AppConfigurationProviderProperties.class), appProperties);
        } else {
            holder.appProperties = binder.bind(AppConfigurationProviderProperties.CONFIG_PREFIX,
                Bindable.of(AppConfigurationProviderProperties.class), bindHandler).orElseGet(AppConfigurationProviderProperties::new);
        }

        holder.kvcf = appConfigurationKeyVaultClientFactory(context, binder);
        holder.rcb = replicaClientBuilder(context, binder, holder.appProperties, holder.kvcf);
        holder.rcf = buildClientFactory(holder.rcb, holder.properties);

        return holder;
    }

    @Override
    public List<AppConfigDataResource> resolveProfileSpecific(
        ConfigDataLocationResolverContext resolverContext, ConfigDataLocation location, Profiles profiles)
        throws ConfigDataLocationNotFoundException {

        Holder holder = loadProperties(resolverContext);
        List<AppConfigDataResource> locations = new ArrayList<>();

        for (ConfigStore store : holder.properties.getStores()) {
            locations.add(new AppConfigDataResource(store, profiles, holder.kvcf, holder.rcf, holder.rcb, holder.appProperties));

        }

        return locations;
    }

    private BindHandler getBindHandler(ConfigDataLocationResolverContext context) {
        return context.getBootstrapContext().getOrElse(BindHandler.class, null);
    }

    private class Holder {
        AppConfigurationProperties properties;
        
        AppConfigurationProviderProperties appProperties;

        AppConfigurationKeyVaultClientFactory kvcf;

        AppConfigurationReplicaClientFactory rcf;
        
        AppConfigurationReplicaClientsBuilder rcb;
    }

    AppConfigurationKeyVaultClientFactory appConfigurationKeyVaultClientFactory(
        ConfigDataLocationResolverContext context, Binder binder)
        throws IllegalArgumentException {
        AzureGlobalProperties globalSource = binder.bindOrCreate(AzureGlobalProperties.PREFIX,
            AzureGlobalProperties.class);
        AzureGlobalProperties serviceSource = binder.bindOrCreate(AzureKeyVaultSecretProperties.PREFIX,
            AzureGlobalProperties.class);

        AzureKeyVaultSecretProperties globalProperties = AzureGlobalPropertiesUtils.loadProperties(globalSource,
            new AzureKeyVaultSecretProperties());
        AzureKeyVaultSecretProperties clientProperties = AzureGlobalPropertiesUtils.loadProperties(serviceSource,
            new AzureKeyVaultSecretProperties());

        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(globalProperties, clientProperties);

        SecretClientBuilderFactory secretClientBuilderFactory = new SecretClientBuilderFactory(clientProperties);

        boolean credentialConfigured = isCredentialConfigured(clientProperties);

        return new AppConfigurationKeyVaultClientFactory(secretClientBuilderFactory, credentialConfigured);
    }

    AppConfigurationReplicaClientFactory buildClientFactory(AppConfigurationReplicaClientsBuilder clientBuilder,
        AppConfigurationProperties properties) {
        return new AppConfigurationReplicaClientFactory(clientBuilder, properties.getStores());
    }

    AppConfigurationReplicaClientsBuilder replicaClientBuilder(ConfigDataLocationResolverContext context, Binder binder,
        AppConfigurationProviderProperties appProperties, AppConfigurationKeyVaultClientFactory keyVaultClientFactory) {
        AzureGlobalProperties globalSource = binder.bindOrCreate(AzureGlobalProperties.PREFIX,
            AzureGlobalProperties.class);
        AzureGlobalProperties serviceSource = binder.bindOrCreate(AzureAppConfigurationProperties.PREFIX,
            AzureGlobalProperties.class);

        AzureGlobalProperties globalProperties = AzureGlobalPropertiesUtils.loadProperties(globalSource,
            new AzureGlobalProperties());
        AzureAppConfigurationProperties clientProperties = AzureGlobalPropertiesUtils.loadProperties(serviceSource,
            new AzureAppConfigurationProperties());

        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(globalProperties, clientProperties);

        ConfigurationClientBuilderFactory clientFactory = new ConfigurationClientBuilderFactory(clientProperties);

        clientFactory.setSpringIdentifier(AzureSpringIdentifier.AZURE_SPRING_APP_CONFIG);
        //customizers.orderedStream().forEach(clientFactory::addBuilderCustomizer);

        boolean credentialConfigured = isCredentialConfigured(clientProperties);

        AppConfigurationReplicaClientsBuilder clientBuilder = new AppConfigurationReplicaClientsBuilder(
            appProperties.getMaxRetries(), clientFactory, credentialConfigured);

        clientBuilder.setIsKeyVaultConfigured(keyVaultClientFactory.isConfigured());

        return clientBuilder;
    }

    private boolean isCredentialConfigured(AbstractAzureHttpConfigurationProperties properties) {
        if (properties.getCredential() != null) {
            TokenCredentialConfigurationProperties tokenProps = properties.getCredential();
            if (StringUtils.hasText(tokenProps.getClientCertificatePassword())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getClientCertificatePath())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getClientId())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getClientSecret())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getUsername())) {
                return true;
            } else if (StringUtils.hasText(tokenProps.getPassword())) {
                return true;
            }
        }

        return false;
    }

}
