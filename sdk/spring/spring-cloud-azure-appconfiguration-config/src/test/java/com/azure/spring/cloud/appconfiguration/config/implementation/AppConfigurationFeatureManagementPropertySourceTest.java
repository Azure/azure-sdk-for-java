// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;

public class AppConfigurationFeatureManagementPropertySourceTest {

    @Mock
    private FeatureFlagClient featureFlagLoaderMock;
    
    private static final String FEATURE_FLAG_KEY = "feature_management.feature_flags";
    
    private MockitoSession session;

    @BeforeEach
    public void init() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void getPropertyNamesTest() {
        AppConfigurationFeatureManagementPropertySource featureManagementPropertySource = new AppConfigurationFeatureManagementPropertySource(
            featureFlagLoaderMock);
        
        String[] names = featureManagementPropertySource.getPropertyNames();
        assertTrue(names.length == 1);
        assertEquals(FEATURE_FLAG_KEY, names[0]);
    }
    
    @Test
    public void getPropertyTest() {
        AppConfigurationFeatureManagementPropertySource featureManagementPropertySource = new AppConfigurationFeatureManagementPropertySource(
            featureFlagLoaderMock);
        
        assertNull(featureManagementPropertySource.getProperty("NotFeatureFlagProperty"));
        when(featureFlagLoaderMock.getProperties()).thenReturn(new HashMap<String, Feature>());
        assertNotNull(featureManagementPropertySource.getProperty(FEATURE_FLAG_KEY));
        
    }

}
