// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

<<<<<<<< HEAD:sdk/appconfiguration/spring-cloud-azure-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java
import static com.azure.spring.cloud.config.implementation.AppConfigurationConstants.KEY_VAULT_CONTENT_TYPE;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_CONN_STRING;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_KEY_1;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_KEY_2;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_KEY_3;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_KEY_VAULT_1;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_LABEL_1;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_LABEL_2;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_LABEL_3;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_LABEL_VAULT_1;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_STORE_NAME;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_URI_VAULT_1;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_VALUE_1;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_VALUE_2;
import static com.azure.spring.cloud.config.implementation.TestConstants.TEST_VALUE_3;
========
import static com.azure.spring.cloud.config.AppConfigurationConstants.KEY_VAULT_CONTENT_TYPE;
import static com.azure.spring.cloud.config.TestConstants.TEST_CONN_STRING;
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
>>>>>>>> c595c31b45e92273feaec522e5d53130d7537677:sdk/appconfiguration/azure-spring-cloud-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java
import static com.azure.spring.cloud.config.implementation.TestUtils.createItem;
import static com.azure.spring.cloud.config.implementation.TestUtils.createSecretReference;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
import com.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.azure.spring.cloud.config.KeyVaultSecretProvider;
<<<<<<<< HEAD:sdk/appconfiguration/spring-cloud-azure-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.implementation.properties.AppConfigurationProviderProperties;
========
import com.azure.spring.cloud.config.feature.management.entity.FeatureSet;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties;
import com.azure.spring.cloud.config.properties.AppConfigurationStoreSelects;
import com.azure.spring.cloud.config.properties.ConfigStore;
>>>>>>>> c595c31b45e92273feaec522e5d53130d7537677:sdk/appconfiguration/azure-spring-cloud-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java

import reactor.core.publisher.Mono;

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

    private AppConfigurationProperties appConfigurationProperties;

    private AppConfigurationProviderProperties appProperties;

    @Mock
    private SecretClientBuilder builderMock;

    @Mock
    private AppConfigurationReplicaClient replicaClientMock;

    @Mock
    private SecretAsyncClient clientMock;

    @Mock
    private List<ConfigurationSetting> configurationListMock;

    private KeyVaultCredentialProvider tokenCredentialProvider = null;

    @BeforeEach
    public void init() {
        TestUtils.addStore(TEST_PROPS, TEST_STORE_NAME, TEST_CONN_STRING, KEY_FILTER);

        KEY_VAULT_ITEM.setContentType(KEY_VAULT_CONTENT_TYPE);

        MockitoAnnotations.openMocks(this);
        appConfigurationProperties = new AppConfigurationProperties();
        appProperties = new AppConfigurationProviderProperties();
        appProperties.setMaxRetryTime(0);
<<<<<<<< HEAD:sdk/appconfiguration/spring-cloud-azure-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java

        String[] labelFilter = {"\0"};
        propertySource = new AppConfigurationApplicationSettingPropertySource(TEST_STORE_NAME, replicaClientMock, KEY_FILTER, labelFilter,
            appConfigurationProperties, appProperties, tokenCredentialProvider, null,
========
        ConfigStore testStore = new ConfigStore();
        testStore.setEndpoint(TEST_STORE_NAME);
        AppConfigurationStoreSelects selects = new AppConfigurationStoreSelects().setKeyFilter(KEY_FILTER)
            .setLabelFilter("\0");
        propertySource = new AppConfigurationPropertySource(testStore, selects, new ArrayList<>(),
            appConfigurationProperties, replicaClientMock, appProperties, tokenCredentialProvider, null,
>>>>>>>> c595c31b45e92273feaec522e5d53130d7537677:sdk/appconfiguration/azure-spring-cloud-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java
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
    public void testKeyVaultTest() throws AppConfigurationStatusException, IOException {
        TEST_ITEMS.add(KEY_VAULT_ITEM);
<<<<<<<< HEAD:sdk/appconfiguration/spring-cloud-azure-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java
        when(pagedFluxMock.iterator()).thenReturn(TEST_ITEMS.iterator())
            .thenReturn(new ArrayList<ConfigurationSetting>().iterator());
        when(replicaClientMock.listSettings(Mockito.any())).thenReturn(pagedFluxMock).thenReturn(pagedFluxMock);
========
        when(configurationListMock.iterator()).thenReturn(TEST_ITEMS.iterator())
            .thenReturn(Collections.emptyIterator());
        when(replicaClientMock.listConfigurationSettings(Mockito.any())).thenReturn(configurationListMock).thenReturn(configurationListMock);
>>>>>>>> c595c31b45e92273feaec522e5d53130d7537677:sdk/appconfiguration/azure-spring-cloud-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java

        Mockito.when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        KeyVaultSecret secret = new KeyVaultSecret("mySecret", "mySecretValue");
        when(clientMock.getSecret(Mockito.anyString(), Mockito.anyString())).thenReturn(Mono.just(secret));

<<<<<<<< HEAD:sdk/appconfiguration/spring-cloud-azure-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java
        try {
            propertySource.initProperties();
        } catch (IOException e) {
            fail("Failed Reading in Feature Flags");
        }
========
        FeatureSet featureSet = new FeatureSet();

        propertySource.initProperties(featureSet);
>>>>>>>> c595c31b45e92273feaec522e5d53130d7537677:sdk/appconfiguration/azure-spring-cloud-appconfiguration-config/src/test/java/com/azure/spring/cloud/config/implementation/AppConfigurationPropertySourceKeyVaultTest.java

        String[] keyNames = propertySource.getPropertyNames();
        String[] expectedKeyNames = TEST_ITEMS.stream()
            .map(t -> t.getKey().substring(KEY_FILTER.length())).toArray(String[]::new);

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
