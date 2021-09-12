// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.context;

import com.azure.spring.keyvault.KeyVaultEnvironmentPostProcessor;
import com.azure.spring.keyvault.KeyVaultProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.azure.spring.utils.PropertyLoader.loadPropertiesFromClassPath;

/**
 * Convert legacy properties to the current and set into environment before {@link KeyVaultEnvironmentPostProcessor}.
 */
public class PreLegacyPropertyEnvironmentPostProcessor extends AbstractLegacyPropertyEnvironmentPostProcessor {

    public static final int DEFAULT_ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(PreLegacyPropertyEnvironmentPostProcessor.class);
    private static final String KEYVAULT_LEGACY_PREFIX = "azure.keyvault";
    public static final String DELIMITER = ".";

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Override
    protected Properties buildLegacyToCurrentPropertyMap(ConfigurableEnvironment environment) {
        Properties legacyToCurrentMap = super.buildLegacyToCurrentPropertyMap(environment);
        String[] keyVaultNames = getMultipleKeyVaultNames(environment);
        if (keyVaultNames != null) {
            // Load the mapping relationship of Key Vault legacy properties and associated current properties from
            // classpath, this is used for handling multiple key vault cases.
            Properties keyVaultPropertySuffixMap = loadPropertiesFromClassPath("legacy-keyvault-property-suffix-mapping.properties");
            addMultipleKVPropertyToMap(keyVaultNames, keyVaultPropertySuffixMap, legacyToCurrentMap);
        }
        return legacyToCurrentMap;
    }

    /**
     * Load all possible Key Vault names from property "spring.cloud.azure.keyvault.order" if existed. Otherwise load
     * from legacy property "azure.keyvault.order".
     *
     * @param environment The application environment to load property from.
     * @return A string with all Key Vaults names concatenated by commas, or null if no Key Vault names specified.
     */
    private String[] getMultipleKeyVaultNames(ConfigurableEnvironment environment) {
        String[] kvNames = null;
        List<String> kvOrderPropertyNames = Arrays.asList(KeyVaultProperties.PREFIX + ".order",
            KEYVAULT_LEGACY_PREFIX + ".order");
        for (String kvOrderPropertyName : kvOrderPropertyNames) {
            String kvNamesString = Binder.get(environment)
                                         .bind(kvOrderPropertyName, Bindable.of(String.class))
                                         .orElse(null);
            if (StringUtils.hasText(kvNamesString)) {
                kvNames = Arrays.stream(kvNamesString.split(","))
                                .map(String::trim)
                                .toArray(size -> new String[size]);
                break;
            }
        }

        return kvNames;
    }

    /**
     * Build a legacy Key Vault property map of multiple key vault use case. When multiple Key Vaults are used, Key
     * Vault property names are not fixed and varies across user definition.
     *
     * @param keyVaultNames A string array contains all Key Vaults names.
     * @param keyVaultPropertySuffixMap A {@link Properties} contains a map of Key Vault property suffixes from legacy
     *                                  to current.
     * @param legacyToCurrentMap A {@link Properties} contains a map of all legacy properties and associated current ones.
     */
    protected void addMultipleKVPropertyToMap(String[] keyVaultNames, Properties keyVaultPropertySuffixMap,
                                              Properties legacyToCurrentMap) {
        Arrays.stream(keyVaultNames).forEach(keyVault -> {
            for (Map.Entry<Object, Object> mapping : keyVaultPropertySuffixMap.entrySet()) {
                String legacy = buildPropertyName(KEYVAULT_LEGACY_PREFIX, keyVault, (String) mapping.getKey());
                String current = buildPropertyName(KeyVaultProperties.PREFIX, keyVault, (String) mapping.getValue());
                legacyToCurrentMap.put(legacy, current);
            }
        });
    }

    private String buildPropertyName(String propertyPrefix, String propertyInfix, String propertySuffix) {
        return String.join(DELIMITER, propertyPrefix, propertyInfix, propertySuffix);
    }

    /**
     * When only legacy properties are detected from all property sources, convert legacy properties to the current,
     * and create a new {@link Properties} to store all of the converted current properties.
     *
     * @param environment The application environment to load property from.
     * @param legacyToCurrentMap A {@link Properties} contains a map of all legacy properties and associated current properties.
     * @return A {@link Properties} to store mapped current properties
     */
    @Override
    protected Properties convertLegacyToCurrent(ConfigurableEnvironment environment, Properties legacyToCurrentMap) {
        Properties properties = new Properties();
        for (Map.Entry<Object, Object> entry : legacyToCurrentMap.entrySet()) {
            String legacyPropertyName = (String) entry.getKey();
            BindResult<Object> legacyPropertyValue = Binder.get(environment)
                                                           .bind(legacyPropertyName, Bindable.of(Object.class));
            if (!legacyPropertyValue.isBound()) {
                continue;
            }
            String currentPropertyName = (String) entry.getValue();
            BindResult<Object> currentPropertyValue = Binder.get(environment)
                                                            .bind(currentPropertyName, Bindable.of(Object.class));
            if (!currentPropertyValue.isBound()) {
                properties.put(currentPropertyName, legacyPropertyValue.get());
                LOGGER.warn(toLogString(legacyPropertyName, currentPropertyName));
            }
        }
        return properties;
    }

    /**
     * Add the mapped current properties to application environment, of which the precedence does not count.
     *
     * @param environment The application environment to load property from.
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

    public static String toLogString(String legacyPropertyName, String currentPropertyName) {
        return String.format("Deprecated property %s detected! Use %s instead!", legacyPropertyName, currentPropertyName);
    }
}
