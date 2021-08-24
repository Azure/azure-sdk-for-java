// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

import static com.azure.spring.cloud.config.Constants.KEY_VAULT_CONTENT_TYPE;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONTEXT;
import static com.azure.spring.cloud.config.TestConstants.TEST_KEY_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_KEY_2;
import static com.azure.spring.cloud.config.TestConstants.TEST_KEY_3;
import static com.azure.spring.cloud.config.TestConstants.TEST_KEY_VAULT_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_LABEL_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_LABEL_2;
import static com.azure.spring.cloud.config.TestConstants.TEST_LABEL_3;
import static com.azure.spring.cloud.config.TestConstants.TEST_LABEL_VAULT_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.config.TestConstants.TEST_URI_VAULT_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_VALUE_1;
import static com.azure.spring.cloud.config.TestConstants.TEST_VALUE_2;
import static com.azure.spring.cloud.config.TestConstants.TEST_VALUE_3;
import static com.azure.spring.cloud.config.TestUtils.createItem;
import static com.azure.spring.cloud.config.TestUtils.createSecretReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.ConfigStore;
import com.azure.spring.cloud.config.stores.ClientStore;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AppConfigurationPropertySourceKeyVaultTest {

    public static final List<ConfigurationSetting> TEST_ITEMS = new ArrayList<>();

    public static final List<ConfigurationSetting> FEATURE_ITEMS = new ArrayList<>();

    private static final String EMPTY_CONTENT_TYPE = "";

    private static final AppConfigurationProperties TEST_PROPS = new AppConfigurationProperties();

    private static final ConfigurationSetting ITEM_1 = createItem(TEST_CONTEXT, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_2 = createItem(TEST_CONTEXT, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_3 = createItem(TEST_CONTEXT, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3,
        EMPTY_CONTENT_TYPE);

    private static final SecretReferenceConfigurationSetting KEY_VAULT_ITEM = createSecretReference(TEST_CONTEXT,
        TEST_KEY_VAULT_1,
        TEST_URI_VAULT_1, TEST_LABEL_VAULT_1, KEY_VAULT_CONTENT_TYPE);

    private AppConfigurationPropertySource propertySource;

    private AppConfigurationProperties appConfigurationProperties;

    private AppConfigurationProviderProperties appProperties;

    @Mock
    private ClientStore clientStoreMock;

    @Mock
    private SecretClientBuilder builderMock;

    @Mock
    private SecretAsyncClient clientMock;

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

    private KeyVaultCredentialProvider tokenCredentialProvider = null;

    @BeforeEach
    public void init() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING);

        KEY_VAULT_ITEM.setContentType(KEY_VAULT_CONTENT_TYPE);

        MockitoAnnotations.openMocks(this);
        appConfigurationProperties = new AppConfigurationProperties();
        appProperties = new AppConfigurationProviderProperties();
        appProperties.setMaxRetryTime(0);
        ConfigStore testStore = new ConfigStore();
        testStore.setEndpoint(TEST_STORE_NAME);
        ArrayList<String> contexts = new ArrayList<String>();
        contexts.add("/application/*");
        AppConfigurationStoreSelects selects = new AppConfigurationStoreSelects().setKeyFilter("/foo/")
            .setLabelFilter("\0");
        propertySource = new AppConfigurationPropertySource(TEST_CONTEXT, testStore, selects, new ArrayList<>(),
            appConfigurationProperties, clientStoreMock, appProperties, tokenCredentialProvider, null,
            new TestClient());

        TEST_ITEMS.add(ITEM_1);
        TEST_ITEMS.add(ITEM_2);
        TEST_ITEMS.add(ITEM_3);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testKeyVaultTest() throws Exception {
        TEST_ITEMS.add(KEY_VAULT_ITEM);
        when(clientStoreMock.listSettings(Mockito.any(), Mockito.anyString())).thenReturn(TEST_ITEMS)
            .thenReturn(new ArrayList<ConfigurationSetting>());

        Mockito.when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        KeyVaultSecret secret = new KeyVaultSecret("mySecret", "mySecretValue");
        when(clientMock.getSecret(Mockito.anyString(), Mockito.anyString())).thenReturn(Mono.just(secret));

        FeatureSet featureSet = new FeatureSet();

        try {
            propertySource.initProperties(featureSet);
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = TEST_ITEMS.stream()
            .map(t -> t.getKey().substring(TEST_CONTEXT.length())).toArray(String[]::new);

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
        assertThat(propertySource.getProperty(TEST_KEY_VAULT_1)).isEqualTo("mySecretValue");
    }

    class TestClient implements KeyVaultSecretProvider {

        @Override
        public String getSecret(String uri) {
            return "mySecretValue";
        }

    }
}
