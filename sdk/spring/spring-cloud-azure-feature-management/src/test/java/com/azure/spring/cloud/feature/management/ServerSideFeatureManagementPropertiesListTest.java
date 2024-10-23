// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;

public class ServerSideFeatureManagementPropertiesListTest {

    private FeatureManagementProperties serverSideProperties;

    @Test
    void featureManagementTest() {
        serverSideProperties = new FeatureManagementProperties();
        Map<String, List<Object>> test = new LinkedHashMap<>();
        Map<String, Object> alpha = new LinkedHashMap<>();
        alpha.put("id", "Alpha");
        alpha.put("enabled", true);

        Map<String, Object> randomFilter = new LinkedHashMap<>();
        randomFilter.put("name", "Microsoft.Random");
        randomFilter.put("parameters", Map.of("Value", 50));

        Map<String, Object> clientFilters = new LinkedHashMap<>();
        clientFilters.put("0", randomFilter);
        Map<String, Object> conditions = new LinkedHashMap<>();
        conditions.put("client_filters", clientFilters);
        Map<String, Object> beta = new LinkedHashMap<>();
        beta.put("id", "Beta");
        beta.put("enabled", true);
        beta.put("conditions", conditions);

        test.put("feature_flags", List.of(alpha, beta));
        serverSideProperties.putAll(test);
        assertEquals(1, serverSideProperties.getOnOff().size());
        assertEquals(1, serverSideProperties.getFeatureManagement().size());
    }

}
