// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_ROLLOUT_PERCENTAGE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.GROUPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.USERS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_BOOLEAN_VALUE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_LABEL;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_VALUE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_VALUE_PARAMETERS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_VALUE_TARGETING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_VALUE_TELEMETRY;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_E_TAG;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestUtils.createItemFeatureFlag;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.util.Configuration;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.Feature;
import com.azure.spring.cloud.appconfiguration.config.implementation.feature.entity.FeatureSet;
import com.azure.spring.cloud.appconfiguration.config.implementation.http.policy.TracingInfo;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.FeatureFlagStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public class FeatureFlagLoaderTest2 {

    public static final List<ConfigurationSetting> FEATURE_ITEMS = new ArrayList<>();

    public static final List<ConfigurationSetting> FEATURE_ITEMS_TARGETING = new ArrayList<>();

    private static final AppConfigurationProperties TEST_PROPS = new AppConfigurationProperties();

    private static final String KEY_FILTER = "/foo/";

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM = createItemFeatureFlag(".appconfig.featureflag/",
        "Alpha",
        FEATURE_VALUE, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_2 = createItemFeatureFlag(
        ".appconfig.featureflag/", "Beta",
        FEATURE_BOOLEAN_VALUE, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_3 = createItemFeatureFlag(
        ".appconfig.featureflag/", "Gamma",
        FEATURE_VALUE_PARAMETERS, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_4 = createItemFeatureFlag(
        ".appconfig.featureflag/", "Delta",
        FEATURE_VALUE_TELEMETRY, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE, TEST_E_TAG);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_NULL = createItemFeatureFlag(
        ".appconfig.featureflag/", "Alpha",
        FEATURE_VALUE,
        FEATURE_LABEL, null);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_TARGETING = createItemFeatureFlag(
        ".appconfig.featureflag/", "target",
        FEATURE_VALUE_TARGETING, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FeatureFlagLoader featureFlagLoader;

    @Mock
    private AppConfigurationReplicaClient clientMock;

    private FeatureFlagStore featureFlagStore;

    @Mock
    private List<ConfigurationSetting> featureListMock;

    @BeforeAll
    public static void setup() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING, KEY_FILTER);

        FEATURE_ITEM.setContentType(FEATURE_FLAG_CONTENT_TYPE);
        FEATURE_ITEMS.add(FEATURE_ITEM);
        FEATURE_ITEMS.add(FEATURE_ITEM_2);
        FEATURE_ITEMS.add(FEATURE_ITEM_3);
        FEATURE_ITEMS.add(FEATURE_ITEM_4);

        FEATURE_ITEMS_TARGETING.add(FEATURE_ITEM_TARGETING);
    }

    @BeforeEach
    public void init() {
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        MockitoAnnotations.openMocks(this);

        featureFlagStore = new FeatureFlagStore();

        featureFlagLoader = new FeatureFlagLoader();
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void overrideTest() {
        String[] labels = {"test"};
        when(featureListMock.iterator()).thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listSettings(Mockito.any()))
            .thenReturn(featureListMock).thenReturn(featureListMock);
        when(clientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));
        featureFlagStore.setEnabled(true);

        //propertySourceOverride.initProperties(null);

        Map<Integer, FeatureFlagFilter> filters = new HashMap<>();
        FeatureFlagFilter ffec = new FeatureFlagFilter("TestFilter");
        filters.put(0, ffec);
        Feature gamma = new Feature();
        gamma.setId("Gamma");
        filters = new HashMap<>();
        ffec = new FeatureFlagFilter("TestFilter");
        Map<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("key", "value");
        ffec.setParameters(parameters);
        filters.put(0, ffec);
        //gamma.setEnabledFor(filters);

        //assertEquals(gamma.getId(),
        //    ((Feature) propertySourceOverride.getProperty(FEATURE_MANAGEMENT_KEY + "Gamma")));
    }

    @Test
    public void testFeatureFlagCanBeInitedAndQueried() {
        when(featureListMock.iterator()).thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listSettings(Mockito.any()))
            .thenReturn(featureListMock).thenReturn(featureListMock);
        when(clientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));
        featureFlagStore.setEnabled(true);

        //propertySource.initProperties(null);

        HashMap<Integer, FeatureFlagFilter> filters = new HashMap<>();
        FeatureFlagFilter ffec = new FeatureFlagFilter("TestFilter");
        filters.put(0, ffec);
        Feature gamma = new Feature();
        gamma.setId("Gamma");
        filters = new HashMap<>();
        ffec = new FeatureFlagFilter("TestFilter");
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("key", "value");
        ffec.setParameters(parameters);
        filters.put(0, ffec);
        //gamma.setEnabledFor(filters);

        //assertEquals(gamma.getId(),
        //    ((Feature) propertySource.getProperty(FEATURE_MANAGEMENT_KEY + "Gamma")));
    }

    @Test
    public void testFeatureFlagThrowError() {
        when(featureListMock.iterator()).thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listSettings(Mockito.any())).thenReturn(featureListMock);
        when(clientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));
        try {
            //propertySource.initProperties(null);
        } catch (Exception e) {
            assertEquals("Found Feature Flag /foo/test_key_1 with invalid Content Type of ", e.getMessage());
        }
    }

    @Test
    public void initNullInvalidContentTypeFeatureFlagTest() {
        ArrayList<ConfigurationSetting> items = new ArrayList<>();
        items.add(FEATURE_ITEM_NULL);
        when(featureListMock.iterator()).thenReturn(Collections.emptyIterator())
            .thenReturn(items.iterator());
        when(clientMock.listSettings(Mockito.any()))
            .thenReturn(featureListMock).thenReturn(featureListMock);

        //propertySource.initProperties(null);

        //String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = {};

        //assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }

    @Test
    public void testFeatureFlagTargeting() {
        when(featureListMock.iterator()).thenReturn(FEATURE_ITEMS_TARGETING.iterator());
        when(clientMock.listSettings(Mockito.any()))
            .thenReturn(featureListMock).thenReturn(featureListMock);
        when(clientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));
        featureFlagStore.setEnabled(true);

        List<FeatureFlags> featureFlags = featureFlagLoader.loadFeatureFlags(clientMock, KEY_FILTER, null);

        FeatureSet featureSetExpected = new FeatureSet();
        Feature feature = new Feature();
        feature.setId("target");
        HashMap<Integer, FeatureFlagFilter> filters = new HashMap<>();
        FeatureFlagFilter ffec = new FeatureFlagFilter("targetingFilter");

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

        ffec.setParameters(parameters);
        filters.put(0, ffec);
        //feature.setEnabledFor(filters);

        featureSetExpected.addFeature("target", feature);
        //Feature targeting = (Feature) propertySource.getProperty(FEATURE_MANAGEMENT_KEY + "target");

        //FeatureFlagFilter filter = targeting.getEnabledFor().get(0);

        //assertNotNull(filter);
        //assertEquals("targetingFilter", filter.getName());
        //assertEquals(parameters.size(), filter.getParameters().size());
    }

    @Test
    public void testFeatureFlagTelemetry() {
        when(featureListMock.iterator()).thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listSettings(Mockito.any()))
            .thenReturn(featureListMock).thenReturn(featureListMock);
        when(clientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0, Configuration.getGlobalConfiguration()));
        when(clientMock.getEndpoint()).thenReturn(TEST_ENDPOINT);
        featureFlagStore.setEnabled(true);

        //propertySource.initProperties(null);

        String featureFlagId = "yON6V7DTGfVgOKfnPtue_2hS-CFVV5ecv-dcjqCFQt4";
        String featureFlagReference = String.format("%s/kv/%s", TEST_ENDPOINT, ".appconfig.featureflag/Delta");

        //Feature featureTest = ((Feature) propertySource.getProperty(FEATURE_MANAGEMENT_KEY + "Delta"));
        //assertEquals(featureFlagId, featureTest.getTelemetry().getMetadata().get(FEATURE_FLAG_ID));
        //assertEquals(featureFlagReference, featureTest.getTelemetry().getMetadata().get(FEATURE_FLAG_REFERENCE));
        //assertEquals(TEST_E_TAG, featureTest.getTelemetry().getMetadata().get(E_TAG));
    }
}
