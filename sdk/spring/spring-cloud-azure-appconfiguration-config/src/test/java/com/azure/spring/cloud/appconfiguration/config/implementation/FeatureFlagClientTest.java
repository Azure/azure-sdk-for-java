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
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Allocation;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Variant;

public class FeatureFlagClientTest {

    @Mock
    private AppConfigurationReplicaClient clientMock;

    @Mock
    private Context contextMock;

    private FeatureFlagClient featureFlagClient;

    private String[] emptyLabelList = {"\0"};

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

        List<FeatureFlags> featureFlagsList = featureFlagClient.loadFeatureFlags(clientMock, null, emptyLabelList,
                contextMock);
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

        List<FeatureFlags> featureFlagsList = featureFlagClient.loadFeatureFlags(clientMock, null, emptyLabelList,
                contextMock);
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

        List<FeatureFlags> featureFlagsList = featureFlagClient.loadFeatureFlags(clientMock, null, emptyLabelList,
                contextMock);
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

        List<FeatureFlags> featureFlagsList = featureFlagClient.loadFeatureFlags(clientMock, null, emptyLabelList,
                contextMock);
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
                "{\"allocation\":{\"seed\":\"newSeed\"},\"telemetry\":{\"enabled\":true}}", FEATURE_LABEL,
                FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

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
                "{\"allocation\":{},\"telemetry\":{\"enabled\":true}}}", FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE,
                TEST_E_TAG);

        Feature feature = FeatureFlagClient.createFeature(featureFlag, TEST_ENDPOINT);
        assertNull(feature.getTelemetry().getMetadata().get("AllocationId"));
    }

    @Test
    public void testVariantsParsing() {
        String flagValue = "{\"id\":\"TestFeature\",\"enabled\":true,"
                + "\"variants\":["
                + "{\"name\":\"Red\",\"configuration_value\":\"#FF0000\",\"status_override\":\"Enabled\"},"
                + "{\"name\":\"Green\",\"configuration_value\":\"#00FF00\",\"status_override\":\"None\"}"
                + "]}";
        FeatureFlagConfigurationSetting featureFlag = createItemFeatureFlag(
                ".appconfig.featureflag/", "TestFeature", flagValue, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

        Feature feature = FeatureFlagClient.createFeature(featureFlag, TEST_ENDPOINT);

        assertNull(feature.getAllocation());
        assertEquals(2, feature.getVariants().size());

        Variant redVariant = feature.getVariants().get(0);
        assertEquals("Red", redVariant.getName());
        assertEquals("#FF0000", redVariant.getConfigurationValue());
        assertEquals("Enabled", redVariant.getStatusOverride());

        Variant greenVariant = feature.getVariants().get(1);
        assertEquals("Green", greenVariant.getName());
        assertEquals("#00FF00", greenVariant.getConfigurationValue());
        assertEquals("None", greenVariant.getStatusOverride());
    }

    @Test
    public void testAllocationParsing() {
        String flagValue = "{\"id\":\"TestFeature\",\"enabled\":true,"
                + "\"allocation\":{"
                + "\"default_when_enabled\":\"Red\","
                + "\"default_when_disabled\":\"Off\","
                + "\"seed\":\"testSeed\","
                + "\"user\":[{\"variant\":\"Green\",\"users\":[\"user1\",\"user2\"]}],"
                + "\"group\":[{\"variant\":\"Blue\",\"groups\":[\"group1\"]}],"
                + "\"percentile\":[{\"variant\":\"Red\",\"from\":0,\"to\":50},{\"variant\":\"Green\",\"from\":50,\"to\":100}]"
                + "}}";
        FeatureFlagConfigurationSetting featureFlag = createItemFeatureFlag(
                ".appconfig.featureflag/", "TestFeature", flagValue, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

        Feature feature = FeatureFlagClient.createFeature(featureFlag, TEST_ENDPOINT);

        assertNull(feature.getVariants());
        Allocation allocation = feature.getAllocation();
        assertEquals("Red", allocation.getDefaultWhenEnabled());
        assertEquals("Off", allocation.getDefaultWhenDisabled());
        assertEquals("testSeed", allocation.getSeed());

        assertEquals(1, allocation.getUser().size());
        assertEquals("Green", allocation.getUser().get(0).getVariant());
        assertEquals(2, allocation.getUser().get(0).getUsers().size());
        assertTrue(allocation.getUser().get(0).getUsers().contains("user1"));
        assertTrue(allocation.getUser().get(0).getUsers().contains("user2"));

        assertEquals(1, allocation.getGroup().size());
        assertEquals("Blue", allocation.getGroup().get(0).getVariant());
        assertEquals(1, allocation.getGroup().get(0).getGroups().size());
        assertTrue(allocation.getGroup().get(0).getGroups().contains("group1"));

        assertEquals(2, allocation.getPercentile().size());
        assertEquals("Red", allocation.getPercentile().get(0).getVariant());
        assertEquals(0.0, allocation.getPercentile().get(0).getFrom());
        assertEquals(50.0, allocation.getPercentile().get(0).getTo());
        assertEquals("Green", allocation.getPercentile().get(1).getVariant());
        assertEquals(50.0, allocation.getPercentile().get(1).getFrom());
        assertEquals(100.0, allocation.getPercentile().get(1).getTo());
    }

    @Test
    public void testVariantsAndAllocationTogether() {
        String flagValue = "{\"id\":\"TestFeature\",\"enabled\":true,"
                + "\"variants\":["
                + "{\"name\":\"Red\",\"configuration_value\":\"#FF0000\"},"
                + "{\"name\":\"Green\",\"configuration_value\":\"#00FF00\"}"
                + "],"
                + "\"allocation\":{"
                + "\"default_when_enabled\":\"Red\","
                + "\"percentile\":[{\"variant\":\"Red\",\"from\":0,\"to\":50},{\"variant\":\"Green\",\"from\":50,\"to\":100}]"
                + "}}";
        FeatureFlagConfigurationSetting featureFlag = createItemFeatureFlag(
                ".appconfig.featureflag/", "TestFeature", flagValue, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

        Feature feature = FeatureFlagClient.createFeature(featureFlag, TEST_ENDPOINT);

        // Test variants
        assertEquals(2, feature.getVariants().size());
        assertEquals("Red", feature.getVariants().get(0).getName());
        assertEquals("Green", feature.getVariants().get(1).getName());

        // Test allocation
        Allocation allocation = feature.getAllocation();
        assertEquals("Red", allocation.getDefaultWhenEnabled());
        assertEquals(2, allocation.getPercentile().size());
    }

    @Test
    public void testVariantsWithComplexConfigurationValue() {
        String flagValue = "{\"id\":\"TestFeature\",\"enabled\":true,"
                + "\"variants\":["
                + "{\"name\":\"SimpleString\",\"configuration_value\":\"hello\"},"
                + "{\"name\":\"Number\",\"configuration_value\":42},"
                + "{\"name\":\"Boolean\",\"configuration_value\":true},"
                + "{\"name\":\"Object\",\"configuration_value\":{\"key\":\"value\",\"nested\":{\"prop\":123}}}"
                + "]}";
        FeatureFlagConfigurationSetting featureFlag = createItemFeatureFlag(
                ".appconfig.featureflag/", "TestFeature", flagValue, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

        Feature feature = FeatureFlagClient.createFeature(featureFlag, TEST_ENDPOINT);

        assertEquals(4, feature.getVariants().size());

        assertEquals("hello", feature.getVariants().get(0).getConfigurationValue());
        assertEquals(42, feature.getVariants().get(1).getConfigurationValue());
        assertEquals(true, feature.getVariants().get(2).getConfigurationValue());

        // For complex objects, Jackson will parse them as LinkedHashMap
        assertTrue(feature.getVariants().get(3).getConfigurationValue() instanceof LinkedHashMap);
    }

    @Test
    public void testFeatureFlagWithoutVariantsOrAllocation() {
        String flagValue = "{\"id\":\"TestFeature\",\"enabled\":true}";
        FeatureFlagConfigurationSetting featureFlag = createItemFeatureFlag(
                ".appconfig.featureflag/", "TestFeature", flagValue, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

        Feature feature = FeatureFlagClient.createFeature(featureFlag, TEST_ENDPOINT);

        assertNull(feature.getVariants());
        assertNull(feature.getAllocation());
        assertEquals("TestFeature", feature.getId());
        assertTrue(feature.isEnabled());
    }
}
