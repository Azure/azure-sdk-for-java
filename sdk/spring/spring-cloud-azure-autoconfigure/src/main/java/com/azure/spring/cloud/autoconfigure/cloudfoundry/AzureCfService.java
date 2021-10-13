// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.cloudfoundry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

enum AzureCfService {

    SERVICEBUS("servicebus", "azure-servicebus", getImmutableMap("connectionString", "connection-string")),
    EVENTHUB("eventhub", "azure-eventhubs", getImmutableMap("connectionString", "connection-string")),
    STORAGE("storage", "azure-storage", getImmutableMap("storageAccountName", "account", "accessKey", "access-key")),
    STORAGE_EVENTHUB("eventhub", "azure-storage",
        getImmutableMap("storageAccountName", "checkpoint-storage-account", "accessKey", "checkpoint-access-key")),
    REDIS("spring.redis", "azure-rediscache", getImmutableMap("host", "host", "password", "password", "port", "port"),
        false);

    private static final String SPRING_CLOUD_AZURE_PROPERTY_PREFIX = "spring.cloud.azure.";

    /**
     * Name of the Azure Cloud Foundry service in the VCAP_SERVICES JSON.
     */
    private final String cfServiceName;

    /**
     * Name of the Spring Cloud Azure property.
     */
    private final String azureServiceName;

    /**
     * Direct mapping of Azure service broker field names in VCAP_SERVICES JSON to Spring Cloud
     * Azure property names.
     */
    private final Map<String, String> cfToAzureProperties;

    private final boolean isAzureProperty;

    AzureCfService(String azureServiceName, String cfServiceName, Map<String, String> cfToAzureProperties) {
        this(azureServiceName, cfServiceName, cfToAzureProperties, true);
    }

    AzureCfService(String azureServiceName, String cfServiceName, Map<String, String> cfToAzureProperties,
                   boolean isAzureProperty) {
        this.cfServiceName = cfServiceName;
        this.azureServiceName = azureServiceName;
        this.isAzureProperty = isAzureProperty;
        this.cfToAzureProperties = buildCfToAzureProperties(cfToAzureProperties);
    }

    public String getCfServiceName() {
        return this.cfServiceName;
    }

    public Map<String, String> getCfToAzureProperties() {
        return this.cfToAzureProperties;
    }

    public String getAzureServiceName() {
        return this.azureServiceName;
    }

    private Map<String, String> buildCfToAzureProperties(Map<String, String> cfToAzureProperties) {
        return cfToAzureProperties.entrySet().stream().collect(
            Collectors.toMap(Map.Entry::getKey, e -> getPropertyPrefix() + e.getValue()));
    }

    private String getPropertyPrefix() {
        if (this.isAzureProperty) {
            return SPRING_CLOUD_AZURE_PROPERTY_PREFIX + this.azureServiceName + ".";
        }

        return this.azureServiceName + ".";
    }

    private static Map<String, String> getImmutableMap(String... values) {
        int pairs = values.length / 2;
        Map<String, String> output = new HashMap<>(pairs);

        for (int i = 0; i < pairs; i++) {
            String key = values[2 * i];
            String value = values[(2 * i) + 1];
            output.put(key, value);
        }

        return Collections.unmodifiableMap(output);
    }
}
