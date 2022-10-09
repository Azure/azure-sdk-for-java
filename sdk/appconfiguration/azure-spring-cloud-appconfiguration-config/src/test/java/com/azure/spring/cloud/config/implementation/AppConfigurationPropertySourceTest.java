// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static com.azure.spring.cloud.config.AppConfigurationConstants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_BOOLEAN_VALUE;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_LABEL;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_VALUE;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_VALUE_PARAMETERS;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_VALUE_TARGETING;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_KEY_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_KEY_2;
import static com.azure.spring.cloud.config.TestConstants.TEST_KEY_3;
import static com.azure.spring.cloud.config.TestConstants.TEST_LABEL_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_LABEL_2;
import static com.azure.spring.cloud.config.TestConstants.TEST_LABEL_3;
import static com.azure.spring.cloud.config.TestConstants.TEST_SLASH_KEY;
import static com.azure.spring.cloud.config.TestConstants.TEST_SLASH_VALUE;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.config.TestConstants.TEST_VALUE_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_VALUE_2;
import static com.azure.spring.cloud.config.TestConstants.TEST_VALUE_3;
import static com.azure.spring.cloud.config.implementation.TestUtils.createItem;
import static com.azure.spring.cloud.config.implementation.TestUtils.createItemFeatureFlag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting;
import com.azure.data.appconfiguration.models.FeatureFlagFilter;
import com.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.azure.spring.cloud.config.feature.management.entity.Feature;
import com.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AppConfigurationPropertySourceTest {

    public static final List<ConfigurationSetting> FEATURE_ITEMS = new ArrayList<>();

    public static final List<ConfigurationSetting> FEATURE_ITEMS_TARGETING = new ArrayList<>();

    private static final String EMPTY_CONTENT_TYPE = "";

    private static final String USERS = "users";

    private static final String GROUPS = "groups";

    private static final String DEFAULT_ROLLOUT_PERCENTAGE = "defaultRolloutPercentage";

    private static final AppConfigurationProperties TEST_PROPS = new AppConfigurationProperties();

    private static final String KEY_FILTER = "/foo/";

    private static final ConfigurationSetting ITEM_1 = createItem(KEY_FILTER, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_2 = createItem(KEY_FILTER, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_3 = createItem(KEY_FILTER, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_NULL = createItem(KEY_FILTER, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3,
        null);

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

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    private static ObjectMapper mapper = new ObjectMapper();

    private List<ConfigurationSetting> testItems = new ArrayList<>();

    private AppConfigurationPropertySource propertySource;

    private AppConfigurationProperties appConfigurationProperties;

    @Mock
    private AppConfigurationReplicaClient clientMock;

    @Mock
    private ConfigurationAsyncClient configClientMock;

    @Mock
    private PagedFlux<ConfigurationSetting> settingsMock;

    @Mock
    private Flux<PagedResponse<ConfigurationSetting>> pageMock;

    @Mock
    private Mono<List<PagedResponse<ConfigurationSetting>>> collectionMock;

    @Mock
    private List<PagedResponse<ConfigurationSetting>> itemsMock;

    @Mock
    private Iterator<PagedResponse<ConfigurationSetting>> itemsIteratorMock;

    @Mock
    private PagedResponse<ConfigurationSetting> pagedResponseMock;

    @Mock
    private ConfigStore configStoreMock;

    private FeatureFlagStore featureFlagStore;

    private AppConfigurationProviderProperties appProperties;

    private KeyVaultCredentialProvider tokenCredentialProvider = null;

    @Mock
    private List<ConfigurationSetting> configurationListMock;

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
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        MockitoAnnotations.openMocks(this);
        appConfigurationProperties = new AppConfigurationProperties();
        appProperties = new AppConfigurationProviderProperties();

        AppConfigurationStoreSelects selectedKeys = new AppConfigurationStoreSelects().setKeyFilter(KEY_FILTER)
            .setLabelFilter("\0");

        testItems = new ArrayList<>();
        testItems.add(ITEM_1);
        testItems.add(ITEM_2);
        testItems.add(ITEM_3);

        when(configStoreMock.getEndpoint()).thenReturn(TEST_STORE_NAME);
        featureFlagStore = new FeatureFlagStore();
        when(configStoreMock.getFeatureFlags()).thenReturn(featureFlagStore);
        when(configClientMock.listConfigurationSettings(Mockito.any())).thenReturn(settingsMock);
        when(settingsMock.byPage()).thenReturn(pageMock);
        when(pageMock.collectList()).thenReturn(collectionMock);
        when(collectionMock.block()).thenReturn(itemsMock);
        when(itemsMock.iterator()).thenReturn(itemsIteratorMock);
        when(itemsIteratorMock.next()).thenReturn(pagedResponseMock);

        propertySource = new AppConfigurationPropertySource(configStoreMock, selectedKeys, new ArrayList<>(),
            appConfigurationProperties, clientMock, appProperties, tokenCredentialProvider, null, null);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testPropCanBeInitAndQueried() throws AppConfigurationStatusException, IOException {
        when(configurationListMock.iterator()).thenReturn(testItems.iterator()).thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listConfigurationSettings(Mockito.any())).thenReturn(configurationListMock)
            .thenReturn(configurationListMock);

        FeatureSet featureSet = new FeatureSet();
        propertySource.initProperties(featureSet);
        propertySource.initFeatures(featureSet);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
            .map(t -> t.getKey().substring(KEY_FILTER.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = new String[expectedKeyNames.length + 1];

        String[] featureManagementKey = { FEATURE_MANAGEMENT_KEY };

        System.arraycopy(expectedKeyNames, 0, allExpectedKeyNames, 0, expectedKeyNames.length);
        System.arraycopy(featureManagementKey, 0, allExpectedKeyNames, expectedKeyNames.length, 1);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
    }

    @Test
    public void testPropertyNameSlashConvertedToDots() throws AppConfigurationStatusException, IOException {
        ConfigurationSetting slashedProp = createItem(KEY_FILTER, TEST_SLASH_KEY, TEST_SLASH_VALUE, null,
            EMPTY_CONTENT_TYPE);
        List<ConfigurationSetting> settings = new ArrayList<>();
        settings.add(slashedProp);
        when(configurationListMock.iterator()).thenReturn(settings.iterator())
            .thenReturn(Collections.<ConfigurationSetting>emptyIterator());
        when(clientMock.listConfigurationSettings(Mockito.any())).thenReturn(configurationListMock)
            .thenReturn(configurationListMock);
        FeatureSet featureSet = new FeatureSet();
        propertySource.initProperties(featureSet);

        String expectedKeyName = TEST_SLASH_KEY.replace('/', '.');
        String[] actualKeyNames = propertySource.getPropertyNames();

        assertThat(actualKeyNames.length).isEqualTo(1);
        assertThat(actualKeyNames[0]).isEqualTo(expectedKeyName);
        assertThat(propertySource.getProperty(TEST_SLASH_KEY)).isNull();
        assertThat(propertySource.getProperty(expectedKeyName)).isEqualTo(TEST_SLASH_VALUE);
    }

    @Test
    public void testFeatureFlagCanBeInitedAndQueried() {
        when(configurationListMock.iterator()).thenReturn(Collections.emptyIterator())
            .thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listConfigurationSettings(Mockito.any()))
            .thenReturn(configurationListMock).thenReturn(configurationListMock);
        featureFlagStore.setEnabled(true);

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(featureSet);

        FeatureSet featureSetExpected = new FeatureSet();
        Feature feature = new Feature();
        feature.setKey("Alpha");
        HashMap<Integer, FeatureFlagFilter> filters = new HashMap<>();
        FeatureFlagFilter featureFlagFilter = new FeatureFlagFilter("TestFilter");
        filters.put(0, featureFlagFilter);
        feature.setEnabledFor(filters);
        Feature gamma = new Feature();
        gamma.setKey("Gamma");
        filters = new HashMap<>();
        featureFlagFilter = new FeatureFlagFilter("TestFilter");
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("key", "value");
        featureFlagFilter.setParameters(parameters);
        filters.put(0, featureFlagFilter);
        gamma.setEnabledFor(filters);
        featureSetExpected.addFeature("Alpha", feature);
        featureSetExpected.addFeature("Beta", true);
        featureSetExpected.addFeature("Gamma", gamma);
        LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSetExpected.getFeatureManagement(),
            LinkedHashMap.class);

        assertEquals(convertedValue, propertySource.getProperty(FEATURE_MANAGEMENT_KEY));
    }

    @Test
    public void testFeatureFlagDisabled() throws AppConfigurationStatusException, IOException {
        when(configurationListMock.iterator()).thenReturn(Collections.emptyIterator())
            .thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listConfigurationSettings(Mockito.any()))
            .thenReturn(configurationListMock).thenReturn(configurationListMock);
        featureFlagStore.setEnabled(false);

        FeatureSet featureSet = new FeatureSet();
        propertySource.initProperties(featureSet);
        propertySource.initFeatures(featureSet);

        assertNull(propertySource.getProperty(FEATURE_MANAGEMENT_KEY));
    }

    @Test
    public void testFeatureFlagThrowError() {
        FeatureSet featureSet = new FeatureSet();
        when(configurationListMock.iterator()).thenReturn(Collections.emptyIterator());
        when(clientMock.listConfigurationSettings(Mockito.any())).thenReturn(configurationListMock);
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            assertEquals("Found Feature Flag /foo/test_key_1 with invalid Content Type of ", e.getMessage());
        }
    }

    @Test
    public void testFeatureFlagBuildError() {
        featureFlagStore.setEnabled(true);
        when(configurationListMock.iterator()).thenReturn(Collections.emptyIterator())
            .thenReturn(FEATURE_ITEMS.iterator());
        when(clientMock.listConfigurationSettings(Mockito.any())).thenReturn(configurationListMock);

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail();
        }
        propertySource.initFeatures(featureSet);

        FeatureSet featureSetExpected = new FeatureSet();

        HashMap<Integer, FeatureFlagFilter> filters = new HashMap<>();
        FeatureFlagFilter featureFlagFilter = new FeatureFlagFilter("TestFilter");

        filters.put(0, featureFlagFilter);

        Feature alpha = new Feature();
        alpha.setKey("Alpha");
        alpha.setEnabledFor(filters);

        HashMap<Integer, FeatureFlagFilter> filters2 = new HashMap<>();
        FeatureFlagFilter featureFlagFilter2 = new FeatureFlagFilter("TestFilter");

        filters2.put(0, featureFlagFilter2);

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("key", "value");
        featureFlagFilter2.setParameters(parameters);

        Feature gamma = new Feature();
        gamma.setKey("Gamma");
        gamma.setEnabledFor(filters2);
        filters2.put(0, featureFlagFilter2);

        featureSetExpected.addFeature("Alpha", alpha);
        featureSetExpected.addFeature("Beta", true);
        featureSetExpected.addFeature("Gamma", gamma);
        LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSetExpected.getFeatureManagement(),
            LinkedHashMap.class);

        assertEquals(convertedValue, propertySource.getProperty(FEATURE_MANAGEMENT_KEY));
    }

    @Test
    public void initNullValidContentTypeTest() throws AppConfigurationStatusException, IOException {
        ArrayList<ConfigurationSetting> items = new ArrayList<>();
        items.add(ITEM_NULL);
        when(configurationListMock.iterator()).thenReturn(items.iterator())
            .thenReturn(Collections.emptyIterator());
        when(clientMock.listConfigurationSettings(Mockito.any())).thenReturn(configurationListMock);

        FeatureSet featureSet = new FeatureSet();
        propertySource.initProperties(featureSet);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = items.stream()
            .map(t -> t.getKey().substring(KEY_FILTER.length())).toArray(String[]::new);

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }

    @Test
    public void initNullInvalidContentTypeFeatureFlagTest() throws AppConfigurationStatusException, IOException {
        ArrayList<ConfigurationSetting> items = new ArrayList<>();
        items.add(FEATURE_ITEM_NULL);
        when(configurationListMock.iterator()).thenReturn(Collections.emptyIterator())
            .thenReturn(items.iterator());
        when(clientMock.listConfigurationSettings(Mockito.any()))
            .thenReturn(configurationListMock).thenReturn(configurationListMock);

        FeatureSet featureSet = new FeatureSet();
        propertySource.initProperties(featureSet);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = {};

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }

    @Test
    public void testFeatureFlagTargeting() throws AppConfigurationStatusException, IOException {
        when(configurationListMock.iterator()).thenReturn(Collections.emptyIterator())
            .thenReturn(FEATURE_ITEMS_TARGETING.iterator());
        when(clientMock.listConfigurationSettings(Mockito.any()))
            .thenReturn(configurationListMock).thenReturn(configurationListMock);
        featureFlagStore.setEnabled(true);

        FeatureSet featureSet = new FeatureSet();
        propertySource.initProperties(featureSet);
        propertySource.initFeatures(featureSet);

        FeatureSet featureSetExpected = new FeatureSet();
        Feature feature = new Feature();
        feature.setKey("target");
        HashMap<Integer, FeatureFlagFilter> filters = new HashMap<>();
        FeatureFlagFilter featureFlagFilter = new FeatureFlagFilter("targetingFilter");

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

        featureFlagFilter.setParameters(parameters);
        filters.put(0, featureFlagFilter);
        feature.setEnabledFor(filters);

        featureSetExpected.addFeature("target", feature);
        LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSetExpected.getFeatureManagement(),
            LinkedHashMap.class);

        assertEquals(convertedValue.toString().length(),
            propertySource.getProperty(FEATURE_MANAGEMENT_KEY).toString().length());
    }
}
