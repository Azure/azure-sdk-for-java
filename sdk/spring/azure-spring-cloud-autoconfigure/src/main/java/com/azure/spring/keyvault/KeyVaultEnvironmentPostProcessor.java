// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultPropertySourceProperties;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.keyvault.secrets.SecretClientBuilderFactory;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.core.properties.AzurePropertiesUtils;
import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.Collections;
import java.util.List;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

/**
 * Leverage {@link EnvironmentPostProcessor} to add Key Vault secrets as a property source.
 */
public class KeyVaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 1;

    private final Log logger;

    public KeyVaultEnvironmentPostProcessor(Log logger) {
        this.logger = logger;
    }


    /**
     * Post-process the environment.
     *
     * <p>
     * Here we are going to process any key vault(s) and make them as available PropertySource(s). Note this supports
     * both the singular key vault setup, as well as the multiple key vault setup.
     * </p>
     *
     * @param environment the environment.
     * @param application the application.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isKeyVaultClientAvailable()) {
            logger.info("Key Vault client is not present, skip the Key Vault property source");
            return;
        }

        final AzureKeyVaultSecretProperties keyVaultSecretProperties = loadProperties(Binder.get(environment));

        if (isKeyVaultPropertySourceEnabled(keyVaultSecretProperties)) {

            // TODO (xiada): confirm the order
            final List<AzureKeyVaultPropertySourceProperties> propertySources = keyVaultSecretProperties.getPropertySources();
            Collections.reverse(propertySources);

            if (propertySources.isEmpty()) {
                propertySources.add(new AzureKeyVaultPropertySourceProperties());
            }

            for (AzureKeyVaultPropertySourceProperties propertySource : propertySources) {
                final AzureKeyVaultPropertySourceProperties properties = getMergeProperties(keyVaultSecretProperties,
                                                                                            propertySource);
                addKeyVaultPropertySource(environment, properties);
            }
        } else {
            logger.debug("Key Vault property source is not enabled");
        }
    }

    // TODO (xiada) better way to implement this
    private AzureKeyVaultPropertySourceProperties getMergeProperties(AzureKeyVaultSecretProperties secretProperties,
                                                                     AzureKeyVaultPropertySourceProperties propertySource) {
        AzureKeyVaultPropertySourceProperties mergedResult = new AzureKeyVaultPropertySourceProperties();
        AzurePropertiesUtils.copyAzureProperties(secretProperties, mergedResult);
        AzurePropertiesUtils.copyAzurePropertiesIgnoreNull(propertySource, mergedResult);

        mergedResult.setVaultUrl(secretProperties.getVaultUrl());
        mergedResult.setServiceVersion(secretProperties.getServiceVersion());
        mergedResult.setName(propertySource.getName());
        mergedResult.setCaseSensitive(propertySource.getCaseSensitive());
        mergedResult.setSecretKeys(propertySource.getSecretKeys());
        mergedResult.setRefreshInterval(propertySource.getRefreshInterval());

        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(propertySource.getVaultUrl()).to(mergedResult::setVaultUrl);
        propertyMapper.from(propertySource.getServiceVersion()).to(mergedResult::setServiceVersion);

        return mergedResult;
    }


    /**
     * Add a key vault property source.
     *
     * <p>
     * The normalizedName is used to target a specific key vault (note if the name is the empty string it works as
     * before with only one key vault present). The normalized name is the name of the specific key vault plus a
     * trailing "." at the end.
     * </p>
     *
     * @param environment The Spring environment.
     * @param propertySource The property source properties.
     * @throws IllegalStateException If KeyVaultOperations fails to initialize.
     */
    public void addKeyVaultPropertySource(ConfigurableEnvironment environment,
                                          AzureKeyVaultPropertySourceProperties propertySource) {
        Assert.notNull(propertySource.getVaultUrl(), "vaultUri must not be null!");

        AzureKeyVaultSecretProperties secretProperties = new AzureKeyVaultSecretProperties();
        AzurePropertiesUtils.copyAzureProperties(propertySource, secretProperties);
        secretProperties.setServiceVersion(propertySource.getServiceVersion());
        secretProperties.setVaultUrl(propertySource.getVaultUrl());

        final SecretClient secretClient = new SecretClientBuilderFactory(secretProperties).build().buildClient();
        try {
            final MutablePropertySources sources = environment.getPropertySources();
            final boolean caseSensitive = Boolean.TRUE.equals(propertySource.getCaseSensitive());
            final KeyVaultOperation keyVaultOperation = new KeyVaultOperation(secretClient,
                                                                              propertySource.getRefreshInterval(),
                                                                              propertySource.getSecretKeys(),
                                                                              caseSensitive);

            KeyVaultPropertySource keyVaultPropertySource = new KeyVaultPropertySource(propertySource.getName(),
                                                                                       keyVaultOperation);

            if (sources.contains(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                sources.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, keyVaultPropertySource);
            } else {
                // TODO (xiada): confirm the order
                sources.addFirst(keyVaultPropertySource);
            }

        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to configure KeyVault property source", ex);
        }
    }

    private AzureKeyVaultSecretProperties loadProperties(Binder binder) {
        AzureGlobalProperties azureProperties = binder
            .bind(AzureGlobalProperties.PREFIX, Bindable.of(AzureGlobalProperties.class))
            .orElseGet(AzureGlobalProperties::new);

        AzureKeyVaultSecretProperties existingValue = new AzureKeyVaultSecretProperties();
        AzurePropertiesUtils.copyAzureProperties(azureProperties, existingValue);


        return binder
            .bind(AzureKeyVaultSecretProperties.PREFIX,
                  Bindable.of(AzureKeyVaultSecretProperties.class).withExistingValue(existingValue))
            .orElseGet(AzureKeyVaultSecretProperties::new);
    }

    /**
     * Is the Key Vault property source enabled.
     *
     * @param properties The Azure Key Vault Secret properties.
     * @return true if the key vault is enabled, false otherwise.
     */
    private boolean isKeyVaultPropertySourceEnabled(AzureKeyVaultSecretProperties properties) {
        return Boolean.TRUE.equals(properties.getPropertySourceEnabled())
                   || !properties.getPropertySources().isEmpty();
    }

    private boolean isKeyVaultClientAvailable() {
        return ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient",
                                    KeyVaultEnvironmentPostProcessor.class.getClassLoader());
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

}
