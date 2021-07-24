// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.unity;

import com.azure.spring.keyvault.KeyVaultEnvironmentPostProcessor;
import com.azure.spring.keyvault.KeyVaultProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.CollectionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Convert legacy properties to the current and set into environment before {@link KeyVaultEnvironmentPostProcessor}.
 */
public class PreLegacyPropertyEnvironmentPostProcessor extends AbstractLegacyPropertyEnvironmentPostProcessor {

    public static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER + 1;
    private static final Map<String, String> KEYVAULT_PROPERTY_SUFFIX_MAP = new HashMap<String, String>();
    private static final Logger LOGGER = LoggerFactory.getLogger(PreLegacyPropertyEnvironmentPostProcessor.class);
    private static final String KEYVAULT_LEGACY_PREFIX = "azure.keyvault";
    public static final String DELIMITER = ".";

    static {
        // Load the mapping relationship of Key Vault legacy properties and associated current properties from
        // classpath, this is used for handling multiple key vault cases.
        try (
            InputStream inputStream = AbstractLegacyPropertyEnvironmentPostProcessor.class
                .getClassLoader()
                .getResourceAsStream("legacy-keyvault-property-suffix-mapping.properties");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))
        ) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                KEYVAULT_PROPERTY_SUFFIX_MAP.put(line.split("=")[0], line.split("=")[1]);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Fail to load legacy-keyvault-property-suffix-mapping.properties",
                exception);
        }
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    /**
     * Build a legacy Key Vault property map of multiple key vault use case. When multiple Key Vaults are used, Key
     * Vault property names are not fixed and varies across user definition.
     *
     * @param environment The application environment to load property from.
     * @return A map contains all possible Key Vault properties.
     */
    @Override
    protected Map<String, String> getMultipleKeyVaultsPropertyMap(ConfigurableEnvironment environment) {
        String keyVaultNames = getMultipleKeyVaultNames(environment);
        if (null == keyVaultNames) {
            return null;
        }

        Map<String, String> legacyToCurrentMap = new HashMap<String, String>();
        for (String keyVault : keyVaultNames.split(",")) {
            keyVault = keyVault.trim();
            for (Map.Entry<String, String> mapping : KEYVAULT_PROPERTY_SUFFIX_MAP.entrySet()) {
                String legacy = String.join(DELIMITER, KEYVAULT_LEGACY_PREFIX, keyVault, mapping.getKey());
                String current = String.join(DELIMITER, KeyVaultProperties.PREFIX, keyVault, mapping.getValue());
                legacyToCurrentMap.put(legacy, current);
            }
        }
        return legacyToCurrentMap;
    }

    /**
     * Load all possible Key Vault names from property "spring.cloud.azure.keyvault.order" if existed. Otherwise load
     * from legacy property "azure.keyvault.order".
     *
     * @param environment The application environment to load multiple Key Vault names from.
     * @return A string with all Key Vaults names concatenated by commas, or null if no Key Vault names specified.
     */
    private String getMultipleKeyVaultNames(ConfigurableEnvironment environment) {
        if (getPropertyValue(KeyVaultProperties.PREFIX + ".order", environment) != null) {
            return (String) getPropertyValue(KeyVaultProperties.PREFIX + ".order", environment);
        } else {
            return (String) getPropertyValue(KEYVAULT_LEGACY_PREFIX + ".order", environment);
        }
    }

    /**
     * When only legacy properties are detected from all property sources, convert legacy properties to the current,
     * and create a new {@link Properties} to store all of the converted current properties.
     * @param legacyToCurrentMap A map contains a map of all legacy properties and associated current properties.
     * @param environment The application environment to get and set properties.
     * @return A {@link Properties} to store mapped current properties
     */
    @Override
    protected Properties mapLegacyPropertyToCurrent(Map<String, String> legacyToCurrentMap,
                                                    ConfigurableEnvironment environment) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : legacyToCurrentMap.entrySet()) {
            String legacyPropertyName = entry.getKey();
            Object legacyPropertyValue = getPropertyValue(legacyPropertyName, environment);
            if (null != legacyPropertyValue) {
                String currentPropertyName = entry.getValue();
                Object currentPropertyValue = getPropertyValue(currentPropertyName, environment);
                if (null == currentPropertyValue) {
                    properties.put(currentPropertyName, legacyPropertyValue);
                    LOGGER.warn("Deprecated property {} detected! Use {} instead!", legacyPropertyName,
                        currentPropertyName);
                }
            }
        }
        return properties;
    }

    /**
     * Get property value from all property sources in the environment.
     * @param propertyName Name of the property to get value.
     * @param environment Environment to get value from.
     * @return Property value.
     */
    protected Object getPropertyValue(String propertyName, ConfigurableEnvironment environment) {
        return Binder.get(environment)
                     .bind(propertyName, Bindable.of(Object.class))
                     .orElse(null);
    }

    /**
     * Add the mapped current properties to application environment, of which the precedence does not count.
     *
     * @param environment The application environment to set properties.
     * @param properties The converted current properties to be configured.
     */
    @Override
    protected void setConvertedPropertyToEnvironment(ConfigurableEnvironment environment, Properties properties) {
        // This post-processor is called multiple times but sets the properties only once.
        if (!CollectionUtils.isEmpty(properties)) {
            PropertiesPropertySource convertedPropertySource =
                new PropertiesPropertySource(PreLegacyPropertyEnvironmentPostProcessor.class.getName(), properties);
            environment.getPropertySources().addLast(convertedPropertySource);
        }
    }
}
