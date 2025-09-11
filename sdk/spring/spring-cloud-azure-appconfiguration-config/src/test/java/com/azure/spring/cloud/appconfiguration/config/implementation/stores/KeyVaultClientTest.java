// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.stores;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.appconfiguration.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.service.implementation.keyvault.secrets.SecretClientBuilderFactory;

import reactor.core.publisher.Mono;

public class KeyVaultClientTest {

    private AppConfigurationSecretClientManager clientStore;

    @Mock
    private SecretClientBuilder builderMock;

    @Mock
    private SecretAsyncClient clientMock;

    @Mock
    private TokenCredential credentialMock;

    @Mock
    private Mono<KeyVaultSecret> monoSecret;

    @Mock
    private SecretClientBuilderFactory secretClientBuilderFactoryMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void configProviderAuth() throws URISyntaxException {
        String keyVaultUri = "https://keyvault.vault.azure.net";

        clientStore = new AppConfigurationSecretClientManager(keyVaultUri, null, null, secretClientBuilderFactoryMock,
            false);

        AppConfigurationSecretClientManager test = Mockito.spy(clientStore);
        when(secretClientBuilderFactoryMock.build()).thenReturn(builderMock);

        when(builderMock.vaultUrl(Mockito.any())).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        test.build();

        when(clientMock.getSecret(Mockito.any(), Mockito.any()))
            .thenReturn(monoSecret);
        when(monoSecret.block(Mockito.any())).thenReturn(new KeyVaultSecret("", ""));

        assertNotNull(test.getSecret(new URI(keyVaultUri)));
        assertEquals(test.getSecret(new URI(keyVaultUri)).getName(), "");
    }

    @Test
    public void systemAssignedCredentials() throws URISyntaxException {
        String keyVaultUri = "https://keyvault.vault.azure.net/secrets/mySecret";

        clientStore = new AppConfigurationSecretClientManager(keyVaultUri, null, null, secretClientBuilderFactoryMock,
            false);

        AppConfigurationSecretClientManager test = Mockito.spy(clientStore);
        when(secretClientBuilderFactoryMock.build()).thenReturn(builderMock);

        when(builderMock.vaultUrl(Mockito.any())).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        test.build();

        when(clientMock.getSecret(Mockito.any(), Mockito.any()))
            .thenReturn(monoSecret);
        when(monoSecret.block(Mockito.any())).thenReturn(new KeyVaultSecret("", ""));

        assertNotNull(test.getSecret(new URI(keyVaultUri)));
        assertEquals(test.getSecret(new URI(keyVaultUri)).getName(), "");
    }

    @Test
    public void secretResolverTest() throws URISyntaxException {
        String keyVaultUri = "https://keyvault.vault.azure.net/secrets/mySecret";

        clientStore = new AppConfigurationSecretClientManager(keyVaultUri, null, new TestSecretResolver(),
            secretClientBuilderFactoryMock, false);

        AppConfigurationSecretClientManager test = Mockito.spy(clientStore);
        when(secretClientBuilderFactoryMock.build()).thenReturn(builderMock);

        when(builderMock.vaultUrl(Mockito.any())).thenReturn(builderMock);

        assertEquals("Test-Value", test.getSecret(new URI(keyVaultUri + "/testSecret")).getValue());
        assertEquals("Default-Secret", test.getSecret(new URI(keyVaultUri + "/testSecret2")).getValue());
    }

    class TestSecretResolver implements KeyVaultSecretProvider {

        @Override
        public String getSecret(String uri) {
            if (uri.endsWith("/testSecret")) {
                return "Test-Value";
            }
            return "Default-Secret";
        }

    }
}
