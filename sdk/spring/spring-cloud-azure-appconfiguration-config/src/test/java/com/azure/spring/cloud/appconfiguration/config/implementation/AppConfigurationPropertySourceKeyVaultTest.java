// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.KEY_VAULT_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_KEY_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_KEY_2;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_KEY_3;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_KEY_VAULT_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_LABEL_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_LABEL_2;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_LABEL_3;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_LABEL_VAULT_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_URI_VAULT_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_VALUE_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_VALUE_2;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_VALUE_3;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestUtils.createItem;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestUtils.createSecretReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.stores.AppConfigurationSecretClientManager;

public class AppConfigurationPropertySourceKeyVaultTest {

    public static final List<ConfigurationSetting> TEST_ITEMS = new ArrayList<>();

    private static final String EMPTY_CONTENT_TYPE = "";

    private static final AppConfigurationProperties TEST_PROPS = new AppConfigurationProperties();

    private static final String KEY_FILTER = "/foo/";

    private static final ConfigurationSetting ITEM_1 = createItem(KEY_FILTER, TEST_KEY_1, TEST_VALUE_1, TEST_LABEL_1,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_2 = createItem(KEY_FILTER, TEST_KEY_2, TEST_VALUE_2, TEST_LABEL_2,
        EMPTY_CONTENT_TYPE);

    private static final ConfigurationSetting ITEM_3 = createItem(KEY_FILTER, TEST_KEY_3, TEST_VALUE_3, TEST_LABEL_3,
        EMPTY_CONTENT_TYPE);

    private static final SecretReferenceConfigurationSetting KEY_VAULT_ITEM = createSecretReference(KEY_FILTER,
        TEST_KEY_VAULT_1,
        TEST_URI_VAULT_1, TEST_LABEL_VAULT_1, KEY_VAULT_CONTENT_TYPE);

    private AppConfigurationApplicationSettingPropertySource propertySource;

    @Mock
    private SecretClientBuilder builderMock;

    @Mock
    private AppConfigurationKeyVaultClientFactory keyVaultClientFactory;

    @Mock
    private AppConfigurationSecretClientManager clientManagerMock;

    @Mock
    private AppConfigurationReplicaClient replicaClientMock;

    @Mock
    private SecretAsyncClient clientMock;

    @Mock
    private List<ConfigurationSetting> keyVaultSecretListMock;

    @BeforeEach
    public void init() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING, KEY_FILTER);

        KEY_VAULT_ITEM.setContentType(KEY_VAULT_CONTENT_TYPE);

        MockitoAnnotations.openMocks(this);

        String[] labelFilter = { "\0" };
        propertySource = new AppConfigurationApplicationSettingPropertySource(TEST_STORE_NAME, replicaClientMock,
            keyVaultClientFactory, KEY_FILTER, labelFilter, 60);

        TEST_ITEMS.add(ITEM_1);
        TEST_ITEMS.add(ITEM_2);
        TEST_ITEMS.add(ITEM_3);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void testKeyVaultTest() {
        TEST_ITEMS.add(KEY_VAULT_ITEM);
        when(keyVaultSecretListMock.iterator()).thenReturn(TEST_ITEMS.iterator())
            .thenReturn(Collections.emptyIterator());
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(keyVaultSecretListMock)
            .thenReturn(keyVaultSecretListMock);

        Mockito.when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        KeyVaultSecret secret = new KeyVaultSecret("mySecret", "mySecretValue");
        when(keyVaultClientFactory.getClient(Mockito.eq("https://test.key.vault.com"))).thenReturn(clientManagerMock);
        when(clientManagerMock.getSecret(Mockito.any(URI.class), Mockito.anyInt())).thenReturn(secret);

        try {
            propertySource.initProperties();
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = TEST_ITEMS.stream()
            .map(t -> t.getKey().substring(KEY_FILTER.length())).toArray(String[]::new);

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);

        assertThat(propertySource.getProperty(TEST_KEY_1)).isEqualTo(TEST_VALUE_1);
        assertThat(propertySource.getProperty(TEST_KEY_2)).isEqualTo(TEST_VALUE_2);
        assertThat(propertySource.getProperty(TEST_KEY_3)).isEqualTo(TEST_VALUE_3);
        assertThat(propertySource.getProperty(TEST_KEY_VAULT_1)).isEqualTo("mySecretValue");
    }
}
