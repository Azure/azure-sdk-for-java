// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.unity;

import com.azure.spring.keyvault.KeyVaultEnvironmentPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Abstract class to convert legacy properties to the current when only legacy properties are configured,
 * need to be executed before and after {@link KeyVaultEnvironmentPostProcessor}
 * if {@link KeyVaultEnvironmentPostProcessor} is enabled.
 */
public abstract class AbstractLegacyPropertyEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLegacyPropertyEnvironmentPostProcessor.class);

    public abstract int getOrder();

    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        JSONObject legacyToCurrentMap = loadPropertyMapFromClassPath();
        if (null == legacyToCurrentMap) {
            return;
        }
        Properties properties = mapLegacyPropertyToCurrent(legacyToCurrentMap, environment);
        setConvertedPropertyToEnvironment(environment, properties);
    }

    /**
     * Load the mapping relationship of each legacy properties and the associated current properties from class path.
     *
     * @return A {@JSONObject} contains a map of all legacy properties and associated current properties.
     */
    private JSONObject loadPropertyMapFromClassPath() {

        InputStream inputStream = AbstractLegacyPropertyEnvironmentPostProcessor.class
                                      .getClassLoader()
                                      .getResourceAsStream("legacy-property-mapping.json");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder content = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
            return new JSONObject(content.toString());
        } catch (IOException exception) {
            LOGGER.error("Error while loading legacy-property-mapping.json", exception);
            return null;
        } catch (JSONException exception) {
            LOGGER.error("Error while parsing legacy-property-mapping.json to JSONObject", exception);
            return null;
        }
    }

    /**
     * Convert legacy properties to the current and create new {@link Properties} to store mapped current properties
     * if only legacy properties are configured.
     *
     * @param legacyToCurrentMap A {@JSONObject} contains a map of all legacy properties and associated current ones.
     * @param environment The application environment to get and set properties.
     * @return A {@link Properties} to store mapped current properties
     */
    protected abstract Properties mapLegacyPropertyToCurrent(JSONObject legacyToCurrentMap,
                                                             ConfigurableEnvironment environment);

    /**
     * Add the mapped current properties to application environment,
     * of which the precedence varies in different processors.
     *
     * @param environment The application environment to set properties.
     * @param properties
     */
    protected abstract void setConvertedPropertyToEnvironment(ConfigurableEnvironment environment, Properties properties);
}
