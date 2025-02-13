// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.implemenation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.azure.spring.cloud.feature.management.implementation.FeatureFilterUtils;

public class TargetingFilterUtilsTest {
    
    @Test
    public void isTargetedPercentageTest() {        
        assertEquals(FeatureFilterUtils.isTargetedPercentage(null), 9.875071074318855);        
        assertEquals(FeatureFilterUtils.isTargetedPercentage(""), 26.0813765987012);        
        assertEquals(FeatureFilterUtils.isTargetedPercentage("Alice"), 38.306839656621875);        
        assertEquals(FeatureFilterUtils.isTargetedPercentage("Quinn\nDeb"), 79.98622464481421);        
        assertEquals(FeatureFilterUtils.isTargetedPercentage("\nProd"), 73.47059517015484);
    }

}
