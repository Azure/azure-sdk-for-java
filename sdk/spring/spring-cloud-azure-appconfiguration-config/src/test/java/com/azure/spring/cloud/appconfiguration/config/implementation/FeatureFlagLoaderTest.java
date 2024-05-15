// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_ROLLOUT_PERCENTAGE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.GROUPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.USERS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;

public class FeatureFlagLoaderTest {

    @Mock
    private AppConfigurationReplicaClient clientMock;

    private FeatureFlagClient featureFlagLoader;

    private String[] emptyLabelList = { "\0" };

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);

        featureFlagLoader = new FeatureFlagClient();
    }

    @Test
    public void loadFeatureFlagsTestNoFeatureFlags() {
        List<ConfigurationSetting> settings = List.of(new ConfigurationSetting().setKey("FakeKey"));
        FeatureFlags featureFlags = new FeatureFlags(null, settings);
        when(clientMock.listFeatureFlags(Mockito.any())).thenReturn(featureFlags);

        List<FeatureFlags> featureFlagsList = featureFlagLoader.loadFeatureFlags(clientMock, null, emptyLabelList);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals("FakeKey", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(0, featureFlagLoader.getProperties().size());
    }

    @Test
    public void loadFeatureFlagsTestFeatureFlags() {
        List<ConfigurationSetting> settings = List.of(new FeatureFlagConfigurationSetting("Alpha", false),
            new FeatureFlagConfigurationSetting("Beta", true));
        FeatureFlags featureFlags = new FeatureFlags(null, settings);
        when(clientMock.listFeatureFlags(Mockito.any())).thenReturn(featureFlags);

        List<FeatureFlags> featureFlagsList = featureFlagLoader.loadFeatureFlags(clientMock, null, emptyLabelList);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals(".appconfig.featureflag/Alpha", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(".appconfig.featureflag/Beta", featureFlagsList.get(0).getFeatureFlags().get(1).getKey());
        assertEquals(2, featureFlagLoader.getProperties().size());
    }
    
    @Test
    public void loadFeatureFlagsTestMultipleLoads() {
        List<ConfigurationSetting> settings = List.of(new FeatureFlagConfigurationSetting("Alpha", false),
            new FeatureFlagConfigurationSetting("Beta", true));
        FeatureFlags featureFlags = new FeatureFlags(null, settings);
        when(clientMock.listFeatureFlags(Mockito.any())).thenReturn(featureFlags);

        List<FeatureFlags> featureFlagsList = featureFlagLoader.loadFeatureFlags(clientMock, null, emptyLabelList);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals(".appconfig.featureflag/Alpha", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(".appconfig.featureflag/Beta", featureFlagsList.get(0).getFeatureFlags().get(1).getKey());
        assertEquals(2, featureFlagLoader.getProperties().size());
        
        List<ConfigurationSetting> settings2 = List.of(new FeatureFlagConfigurationSetting("Alpha", true),
            new FeatureFlagConfigurationSetting("Gamma", false));
        featureFlags = new FeatureFlags(null, settings2);
        when(clientMock.listFeatureFlags(Mockito.any())).thenReturn(featureFlags);

        featureFlagsList = featureFlagLoader.loadFeatureFlags(clientMock, null, emptyLabelList);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals(".appconfig.featureflag/Alpha", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(".appconfig.featureflag/Gamma", featureFlagsList.get(0).getFeatureFlags().get(1).getKey());
        assertEquals(3, featureFlagLoader.getProperties().size());
        Map<String, Feature> features = featureFlagLoader.getProperties();
        assertTrue(features.get(".appconfig.featureflag/Alpha").isEnabled());
        assertTrue(features.get(".appconfig.featureflag/Beta").isEnabled());
        assertFalse(features.get(".appconfig.featureflag/Gamma").isEnabled());
    }
    
    @Test
    public void loadFeatureFlagsTestTargetingFilter() {
        FeatureFlagConfigurationSetting targetingFlag = new FeatureFlagConfigurationSetting("TargetingTest", false);
        FeatureFlagFilter targetingFilter = new FeatureFlagFilter("Microsoft.Targeting");
        
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();

        LinkedHashMap<String, String> users = new LinkedHashMap<>();
        users.put("0", "Jeff");
        users.put("1", "Alicia");

        LinkedHashMap<String, Object> groups = new LinkedHashMap<>();
        LinkedHashMap<String, String> ring0 = new LinkedHashMap<>();
        LinkedHashMap<String, String> ring1 = new LinkedHashMap<>();

        ring0.put("name", "Ring0");
        ring0.put("rolloutPercentage", "100");

        ring1.put("name", "Ring1");
        ring1.put("rolloutPercentage", "100");

        groups.put("0", ring0);
        groups.put("1", ring1);

        parameters.put(USERS, users);
        parameters.put(GROUPS, groups);
        parameters.put(DEFAULT_ROLLOUT_PERCENTAGE, 50);
        
        targetingFilter.addParameter("Audience", parameters);
        targetingFlag.addClientFilter(targetingFilter);
        List<ConfigurationSetting> settings = List.of(targetingFlag);
        FeatureFlags featureFlags = new FeatureFlags(null, settings);
        when(clientMock.listFeatureFlags(Mockito.any())).thenReturn(featureFlags);

        List<FeatureFlags> featureFlagsList = featureFlagLoader.loadFeatureFlags(clientMock, null, emptyLabelList);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals(".appconfig.featureflag/TargetingTest", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(1, featureFlagLoader.getProperties().size());
    }

}
