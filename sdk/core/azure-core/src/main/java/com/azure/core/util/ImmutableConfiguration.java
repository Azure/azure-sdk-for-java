package com.azure.core.util;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;

import java.util.HashMap;
import java.util.Map;

public class ImmutableConfiguration extends Configuration {

    private final String path;
    private final Map<String, String> configurations;
    private final ImmutableConfiguration fallback;
    private final ClientLogger logger;
    private final boolean isClientConfiguration;

    ImmutableConfiguration(String path, Map<String, String> config, ImmutableConfiguration fallback) {
        this.configurations = new HashMap<>(config);
        this.fallback = fallback;
        this.logger = new ClientLogger(Configuration.class);
        this.path = path;
        this.isClientConfiguration = fallback != null;
    }

    public boolean contains(ConfigurationProperty<?> property) {
        return getWithFallback(property) != null;
    }

    public <T> T get(ConfigurationProperty<T> property) {
        String valueStr = getWithFallback(property);
        if (valueStr == null) {
            return property.getDefaultValue();
        }

        try {
            return property.getConverter().apply(valueStr);
        } catch (Throwable t) {
            throw logger.logThrowableAsError(Exceptions.propagate(t));
        }
    }

    private String getLocalProperty(String name) {
        String absoluteName = path == null ? name : path + "." + name;
        return configurations.get(absoluteName);
    }

    private <T> String getWithFallback(ConfigurationProperty<T> property) {
        String value = getLocalProperty(property.getFullName());
        if (value != null) {
            return value;
        }

        if (!property.isLocal() && fallback != null) {
            value = fallback.getLocalProperty(property.getFullName());
            if (value != null) {
                return value;
            }
        }


        // order:
        // 1. local full property:            appconfig.http-client.application-id
        // 2. local alias:                    appconfig.client.application-id
        // 3. base full property:             http-client.application-id
        // 4. base alias:                     client.application-id
        // 5. env-var fallback (if existed):  AZURE_CLIENT_APPLICATION_ID

        return property.getEnvironmentVariableName() != null ?
                    get(property.getEnvironmentVariableName()) : null;
    }

    // todo to utils:
    public boolean containsAny(ConfigurationProperty<?> property1, ConfigurationProperty<?> property2) {
        return getWithFallback(property1) != null ||
            getWithFallback(property2) != null;
    }

    public boolean containsAny(ConfigurationProperty<?> property1, ConfigurationProperty<?> property2, ConfigurationProperty<?> property3) {
        return getWithFallback(property1) != null ||
            getWithFallback(property2) != null ||
            getWithFallback(property3) != null;
    }
}
