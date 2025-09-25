// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.feature.management.models.FeatureDefinition;

public class FeatureManagementPropertiesTest {
    
    
    @Test
    public void setFeatureFlagsTest() {
        FeatureManagementProperties properties = new FeatureManagementProperties();
        List<FeatureDefinition> featureFlags = new ArrayList<>();
        properties.setFeatureFlags(featureFlags);
        assertEquals(0, properties.getFeatureFlags().size());
        
        FeatureDefinition alphaFeatureFlag = new FeatureDefinition();
        alphaFeatureFlag.setId("alpha");
        alphaFeatureFlag.setEnabled(true);
        featureFlags.add(alphaFeatureFlag);
        properties.setFeatureFlags(featureFlags);
        assertEquals(1, properties.getFeatureFlags().size());
        assertEquals("alpha", properties.getFeatureFlags().get(0).getId());
        assertEquals(true, properties.getFeatureFlags().get(0).isEnabled());
    }

}
