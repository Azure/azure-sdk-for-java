package com.azure.identity.util;

import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * A {@link ConfigurationSource} that contains empty values for AZURE_* environment variables.
 */
public class EmptyEnvironmentConfigurationSource implements ConfigurationSource {
    final private Map<String, String> testData;

    public EmptyEnvironmentConfigurationSource() {
        this.testData = new HashMap<>();
        testData.put(Configuration.PROPERTY_AZURE_CLIENT_ID, null);
        testData.put(Configuration.PROPERTY_AZURE_USERNAME, null);
        testData.put(Configuration.PROPERTY_AZURE_PASSWORD, null);
    }

    @Override
    public Map<String, String> getProperties(String path) {
        if (path == null) {
            return testData;
        }
        return testData.entrySet().stream()
            .filter(prop -> prop.getKey().startsWith(path + "."))
            .collect(Collectors.toMap(Map.Entry<String, String>::getKey, Map.Entry<String, String>::getValue));
    }
}
