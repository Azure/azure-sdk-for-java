// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.KEY_VAULT_CONTENT_TYPE;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_KEY_VAULT_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_LABEL_VAULT_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_URI_VAULT_1;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_URI_VAULT_2;
import static com.azure.spring.cloud.appconfiguration.config.implementation.TestUtils.createSecretReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.MockitoSession;
import org.mockito.quality.Strictness;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyValueException;

import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.stores.AppConfigurationSecretClientManager;

public class AppConfigurationPropertySourceKeyVaultTest {

    private static final AppConfigurationProperties TEST_PROPS = new AppConfigurationProperties();

    private static final String KEY_FILTER = "/foo/";

    private static final SecretReferenceConfigurationSetting KEY_VAULT_ITEM = createSecretReference(KEY_FILTER,
        TEST_KEY_VAULT_1, TEST_URI_VAULT_1, TEST_LABEL_VAULT_1, KEY_VAULT_CONTENT_TYPE);

    private static final SecretReferenceConfigurationSetting KEY_VAULT_ITEM_INVALID_URI = createSecretReference(
        KEY_FILTER,
        TEST_KEY_VAULT_1, TEST_URI_VAULT_2, TEST_LABEL_VAULT_1, KEY_VAULT_CONTENT_TYPE);

    private AppConfigurationApplicationSettingPropertySource propertySource;

    @Mock
    private SecretClientBuilder builderMock;

    @Mock
    private AppConfigurationKeyVaultClientFactory keyVaultClientFactoryMock;

    @Mock
    private AppConfigurationSecretClientManager clientManagerMock;

    @Mock
    private AppConfigurationReplicaClient replicaClientMock;

    @Mock
    private SecretAsyncClient clientMock;

    @Mock
    private List<ConfigurationSetting> keyVaultSecretListMock;

    private MockitoSession session;

    @BeforeEach
    public void init() {
        session = Mockito.mockitoSession().initMocks(this).strictness(Strictness.STRICT_STUBS).startMocking();
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING, KEY_FILTER);

        KEY_VAULT_ITEM.setContentType(KEY_VAULT_CONTENT_TYPE);

        MockitoAnnotations.openMocks(this);

        String[] labelFilter = { "\0" };
        propertySource = new AppConfigurationApplicationSettingPropertySource(TEST_STORE_NAME, replicaClientMock,
            keyVaultClientFactoryMock, KEY_FILTER, labelFilter);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        session.finishMocking();
    }

    @Test
    public void testKeyVaultTest() {
        List<ConfigurationSetting> settings = List.of(KEY_VAULT_ITEM);
        when(keyVaultSecretListMock.iterator()).thenReturn(settings.iterator())
            .thenReturn(Collections.emptyIterator());
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(keyVaultSecretListMock)
            .thenReturn(keyVaultSecretListMock);

        KeyVaultSecret secret = new KeyVaultSecret("mySecret", "mySecretValue");
        when(keyVaultClientFactoryMock.getClient(Mockito.eq("https://test.key.vault.com"))).thenReturn(clientManagerMock);
        when(clientManagerMock.getSecret(Mockito.any(URI.class))).thenReturn(secret);

        try {
            propertySource.initProperties(null);
        } catch (InvalidConfigurationPropertyValueException e) {
            fail("Failed Reading in Feature Flags");
        }

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = settings.stream()
            .map(t -> t.getKey().substring(KEY_FILTER.length())).toArray(String[]::new);

        assertThat(keyNames).containsExactlyInAnyOrder(expectedKeyNames);
        assertThat(propertySource.getProperty(TEST_KEY_VAULT_1)).isEqualTo("mySecretValue");
    }

    @Test
    public void invalidKeyVaultReferenceInvalidURITest() {
        List<ConfigurationSetting> settings = List.of(KEY_VAULT_ITEM_INVALID_URI);
        when(keyVaultSecretListMock.iterator()).thenReturn(settings.iterator())
            .thenReturn(Collections.emptyIterator());
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(keyVaultSecretListMock)
            .thenReturn(keyVaultSecretListMock);

        InvalidConfigurationPropertyValueException exception = assertThrows(
            InvalidConfigurationPropertyValueException.class, () -> propertySource.initProperties(null));
        assertEquals("test_key_vault_1", exception.getName());
        assertEquals("<Redacted>", exception.getValue());
        assertEquals("Invalid URI found in JSON property field 'uri' unable to parse.", exception.getReason());
    }

    @Test
    public void invalidKeyVaultReferenceParseErrorTest() {
        List<ConfigurationSetting> settings = List.of(KEY_VAULT_ITEM);
        when(keyVaultSecretListMock.iterator()).thenReturn(settings.iterator())
            .thenReturn(Collections.emptyIterator());
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(keyVaultSecretListMock)
            .thenReturn(keyVaultSecretListMock);
        when(keyVaultClientFactoryMock.getClient(Mockito.eq("https://test.key.vault.com"))).thenReturn(clientManagerMock);
        when(clientManagerMock.getSecret(Mockito.any())).thenThrow(new RuntimeException("Parse Failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> propertySource.initProperties(null));
        assertEquals("Parse Failed", exception.getMessage());
    }
}
