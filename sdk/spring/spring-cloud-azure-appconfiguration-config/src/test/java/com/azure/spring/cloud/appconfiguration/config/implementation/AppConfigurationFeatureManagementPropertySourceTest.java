package com.azure.spring.cloud.appconfiguration.config.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;

public class AppConfigurationFeatureManagementPropertySourceTest {

    @Mock
    private FeatureFlagLoader featureFlagLoaderMock;
    
    private static final String FEATURE_FLAG_KEY = "feature_management.feature_flags";

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
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
