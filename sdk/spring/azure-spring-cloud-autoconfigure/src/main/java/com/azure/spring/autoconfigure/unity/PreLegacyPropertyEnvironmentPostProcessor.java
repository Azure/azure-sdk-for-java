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
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Properties;

import static com.azure.spring.utils.PropertyLoader.loadPropertiesFromFile;

/**
 * Convert legacy properties to the current and set into environment before {@link KeyVaultEnvironmentPostProcessor}.
 */
public class PreLegacyPropertyEnvironmentPostProcessor extends AbstractLegacyPropertyEnvironmentPostProcessor {

    public static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER + 1;
    private static Properties keyvaultPropertySuffixMap;
    private static final Logger LOGGER = LoggerFactory.getLogger(PreLegacyPropertyEnvironmentPostProcessor.class);
    private static final String KEYVAULT_LEGACY_PREFIX = "azure.keyvault";
    public static final String DELIMITER = ".";

    static {
        // Load the mapping relationship of Key Vault legacy properties and associated current properties from
        // classpath, this is used for handling multiple key vault cases.
        keyvaultPropertySuffixMap = loadPropertiesFromFile("legacy-keyvault-property-suffix-mapping.properties");
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Override
    protected Properties buildLegacyToCurrentPropertyMap() {
        Properties legacyToCurrentMap = super.buildLegacyToCurrentPropertyMap();
        String keyVaultNames = getMultipleKeyVaultNames();
        if (keyVaultNames != null) {
            return addMultipleKVPropertyToMap(keyVaultNames, legacyToCurrentMap);
        }
        return legacyToCurrentMap;
    }

    /**
     * Build a legacy Key Vault property map of multiple key vault use case. When multiple Key Vaults are used, Key
     * Vault property names are not fixed and varies across user definition.
     *
     * @param keyVaultNames A string with all Key Vaults names concatenated by commas.
     * @param legacyToCurrentMap A {@link Properties} contains a map of all legacy properties and associated current ones.
     * @return A map contains all possible Key Vault properties names.
     */
    protected Properties addMultipleKVPropertyToMap(String keyVaultNames,
                                                    Properties legacyToCurrentMap) {
        for (String keyVault : keyVaultNames.split(",")) {
            keyVault = keyVault.trim();
            for (Map.Entry<Object, Object> mapping : keyvaultPropertySuffixMap.entrySet()) {
                String legacy = buildLegacyPropertyName(keyVault, (String) mapping.getKey());
                String current = buildCurrentPropertyName(keyVault, (String) mapping.getValue());
                legacyToCurrentMap.put(legacy, current);
            }
        }
        return legacyToCurrentMap;
    }

    /**
     * Load all possible Key Vault names from property "spring.cloud.azure.keyvault.order" if existed. Otherwise load
     * from legacy property "azure.keyvault.order".
     *
     * @return A string with all Key Vaults names concatenated by commas, or null if no Key Vault names specified.
     */
    private String getMultipleKeyVaultNames() {
        if (getPropertyValue(KeyVaultProperties.PREFIX + ".order") != null) {
            return (String) getPropertyValue(KeyVaultProperties.PREFIX + ".order");
        } else {
            return (String) getPropertyValue(KEYVAULT_LEGACY_PREFIX + ".order");
        }
    }

    /**
     * For the use case of multiple Key Vaults, create the legacy Key Vault property name for a specific Key Vault.
     *
     * @param keyVaultName The name of a Key Vault, which is used as the property name's infix.
     * @param legacyPropertySuffix Suffix of a legacy Key Vault property name, which is loaded from the keys of
     *                             legacy-keyvault-property-suffix-mapping.properties
     * @return A legacy Key Vault property name with a Key Vault name as infix.
     */
    private String buildLegacyPropertyName(String keyVaultName, String legacyPropertySuffix) {
        return String.join(DELIMITER, KEYVAULT_LEGACY_PREFIX, keyVaultName, legacyPropertySuffix);
    }

    /**
     * For the use case of multiple Key Vaults, create the current Key Vault property name for a specific Key Vault.
     *
     * @param keyVaultName The name of a Key Vault, which is used as the property name's infix.
     * @param currentPropertySuffix Suffix of a current Key Vault property name, which is loaded from the values of
     *                             legacy-keyvault-property-suffix-mapping.properties
     * @return A current Key Vault property name with a Key Vault name as infix.
     */
    private String buildCurrentPropertyName(String keyVaultName, String currentPropertySuffix) {
        return String.join(DELIMITER, KeyVaultProperties.PREFIX, keyVaultName, currentPropertySuffix);
    }

    /**
     * When only legacy properties are detected from all property sources, convert legacy properties to the current,
     * and create a new {@link Properties} to store all of the converted current properties.
     * @param legacyToCurrentMap A {@link Properties} contains a map of all legacy properties and associated current properties.
     * @return A {@link Properties} to store mapped current properties
     */
    @Override
    protected Properties convertLegacyPropertyToCurrent(Properties legacyToCurrentMap) {
        Properties properties = new Properties();
        for (Map.Entry<Object, Object> entry : legacyToCurrentMap.entrySet()) {
            String legacyPropertyName = (String) entry.getKey();
            Object legacyPropertyValue = getPropertyValue(legacyPropertyName);
            if (legacyPropertyValue == null) {
                continue;
            }
            String currentPropertyName = (String) entry.getValue();
            Object currentPropertyValue = getPropertyValue(currentPropertyName);
            if (currentPropertyValue == null) {
                properties.put(currentPropertyName, legacyPropertyValue);
                LOGGER.warn(toLogString(legacyPropertyName, currentPropertyName));

            }
        }
        return properties;
    }

    /**
     * Get property value from all property sources in the environment.
     * @param propertyName Name of the property to get value.
     * @return Property value.
     */
    protected Object getPropertyValue(String propertyName) {
        return Binder.get(environment)
                     .bind(propertyName, Bindable.of(Object.class))
                     .orElse(null);
    }

    /**
     * Add the mapped current properties to application environment, of which the precedence does not count.
     *
     * @param properties The converted current properties to be configured.
     */
    @Override
    protected void setConvertedPropertyToEnvironment(Properties properties) {
        // This post-processor is called multiple times but sets the properties only once.
        if (!CollectionUtils.isEmpty(properties)) {
            PropertiesPropertySource convertedPropertySource =
                new PropertiesPropertySource(PreLegacyPropertyEnvironmentPostProcessor.class.getName(), properties);
            environment.getPropertySources().addLast(convertedPropertySource);
        }
    }

    public static String toLogString(String legacyPropertyName, String currentPropertyName) {
        return String.format("Deprecated property %s detected! Use %s instead!", legacyPropertyName, currentPropertyName);
    }
}
