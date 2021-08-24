// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static com.azure.spring.cloud.config.Constants.FEATURE_FLAG_CONTENT_TYPE;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_BOOLEAN_VALUE;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_LABEL;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_VALUE;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_VALUE_PARAMETERS;
import static com.azure.spring.cloud.config.TestConstants.FEATURE_VALUE_TARGETING;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONTEXT;
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
import static com.azure.spring.cloud.config.TestUtils.createItem;
import static com.azure.spring.cloud.config.TestUtils.createItemFeatureFlag;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
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
import com.azure.spring.cloud.config.feature.management.entity.Feature;
import com.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.properties.FeatureFlagStore;
import com.azure.spring.cloud.config.stores.ClientStore;
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

    private static final ConfigurationSetting ITEM_1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_NULL = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3,
        TEST_LABEL_3,
        null);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM = createItemFeatureFlag(".appconfig.featureflag/", "Alpha",
        FEATURE_VALUE, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_2 = createItemFeatureFlag(".appconfig.featureflag/", "Beta",
        FEATURE_BOOLEAN_VALUE, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_3 = createItemFeatureFlag(".appconfig.featureflag/", "Gamma",
        FEATURE_VALUE_PARAMETERS, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_NULL = createItemFeatureFlag(".appconfig.featureflag/", "Alpha",
        FEATURE_VALUE,
        FEATURE_LABEL, null);

    private static final FeatureFlagConfigurationSetting FEATURE_ITEM_TARGETING = createItemFeatureFlag(".appconfig.featureflag/", "target",
        FEATURE_VALUE_TARGETING, FEATURE_LABEL, FEATURE_FLAG_CONTENT_TYPE);

    private static final String FEATURE_MANAGEMENT_KEY = "feature-management.featureManagement";

    private static ObjectMapper mapper = new ObjectMapper();

    private List<ConfigurationSetting> testItems = new ArrayList<>();

    private AppConfigurationPropertySource propertySource;

    private AppConfigurationProperties appConfigurationProperties;

    @Mock
    private ClientStore clientStoreMock;

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
    
    @BeforeAll
    public static void setup() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING);
        
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
        ArrayList<String> contexts = new ArrayList<String>();
        contexts.add("/application/*");
        AppConfigurationStoreSelects selectedKeys = new AppConfigurationStoreSelects().setKeyFilter("/application/").setLabelFilter("\0");
        propertySource = new AppConfigurationPropertySource(TEST_CONTEXT, configStoreMock, selectedKeys, new ArrayList<>(),
            appConfigurationProperties, clientStoreMock, appProperties, tokenCredentialProvider, null, null);

        testItems = new ArrayList<ConfigurationSetting>();
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
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testPropCanBeInitAndQueried() throws IOException {
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenReturn(testItems)
            .thenReturn(FEATURE_ITEMS);

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(featureSet);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
            .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);
        String[] allExpectedKeyNames = ArrayUtils.addAll(expectedKeyNames, FEATURE_MANAGEMENT_KEY);

        assertThat(keyNames).containsExactlyInAnyOrder(allExpectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
    }

    @Test
    public void testPropertyNameSlashConvertedToDots() throws IOException {
        ConfigurationSetting slashedProp = createItem(TEST_CONTEXT, TEST_SLASH_KEY, TEST_SLASH_VALUE, null,
            EMPTY_CONTENT_TYPE);
        List<ConfigurationSetting> settings = new ArrayList<ConfigurationSetting>();
        settings.add(slashedProp);
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenReturn(settings)
            .thenReturn(new ArrayList<ConfigurationSetting>());
        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }

        String expectedKeyName = TEST_SLASH_KEY.replace('/', '.');
        String[] actualKeyNames = propertySource.getPropertyNames();

        assertThat(actualKeyNames.length).isEqualTo(1);
        assertThat(actualKeyNames[0]).isEqualTo(expectedKeyName);
        assertThat(propertySource.getProperty(TEST_SLASH_KEY)).isNull();
        assertThat(propertySource.getProperty(expectedKeyName)).isEqualTo(TEST_SLASH_VALUE);
    }

    @Test
    public void testFeatureFlagCanBeInitedAndQueried() throws IOException {
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString()))
            .thenReturn(FEATURE_ITEMS).thenReturn(new ArrayList<ConfigurationSetting>());
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
        HashMap<Integer, FeatureFlagFilter> filters = new HashMap<Integer, FeatureFlagFilter>();
        FeatureFlagFilter ffec = new FeatureFlagFilter("TestFilter");
        filters.put(0, ffec);
        feature.setEnabledFor(filters);
        Feature gamma = new Feature();
        gamma.setKey("Gamma");
        filters = new HashMap<Integer, FeatureFlagFilter>();
        ffec = new FeatureFlagFilter("TestFilter");
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put("key", "value");
        ffec.setParameters(parameters);
        filters.put(0, ffec);
        gamma.setEnabledFor(filters);
        featureSetExpected.addFeature("Alpha", feature);
        featureSetExpected.addFeature("Beta", true);
        featureSetExpected.addFeature("Gamma", gamma);
        LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSetExpected.getFeatureManagement(),
            LinkedHashMap.class);

        assertEquals(convertedValue, propertySource.getProperty(FEATURE_MANAGEMENT_KEY));
    }
    
    @Test
    public void testFeatureFlagDisabled() throws IOException {
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString()))
            .thenReturn(new ArrayList<ConfigurationSetting>()).thenReturn(FEATURE_ITEMS);
        featureFlagStore.setEnabled(false);

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
        propertySource.initFeatures(featureSet);

        assertNull(propertySource.getProperty(FEATURE_MANAGEMENT_KEY));
    }

    @Test
    public void testFeatureFlagThrowError() throws IOException {
        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            assertEquals("Found Feature Flag /foo/test_key_1 with invalid Content Type of ", e.getMessage());
        }
    }

    @Test
    public void testFeatureFlagBuildError() throws IOException {
        featureFlagStore.setEnabled(true);
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenReturn(FEATURE_ITEMS);

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail();
        }
        propertySource.initFeatures(featureSet);

        FeatureSet featureSetExpected = new FeatureSet();

        HashMap<Integer, FeatureFlagFilter> filters = new HashMap<Integer, FeatureFlagFilter>();
        FeatureFlagFilter ffec = new FeatureFlagFilter("TestFilter");

        filters.put(0, ffec);

        Feature alpha = new Feature();
        alpha.setKey("Alpha");
        alpha.setEnabledFor(filters);

        HashMap<Integer, FeatureFlagFilter> filters2 = new HashMap<Integer, FeatureFlagFilter>();
        FeatureFlagFilter ffec2 = new FeatureFlagFilter("TestFilter");

        filters2.put(0, ffec2);

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();
        parameters.put("key", "value");
        ffec2.setParameters(parameters);

        Feature gamma = new Feature();
        gamma.setKey("Gamma");
        gamma.setEnabledFor(filters2);
        filters2.put(0, ffec2);

        featureSetExpected.addFeature("Alpha", alpha);
        featureSetExpected.addFeature("Beta", true);
        featureSetExpected.addFeature("Gamma", gamma);
        LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSetExpected.getFeatureManagement(),
            LinkedHashMap.class);

        assertEquals(convertedValue, propertySource.getProperty(FEATURE_MANAGEMENT_KEY));
    }

    @Test
    public void initNullValidContentTypeTest() throws IOException {
        ArrayList<ConfigurationSetting> items = new ArrayList<ConfigurationSetting>();
        items.add(ITEM_NULL);
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenReturn(items)
            .thenReturn(new ArrayList<ConfigurationSetting>());

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = items.stream()
            .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }

    @Test
    public void initNullInvalidContentTypeFeatureFlagTest() throws IOException {
        ArrayList<ConfigurationSetting> items = new ArrayList<ConfigurationSetting>();
        items.add(FEATURE_ITEM_NULL);
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString()))
            .thenReturn(new ArrayList<ConfigurationSetting>()).thenReturn(items);

        FeatureSet featureSet = new FeatureSet();
        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {

        }

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = {};

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }

    @Test
    public void testFeatureFlagTargeting() throws IOException {
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString()))
            .thenReturn(FEATURE_ITEMS_TARGETING).thenReturn(new ArrayList<ConfigurationSetting>());
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
        feature.setKey("target");
        HashMap<Integer, FeatureFlagFilter> filters = new HashMap<Integer, FeatureFlagFilter>();
        FeatureFlagFilter ffec = new FeatureFlagFilter("targetingFilter");

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<String, Object>();

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
        LinkedHashMap<?, ?> convertedValue = mapper.convertValue(featureSetExpected.getFeatureManagement(),
            LinkedHashMap.class);
        System.out.println(convertedValue.toString());
        System.out.println(propertySource.getProperty(FEATURE_MANAGEMENT_KEY).toString());
        assertEquals(convertedValue.toString().length(), propertySource.getProperty(FEATURE_MANAGEMENT_KEY).toString().length());
    }
}
