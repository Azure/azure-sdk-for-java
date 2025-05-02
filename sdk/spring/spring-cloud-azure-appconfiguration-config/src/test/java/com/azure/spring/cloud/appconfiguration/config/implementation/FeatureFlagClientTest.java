// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.E_TAG;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_REFERENCE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.DEFAULT_ROLLOUT_PERCENTAGE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_LABEL;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_VALUE_ALL;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_VALUE_TELEMETRY;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.GROUPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_E_TAG;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.USERS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestUtils.createItemFeatureFlag;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import com.azure.core.util.Context;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.FeatureFlags;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;

public class FeatureFlagClientTest {

    @Mock
    private AppConfigurationReplicaClient clientMock;
    
    @Mock
    private Context contextMock;

    private FeatureFlagClient featureFlagClient;

    private String[] emptyLabelList = { "\0" };
    
    private static final FeatureFlagConfigurationSetting TELEMETRY_FEATURE = createItemFeatureFlag(
        ".appconfig.featureflag/", "Delta",
        FEATURE_VALUE_TELEMETRY, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);
    
    private static final FeatureFlagConfigurationSetting ALL_FEATURE = createItemFeatureFlag(
        ".appconfig.featureflag/", "Delta",
        FEATURE_VALUE_ALL, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

    private MockitoSession session;

    @BeforeEach
    public void init() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MockitoAnnotations.openMocks(this);

        featureFlagClient = new FeatureFlagClient();
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void loadFeatureFlagsTestNoFeatureFlags() {
        List<ConfigurationSetting> settings = List.of(new ConfigurationSetting().setKey("FakeKey"));
        FeatureFlags featureFlags = new FeatureFlags(null, settings);
        when(clientMock.listFeatureFlags(Mockito.any(), Mockito.any(Context.class))).thenReturn(featureFlags);

        List<FeatureFlags> featureFlagsList = featureFlagClient.loadFeatureFlags(clientMock, null, emptyLabelList, contextMock);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals("FakeKey", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(0, featureFlagClient.getFeatureFlags().size());
    }

    @Test
    public void loadFeatureFlagsTestFeatureFlags() {
        List<ConfigurationSetting> settings = List.of(new FeatureFlagConfigurationSetting("Alpha", false),
            new FeatureFlagConfigurationSetting("Beta", true));
        FeatureFlags featureFlags = new FeatureFlags(null, settings);
        when(clientMock.listFeatureFlags(Mockito.any(), Mockito.any(Context.class))).thenReturn(featureFlags);

        List<FeatureFlags> featureFlagsList = featureFlagClient.loadFeatureFlags(clientMock, null, emptyLabelList, contextMock);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals(".appconfig.featureflag/Alpha", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(".appconfig.featureflag/Beta", featureFlagsList.get(0).getFeatureFlags().get(1).getKey());
        assertEquals(2, featureFlagClient.getFeatureFlags().size());
    }

    @Test
    public void loadFeatureFlagsTestMultipleLoads() {
        List<ConfigurationSetting> settings = List.of(new FeatureFlagConfigurationSetting("Alpha", false),
            new FeatureFlagConfigurationSetting("Beta", true));
        FeatureFlags featureFlags = new FeatureFlags(null, settings);
        when(clientMock.listFeatureFlags(Mockito.any(), Mockito.any(Context.class))).thenReturn(featureFlags);

        List<FeatureFlags> featureFlagsList = featureFlagClient.loadFeatureFlags(clientMock, null, emptyLabelList, contextMock);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals(".appconfig.featureflag/Alpha", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(".appconfig.featureflag/Beta", featureFlagsList.get(0).getFeatureFlags().get(1).getKey());
        assertEquals(2, featureFlagClient.getFeatureFlags().size());

        List<ConfigurationSetting> settings2 = List.of(new FeatureFlagConfigurationSetting("Alpha", true),
            new FeatureFlagConfigurationSetting("Gamma", false));
        featureFlags = new FeatureFlags(null, settings2);
        when(clientMock.listFeatureFlags(Mockito.any(), Mockito.any(Context.class))).thenReturn(featureFlags);

        featureFlagsList = featureFlagClient.loadFeatureFlags(clientMock, null, emptyLabelList, contextMock);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals(".appconfig.featureflag/Alpha", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(".appconfig.featureflag/Gamma", featureFlagsList.get(0).getFeatureFlags().get(1).getKey());
        assertEquals(3, featureFlagClient.getFeatureFlags().size());
        List<Feature> features = featureFlagClient.getFeatureFlags();
        assertTrue(features.get(0).isEnabled());
        assertTrue(features.get(1).isEnabled());
        assertFalse(features.get(2).isEnabled());
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
        when(clientMock.listFeatureFlags(Mockito.any(), Mockito.any(Context.class))).thenReturn(featureFlags);

        List<FeatureFlags> featureFlagsList = featureFlagClient.loadFeatureFlags(clientMock, null, emptyLabelList, contextMock);
        assertEquals(1, featureFlagsList.size());
        assertEquals(featureFlags, featureFlagsList.get(0));
        assertEquals(".appconfig.featureflag/TargetingTest", featureFlagsList.get(0).getFeatureFlags().get(0).getKey());
        assertEquals(1, featureFlagClient.getFeatureFlags().size());
    }
    
    @Test
    public void testAndRequirementType() {
        Feature feature = FeatureFlagClient.createFeature(ALL_FEATURE, TEST_ENDPOINT);
        assertEquals("All", feature.getConditions().getRequirementType());
    }

    @Test
    public void testFeatureFlagTelemetry() {
        Feature feature = FeatureFlagClient.createFeature(TELEMETRY_FEATURE, TEST_ENDPOINT);
        String featureFlagReference = String.format("%s/kv/%s", TEST_ENDPOINT, ".appconfig.featureflag/Delta");

        assertEquals(featureFlagReference, feature.getTelemetry().getMetadata().get(FEATURE_FLAG_REFERENCE));
        assertEquals(TEST_E_TAG, feature.getTelemetry().getMetadata().get(E_TAG));
    }

    @Test
    public void testAllocationIdInTelemetry() {
        Feature feature = FeatureFlagClient.createFeature(TELEMETRY_FEATURE, TEST_ENDPOINT);

        assertEquals("wz4oTwm3SjARe1SrmzT7", feature.getTelemetry().getMetadata().get("AllocationId"));

        feature = FeatureFlagClient.createFeature(ALL_FEATURE, TEST_ENDPOINT);
        assertNull(feature.getTelemetry());
    }

    @Test
    public void testAllocationIdWithDifferentSeed() {
        FeatureFlagConfigurationSetting featureFlag = createItemFeatureFlag(
            ".appconfig.featureflag/", "TestFeature",
            "{\"allocation\":{\"seed\":\"newSeed\"},\"telemetry\":{\"enabled\":true}}", FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

        Feature feature = FeatureFlagClient.createFeature(featureFlag, TEST_ENDPOINT);
        assertEquals("RkxUK5CoaOaNWBjc55Mi", feature.getTelemetry().getMetadata().get("AllocationId"));
    }

    @Test
    public void testAllocationIdWithVariants() {
        String flagValue = "{\"allocation\": { \"percentile\": [{\"variant\": \"Off\", \"from\": 0, \"to\": 50}, {\"variant\": \"On\", \"from\": 50, \"to\": 100}], \"default_when_enabled\": \"Off2\", \"default_when_disabled\": \"Off\" }, \"telemetry\": {\"enabled\": true}}";
        FeatureFlagConfigurationSetting featureFlag = createItemFeatureFlag(
            ".appconfig.featureflag/", "TestFeature", flagValue, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

        Feature feature = FeatureFlagClient.createFeature(featureFlag, TEST_ENDPOINT);
        assertEquals("wGzzPy4qGy92SHnMtSvY", feature.getTelemetry().getMetadata().get("AllocationId"));
    }

    @Test
    public void testAllocationIdWithEmptyAllocation() {
        FeatureFlagConfigurationSetting featureFlag = createItemFeatureFlag(
            ".appconfig.featureflag/", "TestFeature",
            "{\"allocation\":{},\"telemetry\":{\"enabled\":true}}}", FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

        Feature feature = FeatureFlagClient.createFeature(featureFlag, TEST_ENDPOINT);
        assertNull(feature.getTelemetry().getMetadata().get("AllocationId"));
    }
}
