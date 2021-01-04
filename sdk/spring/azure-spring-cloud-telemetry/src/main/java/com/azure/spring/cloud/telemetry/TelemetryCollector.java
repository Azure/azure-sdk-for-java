// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.telemetry;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Collect service name and subscription, then return properties could be sent directly.
 *
 * @author Warren Zhu
 */
public final class TelemetryCollector {

    private static final String PROJECT_VERSION = TelemetryCollector.class.getPackage().getImplementationVersion();

    private static final String PROJECT_INFO = "spring-cloud-azure/" + PROJECT_VERSION;

    private static final String VERSION = "version";

    private static final String INSTALLATION_ID = "installationId";

    private static final String SUBSCRIPTION_ID = "subscriptionId";

    private static final String SERVICE_NAME = "serviceName";
    private static final TelemetryCollector INSTANCE = new TelemetryCollector();
    private final String name = "spring-cloud-azure";
    private final Map<String, String> commonProperties = new HashMap<>();
    private final Map<String, Map<String, String>> propertiesByService = new HashMap<>();
    private final Set<String> propertiesNeedHash = new HashSet<>(Arrays.asList("Namespace", "AccountName"));

    private TelemetryCollector() {
        this.buildProperties();
    }

    public static TelemetryCollector getInstance() {
        return INSTANCE;
    }

    public void addService(String service) {
        this.propertiesByService.putIfAbsent(service, new HashMap<>());
        this.propertiesByService.get(service).put(SERVICE_NAME, service);
    }

    public void setSubscription(String subscriptionId) {
        this.commonProperties.put(SUBSCRIPTION_ID, subscriptionId);
    }

    public void addProperty(String service, String key, String value) {
        this.propertiesByService.putIfAbsent(service, new HashMap<>());
        this.propertiesByService.get(service).put(key, value);
        if (propertiesNeedHash.contains(key)) {
            this.propertiesByService.get(service).put("hashed" + key, GetHashMac.hash(value));
        }
    }

    public Collection<Map<String, String>> getProperties() {
        List<Map<String, String>> metrics = new LinkedList<>();

        for (Map<String, String> serviceProperty : this.propertiesByService.values()) {
            Map<String, String> properties = new HashMap<>(this.commonProperties);
            properties.putAll(serviceProperty);
            metrics.add(properties);
        }

        return metrics;
    }

    private void buildProperties() {
        commonProperties.put(VERSION, PROJECT_INFO);
        commonProperties.put(INSTALLATION_ID, GetHashMac.getHashMac());
    }

    public String getName() {
        return this.name;
    }
}
