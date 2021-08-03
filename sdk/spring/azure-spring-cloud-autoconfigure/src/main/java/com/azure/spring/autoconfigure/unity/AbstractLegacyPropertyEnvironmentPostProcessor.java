// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.unity;

import com.azure.spring.keyvault.KeyVaultEnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

import static com.azure.spring.utils.PropertyLoader.loadPropertiesFromClassPath;

/**
 * Abstract class to convert legacy properties to the current when only legacy properties are configured,
 * need to be executed before and after {@link KeyVaultEnvironmentPostProcessor}
 * if {@link KeyVaultEnvironmentPostProcessor} is enabled.
 */
public abstract class AbstractLegacyPropertyEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static Properties springPropertyMap;
    static {
        // Load the map of each service's legacy properties and associated current properties from classpath.
        springPropertyMap = loadPropertiesFromClassPath("legacy-property-mapping.properties");
    }

    @Override
    public abstract int getOrder();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Properties legacyToCurrentMap = buildLegacyToCurrentPropertyMap(environment);
        Properties convertedProperties = convertLegacyToCurrent(environment, legacyToCurrentMap);
        setConvertedPropertyToEnvironment(environment, convertedProperties);
    }

    protected Properties buildLegacyToCurrentPropertyMap(ConfigurableEnvironment environment) {
        Properties legacyToCurrentMap = new Properties();
        legacyToCurrentMap.putAll(springPropertyMap);
        return legacyToCurrentMap;
    }

    /**
     * Convert legacy properties to the current and create new {@link Properties} to store mapped current properties
     * if only legacy properties are configured.
     *
     * @param environment The application environment to load property from.
     * @param legacyToCurrentMap A {@link Properties} contains a map of all legacy properties and associated current ones.
     * @return A {@link Properties} to store mapped current properties
     */
    protected abstract Properties convertLegacyToCurrent(ConfigurableEnvironment environment, Properties legacyToCurrentMap);

    /**
     * Add the mapped current properties to application environment,
     * of which the precedence varies in different processors.
     *
     * @param environment The application environment to load property from.
     * @param properties The converted current properties to be configured.
     */
    protected abstract void setConvertedPropertyToEnvironment(ConfigurableEnvironment environment, Properties properties);
}
