// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;

public class ServerSideFeatureManagementPropertiesListTest {

    private FeatureManagementProperties serverSideProperties;

    @Test
    void featureManagementTest() {
        serverSideProperties = new FeatureManagementProperties();
        Map<String, List<Object>> test = new HashMap<>();
        Map<String, Object> alpha = Map.of("id", "Alpha", "enabled", true);
        
        Map<String, Object> randomFilter = Map.of("name", "Microsoft.Random", "parameters", Map.of("Value", 50));
        Map<String, Object> beta = Map.of("id", "Beta", "enabled", true, "conditions",
            Map.of("client_filters", Map.of("0", randomFilter)));

        test.put("feature_flags", List.of(alpha, beta));
        serverSideProperties.putAll(test);
        assertEquals(1, serverSideProperties.getOnOff().size());
        assertEquals(1, serverSideProperties.getFeatureManagement().size());
    }

}
