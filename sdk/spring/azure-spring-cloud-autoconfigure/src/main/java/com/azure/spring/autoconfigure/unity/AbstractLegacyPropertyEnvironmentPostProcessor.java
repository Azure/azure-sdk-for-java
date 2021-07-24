// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.unity;

import com.azure.spring.keyvault.KeyVaultEnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Abstract class to convert legacy properties to the current when only legacy properties are configured,
 * need to be executed before and after {@link KeyVaultEnvironmentPostProcessor}
 * if {@link KeyVaultEnvironmentPostProcessor} is enabled.
 */
public abstract class AbstractLegacyPropertyEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Map<String, String> SPRING_PROPERTY_MAP = new HashMap<String, String>();
    static {
        // Load the map of each service's legacy properties and associated current properties from classpath.
        try (
                InputStream inputStream = AbstractLegacyPropertyEnvironmentPostProcessor.class
                                              .getClassLoader()
                                              .getResourceAsStream("legacy-property-mapping.properties");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))
        ) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                SPRING_PROPERTY_MAP.put(line.split("=")[0], line.split("=")[1]);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Fail to load legacy-property-mapping.properties", exception);
        }
    }

    public abstract int getOrder();

    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        Map<String, String> legacyToCurrentMap = new HashMap<String, String>(SPRING_PROPERTY_MAP);
        Optional.ofNullable(getMultipleKeyVaultsPropertyMap(environment))
                .ifPresent(legacyToCurrentMap::putAll);
        Properties properties = mapLegacyPropertyToCurrent(legacyToCurrentMap, environment);
        setConvertedPropertyToEnvironment(environment, properties);
    }

    /**
     * Build a legacy Key Vault property map of multiple key vault use case. When multiple Key Vaults are used, Key
     * Vault property names are not fixed and varies across user definition.
     *
     * @param environment The application environment to load property from.
     * @return A map contains all possbile Key Vault properties.
     */
    protected abstract Map<String, String> getMultipleKeyVaultsPropertyMap(ConfigurableEnvironment environment);

    /**
     * Convert legacy properties to the current and create new {@link Properties} to store mapped current properties
     * if only legacy properties are configured.
     *
     * @param legacyToCurrentMap A map contains a map of all legacy properties and associated current ones.
     * @param environment The application environment to get and set properties.
     * @return A {@link Properties} to store mapped current properties
     */
    protected abstract Properties mapLegacyPropertyToCurrent(Map<String, String> legacyToCurrentMap,
                                                             ConfigurableEnvironment environment);

    /**
     * Add the mapped current properties to application environment,
     * of which the precedence varies in different processors.
     *
     * @param environment The application environment to set properties.
     * @param properties The converted current properties to be configured.
     */
    protected abstract void setConvertedPropertyToEnvironment(ConfigurableEnvironment environment, Properties properties);
}
