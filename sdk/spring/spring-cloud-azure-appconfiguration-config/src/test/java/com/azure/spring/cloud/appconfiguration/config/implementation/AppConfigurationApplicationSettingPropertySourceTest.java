// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_KEY_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_KEY_2;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_KEY_3;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_LABEL_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_LABEL_2;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_LABEL_3;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_SLASH_KEY;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_SLASH_VALUE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_VALUE_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_VALUE_2;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_VALUE_3;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;

public class AppConfigurationApplicationSettingPropertySourceTest {

    private static final String EMPTY_CONTENT_TYPE = "";

    private static final String JSON_CONTENT_TYPE = "application/json";

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

    private static final ConfigurationSetting ITEM_INVALID_JSON = createItem(KEY_FILTER, TEST_KEY_3, TEST_VALUE_3,
        TEST_LABEL_3,
        JSON_CONTENT_TYPE);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private List<ConfigurationSetting> testItems = new ArrayList<>();

    private AppConfigurationApplicationSettingPropertySource propertySource;

    @Mock
    private AppConfigurationReplicaClient clientMock;

    @Mock
    private AppConfigurationKeyVaultClientFactory keyVaultClientFactoryMock;

    @Mock
    private List<ConfigurationSetting> configurationListMock;
    
    private MockitoSession session;

    @BeforeAll
    public static void setup() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING, KEY_FILTER);
    }

    @BeforeEach
    public void init() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        MockitoAnnotations.openMocks(this);

        testItems = new ArrayList<>();
        testItems.add(ITEM_1);
        testItems.add(ITEM_2);
        testItems.add(ITEM_3);

        String[] labelFilter = { "\0" };

        propertySource = new AppConfigurationApplicationSettingPropertySource(TEST_STORE_NAME, clientMock,
            keyVaultClientFactoryMock, KEY_FILTER, labelFilter);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void testPropCanBeInitAndQueried() throws IOException {
        when(configurationListMock.iterator()).thenReturn(testItems.iterator());
        when(clientMock.listSettings(Mockito.any(), Mockito.anyBoolean())).thenReturn(configurationListMock)
            .thenReturn(configurationListMock);

        propertySource.initProperties(null, false);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = testItems.stream()
            .map(t -> t.getKey().substring(KEY_FILTER.length())).toArray(String[]::new);

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
    }

    @Test
    public void testPropertyNameSlashConvertedToDots() throws IOException {
        ConfigurationSetting slashedProp = createItem(KEY_FILTER, TEST_SLASH_KEY, TEST_SLASH_VALUE, null,
            EMPTY_CONTENT_TYPE);
        List<ConfigurationSetting> settings = new ArrayList<>();
        settings.add(slashedProp);
        when(configurationListMock.iterator()).thenReturn(settings.iterator())
            .thenReturn(Collections.emptyIterator());
        when(clientMock.listSettings(Mockito.any(), Mockito.anyBoolean())).thenReturn(configurationListMock)
            .thenReturn(configurationListMock);

        propertySource.initProperties(null, false);

        String expectedKeyName = TEST_SLASH_KEY.replace('/', '.');
        String[] actualKeyNames = propertySource.getPropertyNames();

        assertThat(actualKeyNames.length).isEqualTo(1);
        assertThat(actualKeyNames[0]).isEqualTo(expectedKeyName);
        assertThat(propertySource.getProperty(TEST_SLASH_KEY)).isNull();
        assertThat(propertySource.getProperty(expectedKeyName)).isEqualTo(TEST_SLASH_VALUE);
    }

    @Test
    public void initNullValidContentTypeTest() throws IOException {
        List<ConfigurationSetting> items = new ArrayList<>();
        items.add(ITEM_NULL);
        when(configurationListMock.iterator()).thenReturn(items.iterator())
            .thenReturn(Collections.emptyIterator());
        when(clientMock.listSettings(Mockito.any(), Mockito.anyBoolean())).thenReturn(configurationListMock);

        propertySource.initProperties(null, false);

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = items.stream()
            .map(t -> t.getKey().substring(KEY_FILTER.length())).toArray(String[]::new);

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }

    @Test
    public void jsonContentTypeWithInvalidJsonValueTest() {
        List<ConfigurationSetting> items = new ArrayList<>();
        items.add(ITEM_INVALID_JSON);
        when(configurationListMock.iterator()).thenReturn(items.iterator())
            .thenReturn(Collections.emptyIterator());
        when(clientMock.listSettings(Mockito.any(), Mockito.anyBoolean())).thenReturn(configurationListMock);

        assertThatThrownBy(() -> propertySource.initProperties(null, false))
            .isInstanceOf(InvalidConfigurationPropertyValueException.class)
            .hasMessageNotContaining(ITEM_INVALID_JSON.getValue());
    }
}
