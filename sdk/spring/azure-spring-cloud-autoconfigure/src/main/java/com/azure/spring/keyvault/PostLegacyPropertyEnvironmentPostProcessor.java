// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import com.azure.spring.autoconfigure.unity.AbstractLegacyPropertyEnvironmentPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static com.azure.spring.keyvault.KeyVaultEnvironmentPostProcessorHelper.AZURE_KEYVAULT_PROPERTYSOURCE_NAME;
import static org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;

/**
 * Convert legacy properties in Key Vault property sources to the current and set into environment after
 * {@link KeyVaultEnvironmentPostProcessor}.
 *
 * Due to that properties in Key Vault has higher precedence than those in local, thus when comparing whether legacy
 * properties in Key Vault should be converted, they only need to be compared those in Key Vault.
 *
 * The converted current properties should have higher precedence than those in local, so the precedence is set
 * to be lower only than system properties, which is refered to the behavior of Key Vault property sources.
 */
public class PostLegacyPropertyEnvironmentPostProcessor extends AbstractLegacyPropertyEnvironmentPostProcessor {

    public static final int DEFAULT_ORDER = KeyVaultEnvironmentPostProcessor.DEFAULT_ORDER + 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(PostLegacyPropertyEnvironmentPostProcessor.class);

    private int order = DEFAULT_ORDER;
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * When only legacy properties are detected from each key vault property source, convert legacy properties to
     * the current, and create a new {@link Properties} to store the converted current properties of each key vault
     * property source.
     *
     * @param legacyToCurrentMap A {@JSONObject} contains a map of all legacy properties and associated current properties.
     * @param environment The application environment to get and set properties.
     * @return A {@link Properties} to store mapped current properties
     */
    @Override
    protected Properties mapLegacyPropertyToCurrent(JSONObject legacyToCurrentMap,
                                                ConfigurableEnvironment environment) {
        Properties properties = new Properties();
        List<KeyVaultPropertySource> propertySourceList = getKeyVaultPropertySourceList(environment);
        for (int i = propertySourceList.size()-1; i >=0; i--) {
            KeyVaultPropertySource kvSource = propertySourceList.get(i);
            Iterator iterator = legacyToCurrentMap.keys();
            while (iterator.hasNext()) {
                String legacyPropertyName = (String) iterator.next();
                Object legacyPropertyValue = kvSource.getProperty(legacyPropertyName);
                if (null != legacyPropertyValue) {
                    try {
                        String currentPropertyName = legacyToCurrentMap.getString(legacyPropertyName);
                        Object currentPropertyValue = kvSource.getProperty(currentPropertyName);;
                        if (null == currentPropertyValue) {
                            properties.put(currentPropertyName, legacyPropertyValue);
                            LOGGER.warn("Deprecated property {} detected in Key Vault property source {}! "
                                    + "Use {} instead!", legacyPropertyName, kvSource.getName(), currentPropertyName);
                        }
                    } catch (JSONException e) {
                        LOGGER.error("Error while loading current property name mapping to {}", legacyPropertyName);
                    }
                }
            }
        }
        return properties;
    }

    /**
     * Store all Key Vault property sources added by {@link KeyVaultEnvironmentPostProcessor} to a {@link List}.
     *
     * @param environment The application environment to get Key Vault property sources from.
     * @return A {@link List} for all all Key Vault property sources.
     */
    private List<KeyVaultPropertySource> getKeyVaultPropertySourceList(ConfigurableEnvironment environment) {
        List<KeyVaultPropertySource> propertySourceList = new ArrayList<KeyVaultPropertySource>();
        String kvPropertySources = environment.getProperty(KeyVaultProperties.getPropertyName(
            KeyVaultProperties.Property.ORDER), "");
        if (!kvPropertySources.isEmpty()) {
            Arrays.stream(kvPropertySources.split(","))
                  .map(String::trim)
                  .forEach(source -> addKvPropertySourceToList(source, environment, propertySourceList));
        }
        addKvPropertySourceToList(AZURE_KEYVAULT_PROPERTYSOURCE_NAME, environment, propertySourceList);

        return propertySourceList;
    }

    private void addKvPropertySourceToList(String propertySourceName, ConfigurableEnvironment environment,
                                           List<KeyVaultPropertySource> propertySourceList) {
        Optional.ofNullable(environment.getPropertySources().get(propertySourceName))
                .filter(source -> source instanceof KeyVaultPropertySource)
                .ifPresent(source -> propertySourceList.add((KeyVaultPropertySource) source));
    }

    @Override
    protected void setConvertedPropertyToEnvironment(ConfigurableEnvironment environment, Properties properties) {
        // This post-processor is called multiple times but sets the properties only once.
        if (!CollectionUtils.isEmpty(properties)) {
            PropertiesPropertySource convertedPropertySource =
                new PropertiesPropertySource(PostLegacyPropertyEnvironmentPostProcessor.class.getName(), properties);
            // This property source should have higher precedence than that of all of local Key Vault property sources.
            final MutablePropertySources sources = environment.getPropertySources();
            if (sources.contains(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                sources.addAfter(
                    SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                    convertedPropertySource
                );
            } else {
                sources.addFirst(convertedPropertySource);
            }
        }
    }
}
