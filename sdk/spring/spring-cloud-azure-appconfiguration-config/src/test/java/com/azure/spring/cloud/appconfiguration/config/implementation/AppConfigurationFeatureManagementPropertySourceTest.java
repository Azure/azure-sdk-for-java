// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.DEFAULT_ROLLOUT_PERCENTAGE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.EMPTY_LABEL;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.FEATURE_MANAGEMENT_KEY;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.GROUPS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.USERS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_BOOLEAN_VALUE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_LABEL;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_VALUE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_VALUE_PARAMETERS;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.FEATURE_VALUE_TARGETING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestUtils.createItemFeatureFlag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

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

public class AppConfigurationFeatureManagementPropertySourceTest {

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

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_NULL = createItemFeatureFlag(
        ".appconfig.featureflag/", "Alpha",
        FEATURE_VALUE,
        FEATURE_LABEL, null);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_TARGETING = createItemFeatureFlag(
        ".appconfig.featureflag/", "target",
        FEATURE_VALUE_TARGETING, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private AppConfigurationFeatureManagementPropertySource propertySource;

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

        FEATURE_ITEMS_TARGETING.add(FEATURE_ITEM_TARGETING);
    }

    @BeforeEach
    public void init() {
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        MockitoAnnotations.openMocks(this);

        featureFlagStore = new FeatureFlagStore();

        String[] labelFilter = { EMPTY_LABEL };

        propertySource = new AppConfigurationFeatureManagementPropertySource(TEST_STORE_NAME, clientMock, "",
            labelFilter);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testFeatureFlagCanBeInitedAndQueried() {
        when(featureListMock.iterator()).thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listSettings(Mockito.any()))
            .thenReturn(featureListMock).thenReturn(featureListMock);
        when(clientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0));
        featureFlagStore.setEnabled(true);

        propertySource.initProperties();

        HashMap<Integer, FeatureFlagFilter> filters = new HashMap<>();
        FeatureFlagFilter ffec = new FeatureFlagFilter("TestFilter");
        filters.put(0, ffec);
        Feature gamma = new Feature();
        gamma.setKey("Gamma");
        filters = new HashMap<>();
        ffec = new FeatureFlagFilter("TestFilter");
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("key", "value");
        ffec.setParameters(parameters);
        filters.put(0, ffec);
        gamma.setEnabledFor(filters);

        assertEquals(gamma.getKey(),
            ((Feature) propertySource.getProperty(FEATURE_MANAGEMENT_KEY + "Gamma")).getKey());
    }

    @Test
    public void testFeatureFlagThrowError() {
        when(featureListMock.iterator()).thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listSettings(Mockito.any())).thenReturn(featureListMock);
        when(clientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0));
        try {
            propertySource.initProperties();
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

        propertySource.initProperties();

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = {};

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }

    @Test
    public void testFeatureFlagTargeting() {
        when(featureListMock.iterator()).thenReturn(FEATURE_ITEMS_TARGETING.iterator());
        when(clientMock.listSettings(Mockito.any()))
            .thenReturn(featureListMock).thenReturn(featureListMock);
        when(clientMock.getTracingInfo()).thenReturn(new TracingInfo(false, false, 0));
        featureFlagStore.setEnabled(true);

        propertySource.initProperties();

        FeatureSet featureSetExpected = new FeatureSet();
        Feature feature = new Feature();
        feature.setKey("target");
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
        feature.setEnabledFor(filters);

        featureSetExpected.addFeature("target", feature);
        Feature targeting = (Feature) propertySource.getProperty(FEATURE_MANAGEMENT_KEY + "target");

        FeatureFlagFilter filter = targeting.getEnabledFor().get(0);

        assertNotNull(filter);
        assertEquals("targetingFilter", filter.getName());
        assertEquals(parameters.size(), filter.getParameters().size());
    }
}
