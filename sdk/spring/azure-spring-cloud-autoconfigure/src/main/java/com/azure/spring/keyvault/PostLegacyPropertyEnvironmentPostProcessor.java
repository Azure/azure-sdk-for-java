// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.keyvault;

import com.azure.spring.autoconfigure.unity.AbstractLegacyPropertyEnvironmentPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

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

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    /**
     * When only legacy properties are detected from each key vault property source, convert legacy properties to
     * the current, and create a new {@link Properties} to store the converted current properties of each key vault
     * property source.
     *
     * @param legacyToCurrentMap A {@link Properties} contains a map of all legacy properties and associated current properties.
     * @return A {@link Properties} to store mapped current properties
     */
    @Override
    protected Properties convertLegacyPropertyToCurrent(Properties legacyToCurrentMap) {
        Properties properties = new Properties();
        List<KeyVaultPropertySource> propertySourceList = getKeyVaultPropertySourceList();
        // Reverse traversal to keep the Key Vault property source of higher precedence could override the lower ones.
        for (int i = propertySourceList.size() - 1; i >= 0; i--) {
            KeyVaultPropertySource kvSource = propertySourceList.get(i);
            for (Map.Entry<Object, Object> entry : legacyToCurrentMap.entrySet()) {
                String legacyPropertyName = (String) entry.getKey();
                Object legacyPropertyValue = kvSource.getProperty(legacyPropertyName);
                if (legacyPropertyValue == null) {
                    continue;
                }
                String currentPropertyName = (String) entry.getValue();
                Object currentPropertyValue = kvSource.getProperty(currentPropertyName);
                if (currentPropertyValue == null) {
                    properties.put(currentPropertyName, legacyPropertyValue);
                    LOGGER.warn(toLogString(legacyPropertyName, currentPropertyName, kvSource.getName()));
                }
            }
        }
        return properties;
    }

    /**
     * Store all Key Vault property sources added by {@link KeyVaultEnvironmentPostProcessor} to a {@link List}.
     *
     * @return A {@link List} for all all Key Vault property sources.
     */
    private List<KeyVaultPropertySource> getKeyVaultPropertySourceList() {
        return environment.getPropertySources().stream()
                          .filter(source  -> source instanceof KeyVaultPropertySource)
                          .map(source -> ((KeyVaultPropertySource) source))
                          .collect(Collectors.toList());
    }

    /**
     * Add the mapped current properties to application environment, of which the precedence should be higher than that
     * of all of local Key Vault property sources to keep properties in multiple Key Vault sources in order.
     *
     * @param properties The converted current properties to be configured.
     */
    @Override
    protected void setConvertedPropertyToEnvironment(Properties properties) {
        // This post-processor is called multiple times but sets the properties only once.
        if (!CollectionUtils.isEmpty(properties)) {
            PropertiesPropertySource convertedPropertySource =
                new PropertiesPropertySource(PostLegacyPropertyEnvironmentPostProcessor.class.getName(), properties);
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

    public static String toLogString(String legacyPropertyName, String currentPropertyName, String propertySource) {
        return String.format("Deprecated property %s detected in Key Vault property source %s! Use %s instead!",
            legacyPropertyName, propertySource, currentPropertyName);
    }
}
