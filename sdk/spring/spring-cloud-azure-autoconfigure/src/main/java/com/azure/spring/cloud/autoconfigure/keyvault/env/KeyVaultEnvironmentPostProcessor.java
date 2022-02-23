// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.env;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultPropertySourceProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.utils.AzureGlobalPropertiesUtils;
import com.azure.spring.core.util.AzurePropertiesUtils;
import com.azure.spring.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

/**
 * Leverage {@link EnvironmentPostProcessor} to add Key Vault secrets as a property source.
 *
 * @since 4.0.0
 */
public class KeyVaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 1;

    private final Log logger;


    /**
     * Creates a new instance of {@link KeyVaultEnvironmentPostProcessor}.
     * @param logger The logger used in this class.
     */
    public KeyVaultEnvironmentPostProcessor(Log logger) {
        this.logger = logger;
    }

    /**
     * Construct a {@link KeyVaultEnvironmentPostProcessor} instance with default value.
     */
    public KeyVaultEnvironmentPostProcessor() {
        this.logger = new DeferredLog();
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

        // In propertySources list, smaller index has higher priority.
        final List<AzureKeyVaultPropertySourceProperties> propertySources = keyVaultSecretProperties.getPropertySources();
        Collections.reverse(propertySources);

        if (propertySources.isEmpty() && StringUtils.hasText(keyVaultSecretProperties.getEndpoint())) {
            propertySources.add(new AzureKeyVaultPropertySourceProperties());
        }

        if (isKeyVaultPropertySourceEnabled(keyVaultSecretProperties)) {
            for (AzureKeyVaultPropertySourceProperties propertySource : propertySources) {
                final AzureKeyVaultPropertySourceProperties properties = getMergeProperties(keyVaultSecretProperties,
                                                                                            propertySource);
                if (properties.isEnabled()) {
                    addKeyVaultPropertySource(environment, properties);
                }
            }
        } else {
            logger.debug("Key Vault 'propertySourceEnabled' or 'enabled' is not enabled");
        }
    }

    // TODO (xiada) better way to implement this
    private AzureKeyVaultPropertySourceProperties getMergeProperties(AzureKeyVaultSecretProperties secretProperties,
                                                                     AzureKeyVaultPropertySourceProperties propertySource) {
        AzureKeyVaultPropertySourceProperties mergedResult = new AzureKeyVaultPropertySourceProperties();
        AzurePropertiesUtils.mergeAzureCommonProperties(secretProperties, propertySource, mergedResult);

        mergedResult.setEndpoint(secretProperties.getEndpoint());
        mergedResult.setServiceVersion(secretProperties.getServiceVersion());
        mergedResult.setEnabled(propertySource.isEnabled());
        mergedResult.setName(propertySource.getName());
        mergedResult.setCaseSensitive(propertySource.getCaseSensitive());
        mergedResult.setSecretKeys(propertySource.getSecretKeys());
        mergedResult.setRefreshInterval(propertySource.getRefreshInterval());

        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(propertySource.getEndpoint()).to(mergedResult::setEndpoint);
        propertyMapper.from(propertySource.getServiceVersion()).to(mergedResult::setServiceVersion);

        return mergedResult;
    }


    /**
     * Add a Key Vault property source.
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
    private void addKeyVaultPropertySource(ConfigurableEnvironment environment,
                                          AzureKeyVaultPropertySourceProperties propertySource) {
        Assert.notNull(propertySource.getEndpoint(), "endpoint must not be null!");

        AzureKeyVaultSecretProperties secretProperties = new AzureKeyVaultSecretProperties();
        AzurePropertiesUtils.copyAzureCommonProperties(propertySource, secretProperties);
        secretProperties.setServiceVersion(propertySource.getServiceVersion());
        secretProperties.setEndpoint(propertySource.getEndpoint());
        try {
            final MutablePropertySources sources = environment.getPropertySources();
            final boolean caseSensitive = Boolean.TRUE.equals(propertySource.getCaseSensitive());
            final SecretClient secretClient = buildSecretClient(secretProperties);
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

    /**
     * Build a KeyVault Secret client
     * @param secretProperties secret properties
     * @return secret client
     */
    SecretClient buildSecretClient(AzureKeyVaultSecretProperties secretProperties) {
        return new SecretClientBuilderFactory(secretProperties).build().buildClient();
    }

    private AzureKeyVaultSecretProperties loadProperties(Binder binder) {
        AzureGlobalProperties azureProperties = binder
            .bind(AzureGlobalProperties.PREFIX, Bindable.of(AzureGlobalProperties.class))
            .orElseGet(AzureGlobalProperties::new);

        AzureKeyVaultSecretProperties existingValue = new AzureKeyVaultSecretProperties();
        AzureGlobalPropertiesUtils.loadProperties(azureProperties, existingValue);

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
        return properties.isEnabled()
            && (properties.isPropertySourceEnabled() && !properties.getPropertySources().isEmpty());
    }

    private boolean isKeyVaultClientAvailable() {
        return ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient",
                                    KeyVaultEnvironmentPostProcessor.class.getClassLoader());
    }

    /**
     *
     * @return The order value.
     */
    @Override
    public int getOrder() {
        return ORDER;
    }

}
