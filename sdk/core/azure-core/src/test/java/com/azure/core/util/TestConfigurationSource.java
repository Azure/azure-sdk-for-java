package com.azure.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TestConfigurationSource implements ConfigurationSource {
    private Map<String, String> testData;

    public TestConfigurationSource(String... testData) {
        this.testData = new HashMap<>();
        for (int i = 0; i < testData.length; i +=2) {
            this.testData.put(testData[i], testData[i + 1]);
        }
    }

    @Override
    public Iterable<String> getValues(String prefix) {
        if (prefix == null) {
            return testData.keySet();
        }
        return testData.keySet().stream().filter(k -> k.startsWith(prefix + ".")).collect(Collectors.toList());
    }

    @Override
    public String getValue(String propertyName) {
        return testData.get(propertyName);
    }
}
