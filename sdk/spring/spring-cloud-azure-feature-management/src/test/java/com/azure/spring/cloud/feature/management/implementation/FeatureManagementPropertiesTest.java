// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class FeatureManagementPropertiesTest {
    
    
    @Test
    public void setFeatureFlagsTest() {
        FeatureManagementProperties properties = new FeatureManagementProperties();
        List<Map<String, Object>> featureFlags = new ArrayList<>();
        properties.setFeatureFlags(featureFlags);
        assertEquals(0, properties.getFeatureFlags().size());
        
        Map<String, Object> alphaFeatureFlag = new HashMap<String, Object>();
        alphaFeatureFlag.put("id", "alpha");
        alphaFeatureFlag.put("enabled", true);
        featureFlags.add(alphaFeatureFlag);
        properties.setFeatureFlags(featureFlags);
        assertEquals(1, properties.getFeatureFlags().size());
        assertEquals("alpha", properties.getFeatureFlags().get(0).getId());
        assertEquals(true, properties.getFeatureFlags().get(0).isEnabled());
    }

}
