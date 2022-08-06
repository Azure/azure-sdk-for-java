// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_KEY_1;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_KEY_2;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_KEY_3;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_LABEL_1;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_LABEL_2;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_LABEL_3;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_SLASH_KEY;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_SLASH_VALUE;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_VALUE_1;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_VALUE_2;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_VALUE_3;
import static com.azure.spring.cloud.config.implementation.TestUtils.createItem;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.data.appconfiguration.ConfigurationAsyncClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationProviderProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AppConfigurationApplicationSettingPropertySourceTest {

    private static final String EMPTY_CONTENT_TYPE = "";

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

    private static ObjectMapper mapper = new ObjectMapper();

    private List<ConfigurationSetting> testItems = new ArrayList<>();

    private AppConfigurationApplicationSettingPropertySource propertySource;

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

    private AppConfigurationProviderProperties appProperties;

    private KeyVaultCredentialProvider tokenCredentialProvider = null;

    @Mock
    private PagedIterable<ConfigurationSetting> pagedFluxMock;

    @BeforeAll
    public static void setup() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING, KEY_FILTER);
    }

    @BeforeEach
    public void init() {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

        MockitoAnnotations.openMocks(this);
        appConfigurationProperties = new AppConfigurationProperties();
        appProperties = new AppConfigurationProviderProperties();

        testItems = new ArrayList<ConfigurationSetting>();
        testItems.add(ITEM_1);
        testItems.add(ITEM_2);
        testItems.add(ITEM_3);

        when(configClientMock.listConfigurationSettings(Mockito.any())).thenReturn(settingsMock);
        when(settingsMock.byPage()).thenReturn(pageMock);
        when(pageMock.collectList()).thenReturn(collectionMock);
        when(collectionMock.block()).thenReturn(itemsMock);
        when(itemsMock.iterator()).thenReturn(itemsIteratorMock);
        when(itemsIteratorMock.next()).thenReturn(pagedResponseMock);

        String[] labelFilter = { "\0" };

        propertySource = new AppConfigurationApplicationSettingPropertySource(TEST_STORE_NAME, clientMock, KEY_FILTER, labelFilter,
            appConfigurationProperties, appProperties, tokenCredentialProvider, null, null);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testPropCanBeInitAndQueried() throws IOException {
        when(pagedFluxMock.iterator()).thenReturn(testItems.iterator());
        when(clientMock.listSettings(Mockito.any())).thenReturn(pagedFluxMock)
            .thenReturn(pagedFluxMock);

        propertySource.initProperties();

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
        List<ConfigurationSetting> settings = new ArrayList<ConfigurationSetting>();
        settings.add(slashedProp);
        when(pagedFluxMock.iterator()).thenReturn(settings.iterator())
            .thenReturn(new ArrayList<ConfigurationSetting>().iterator());
        when(clientMock.listSettings(Mockito.any())).thenReturn(pagedFluxMock)
            .thenReturn(pagedFluxMock);

        propertySource.initProperties();

        String expectedKeyName = TEST_SLASH_KEY.replace('/', '.');
        String[] actualKeyNames = propertySource.getPropertyNames();

        assertThat(actualKeyNames.length).isEqualTo(1);
        assertThat(actualKeyNames[0]).isEqualTo(expectedKeyName);
        assertThat(propertySource.getProperty(TEST_SLASH_KEY)).isNull();
        assertThat(propertySource.getProperty(expectedKeyName)).isEqualTo(TEST_SLASH_VALUE);
    }

    @Test
    public void initNullValidContentTypeTest() throws IOException {
        ArrayList<ConfigurationSetting> items = new ArrayList<ConfigurationSetting>();
        items.add(ITEM_NULL);
        when(pagedFluxMock.iterator()).thenReturn(items.iterator())
            .thenReturn(new ArrayList<ConfigurationSetting>().iterator());
        when(clientMock.listSettings(Mockito.any())).thenReturn(pagedFluxMock);

        propertySource.initProperties();

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = items.stream()
            .map(t -> t.getKey().substring(KEY_FILTER.length())).toArray(String[]::new);

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
    }
}
