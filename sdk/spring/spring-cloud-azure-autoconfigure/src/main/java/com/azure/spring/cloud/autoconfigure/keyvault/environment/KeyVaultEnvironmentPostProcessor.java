// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.environment;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultPropertySourceProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;
import org.apache.commons.logging.Log;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

/**
 * Leverage {@link EnvironmentPostProcessor} to insert {@link KeyVaultPropertySource}s into {@link ConfigurableEnvironment}.
 * {@link KeyVaultPropertySource}s are constructed according to {@link AzureKeyVaultSecretProperties},
 *
 * @since 4.0.0
 */
public class KeyVaultEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * The order value of the {@link KeyVaultEnvironmentPostProcessor}.
     */
    public static final int ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 1;

    private static final String SKIP_CONFIGURE_REASON_FORMAT = "Skip configuring Key Vault PropertySource because %s.";

    private final Log logger;


    /**
     * Creates a new instance of {@link KeyVaultEnvironmentPostProcessor}.
     * @param logger The logger used in this class.
     */
    public KeyVaultEnvironmentPostProcessor(Log logger) {
        this.logger = logger;
    }

    /**
     * Construct a {@link KeyVaultEnvironmentPostProcessor} instance with a new {@link DeferredLog}.
     */
    public KeyVaultEnvironmentPostProcessor() {
        this.logger = new DeferredLog();
    }

    /**
     * Construct {@link KeyVaultPropertySource}s according to {@link AzureKeyVaultSecretProperties},
     * then insert these {@link KeyVaultPropertySource}s into {@link ConfigurableEnvironment}.
     *
     * @param environment the environment.
     * @param application the application.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!isKeyVaultClientOnClasspath()) {
            logger.debug(String.format(SKIP_CONFIGURE_REASON_FORMAT, "com.azure:azure-security-keyvault-secrets doesn't exist in classpath"));
            return;
        }

        final AzureKeyVaultSecretProperties secretProperties = loadProperties(environment);
        if (!secretProperties.isPropertySourceEnabled()) {
            logger.debug(String.format(SKIP_CONFIGURE_REASON_FORMAT, "spring.cloud.azure.keyvault.secret.property-source-enabled=false"));
            return;
        }
        if (secretProperties.getPropertySources().isEmpty()) {
            logger.debug(String.format(SKIP_CONFIGURE_REASON_FORMAT, "spring.cloud.azure.keyvault.secret.property-sources is empty"));
            return;
        }

        final List<AzureKeyVaultPropertySourceProperties> propertiesList = secretProperties.getPropertySources();
        List<KeyVaultPropertySource> keyVaultPropertySources = buildKeyVaultPropertySourceList(propertiesList);
        final MutablePropertySources propertySources = environment.getPropertySources();
        // reverse iterate order making sure smaller index has higher priority.
        for (int i = keyVaultPropertySources.size() - 1; i >= 0; i--) {
            KeyVaultPropertySource propertySource = keyVaultPropertySources.get(i);
            logger.debug("Inserting Key Vault PropertySource. name = " + propertySource.getName());
            if (propertySources.contains(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                propertySources.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, propertySource);
            } else {
                propertySources.addFirst(propertySource);
            }
        }
    }

    private List<KeyVaultPropertySource> buildKeyVaultPropertySourceList(
            List<AzureKeyVaultPropertySourceProperties> propertiesList) {
        List<KeyVaultPropertySource> propertySources = new ArrayList<>();
        for (int i = 0; i < propertiesList.size(); i++) {
            AzureKeyVaultPropertySourceProperties properties = propertiesList.get(i);
            if (!properties.isEnabled()) {
                logger.debug(String.format(SKIP_CONFIGURE_REASON_FORMAT, "spring.cloud.azure.keyvault.secret.property-sources[" + i + "].enabled = false"));
                continue;
            }
            if (!StringUtils.hasText(properties.getEndpoint())) {
                logger.debug(String.format(SKIP_CONFIGURE_REASON_FORMAT, "spring.cloud.azure.keyvault.secret.property-sources[" + i + "].endpoint is empty"));
                continue;
            }
            propertySources.add(buildKeyVaultPropertySource(properties));
        }
        return propertySources;
    }

    private KeyVaultPropertySource buildKeyVaultPropertySource(
            AzureKeyVaultPropertySourceProperties properties) {
        try {
            final KeyVaultOperation keyVaultOperation = new KeyVaultOperation(
                    buildSecretClient(properties),
                    properties.getRefreshInterval(),
                    properties.getSecretKeys(),
                    properties.isCaseSensitive());
            return new KeyVaultPropertySource(properties.getName(), keyVaultOperation);
        } catch (final Exception exception) {
            throw new IllegalStateException("Failed to configure KeyVault property source", exception);
        }
    }

    private SecretClient buildSecretClient(AzureKeyVaultPropertySourceProperties propertySourceProperties) {
        AzureKeyVaultSecretProperties secretProperties = toAzureKeyVaultSecretProperties(propertySourceProperties);
        return buildSecretClient(secretProperties);
    }

    private AzureKeyVaultSecretProperties toAzureKeyVaultSecretProperties(
            AzureKeyVaultPropertySourceProperties propertySourceProperties) {
        AzureKeyVaultSecretProperties secretProperties = new AzureKeyVaultSecretProperties();
        AzurePropertiesUtils.copyAzureCommonProperties(propertySourceProperties, secretProperties);
        secretProperties.setEndpoint(propertySourceProperties.getEndpoint());
        secretProperties.setServiceVersion(propertySourceProperties.getServiceVersion());
        return secretProperties;
    }

    /**
     * Build a KeyVault Secret client
     * @param secretProperties secret properties
     * @return secret client
     */
    SecretClient buildSecretClient(AzureKeyVaultSecretProperties secretProperties) {
        return new SecretClientBuilderFactory(secretProperties).build().buildClient();
    }

    AzureKeyVaultSecretProperties loadProperties(ConfigurableEnvironment environment) {
        Binder binder = Binder.get(environment);
        AzureGlobalProperties globalProperties = binder
            .bind(AzureGlobalProperties.PREFIX, Bindable.of(AzureGlobalProperties.class))
            .orElseGet(AzureGlobalProperties::new);

        AzureKeyVaultSecretProperties secretProperties = binder
                .bind(AzureKeyVaultSecretProperties.PREFIX, Bindable.of(AzureKeyVaultSecretProperties.class))
                .orElseGet(AzureKeyVaultSecretProperties::new);

        List<AzureKeyVaultPropertySourceProperties> list = secretProperties.getPropertySources();

        // Load properties from global properties.
        for (int i = 0; i < list.size(); i++) {
            list.set(i, buildMergedProperties(globalProperties, list.get(i)));
        }

        // Name must be unique for each property source.
        // Because MutablePropertySources#add will remove property source with existing name.
        for (int i = 0; i < list.size(); i++) {
            AzureKeyVaultPropertySourceProperties propertySourceProperties = list.get(i);
            if (!StringUtils.hasText(propertySourceProperties.getName())) {
                propertySourceProperties.setName(buildPropertySourceName(i));
            }
        }
        return secretProperties;
    }

    private AzureKeyVaultPropertySourceProperties buildMergedProperties(
            AzureGlobalProperties globalProperties,
            AzureKeyVaultPropertySourceProperties propertySourceProperties) {
        AzureKeyVaultPropertySourceProperties mergedProperties = new AzureKeyVaultPropertySourceProperties();
        AzurePropertiesUtils.mergeAzureCommonProperties(globalProperties, propertySourceProperties, mergedProperties);
        mergedProperties.setEnabled(propertySourceProperties.isEnabled());
        mergedProperties.setName(propertySourceProperties.getName());
        mergedProperties.setEndpoint(propertySourceProperties.getEndpoint());
        mergedProperties.setServiceVersion(propertySourceProperties.getServiceVersion());
        mergedProperties.setCaseSensitive(propertySourceProperties.isCaseSensitive());
        mergedProperties.setSecretKeys(propertySourceProperties.getSecretKeys());
        mergedProperties.setRefreshInterval(propertySourceProperties.getRefreshInterval());
        return mergedProperties;
    }

    String buildPropertySourceName(int index) {
        return "azure-key-vault-secret-property-source-" + index;
    }

    private boolean isKeyVaultClientOnClasspath() {
        return ClassUtils.isPresent("com.azure.security.keyvault.secrets.SecretClient",
                                    KeyVaultEnvironmentPostProcessor.class.getClassLoader());
    }

    /**
     * Get the order value of this object.
     * @return The order value.
     */
    @Override
    public int getOrder() {
        return ORDER;
    }

}
