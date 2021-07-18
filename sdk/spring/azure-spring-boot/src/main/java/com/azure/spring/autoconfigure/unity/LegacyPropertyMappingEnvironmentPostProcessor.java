// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.unity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

/**
 * Map legacy property to current spring properties.
 */
public class LegacyPropertyMappingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER + 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyPropertyMappingEnvironmentPostProcessor.class);

    private int order = DEFAULT_ORDER;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        JSONObject legacyToCurrentMap = loadPropertyMapFromClassPath();
        if (null == legacyToCurrentMap) {
            return;
        }

        Properties properties = mapLegacyPropertyToCurrent(legacyToCurrentMap, environment);

        // This post-processor is called multiple times but sets the properties only once.
        if (!CollectionUtils.isEmpty(properties)) {
            PropertiesPropertySource propertiesPropertySource =
                new PropertiesPropertySource(LegacyPropertyMappingEnvironmentPostProcessor.class.getName(), properties);
            environment.getPropertySources().addLast(propertiesPropertySource);
        }
    }

    private JSONObject loadPropertyMapFromClassPath() {

        InputStream inputStream = LegacyPropertyMappingEnvironmentPostProcessor.class.getClassLoader()
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

    private Properties mapLegacyPropertyToCurrent(JSONObject legacyToCurrentMap, ConfigurableEnvironment environment) {
        Properties properties = new Properties();
        Iterator iterator = legacyToCurrentMap.keys();
        while (iterator.hasNext()) {
            String legacyPropertyName = (String) iterator.next();
            Object legacyPropertyValue = Binder.get(environment)
                                               .bind(legacyPropertyName, Bindable.of(Object.class))
                                               .orElse(null);
            if (null != legacyPropertyValue) {
                try {
                    String currentPropertyName = legacyToCurrentMap.getString(legacyPropertyName);
                    Object currentPropertyValue = Binder.get(environment)
                                                        .bind(currentPropertyName, Bindable.of(Object.class))
                                                        .orElse(null);
                    if (null == currentPropertyValue) {
                        properties.put(currentPropertyName, legacyPropertyValue);
                        LOGGER.warn("Deprecated property {} detected! Use {} instead!", legacyPropertyName,
                            currentPropertyName);
                    }
                } catch (JSONException e) {
                    LOGGER.error("Error while loading current property name mapping to {}", legacyPropertyName);
                }
            }
        }
        return properties;
    }
}
