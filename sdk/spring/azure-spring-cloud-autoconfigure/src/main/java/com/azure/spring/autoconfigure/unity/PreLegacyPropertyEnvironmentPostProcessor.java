package com.azure.spring.autoconfigure.unity;

import com.azure.spring.keyvault.KeyVaultEnvironmentPostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.Properties;

/**
 * Convert legacy properties to the current and set into environment before {@link KeyVaultEnvironmentPostProcessor}.
 */
public class PreLegacyPropertyEnvironmentPostProcessor extends AbstractLegacyPropertyEnvironmentPostProcessor{

    public static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER + 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(PreLegacyPropertyEnvironmentPostProcessor.class);

    private int order = DEFAULT_ORDER;

    @Override
    public int getOrder() {
        return order;
    }

    /**
     * When only legacy properties are detected from all property sources, convert legacy properties to the current,
     * and create a new {@link Properties} to store all of the converted current properties.
     * @param legacyToCurrentMap A {@JSONObject} contains a map of all legacy properties and associated current properties.
     * @param environment The application environment to get and set properties.
     * @return A {@link Properties} to store mapped current properties
     */
    @Override
    protected Properties mapLegacyPropertyToCurrent(JSONObject legacyToCurrentMap, ConfigurableEnvironment environment) {
        Properties properties = new Properties();
        Iterator iterator = legacyToCurrentMap.keys();
        while (iterator.hasNext()) {
            String legacyPropertyName = (String) iterator.next();
            Object legacyPropertyValue = getPropertyValue(legacyPropertyName, environment);
            if (null != legacyPropertyValue) {
                try {
                    String currentPropertyName = legacyToCurrentMap.getString(legacyPropertyName);
                    Object currentPropertyValue = getPropertyValue(currentPropertyName, environment);;
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

    /**
     * Get property value from all property sources in the environment.
     * @param propertyName Name of the property to get value.
     * @param environment Environment to get value from.
     * @return Property value.
     */
    private Object getPropertyValue(String propertyName, ConfigurableEnvironment environment) {
        return Binder.get(environment)
                     .bind(propertyName, Bindable.of(Object.class))
                     .orElse(null);
    }

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
