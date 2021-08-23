// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.stores;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.spring.cloud.config.KeyVaultCredentialProvider;
import com.azure.spring.cloud.config.KeyVaultSecretProvider;
import com.azure.spring.cloud.config.properties.AppConfigurationProperties;
import com.azure.spring.cloud.config.resource.AppConfigManagedIdentityProperties;

import reactor.core.publisher.Mono;

public class KeyVaultClientTest {

    static TokenCredential tokenCredential;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private KeyVaultClient clientStore;

    @Mock
    private SecretClientBuilder builderMock;

    @Mock
    private SecretAsyncClient clientMock;

    @Mock
    private TokenCredential credentialMock;

    @Mock
    private Mono<KeyVaultSecret> monoSecret;

    private AppConfigurationProperties azureProperties;

    @Test(expected = IllegalArgumentException.class)
    public void multipleArguments() throws IOException, URISyntaxException {
        azureProperties = new AppConfigurationProperties();
        AppConfigManagedIdentityProperties msiProps = new AppConfigManagedIdentityProperties();
        msiProps.setClientId("testclientid");
        azureProperties.setManagedIdentity(msiProps);

        String keyVaultUri = "https://keyvault.vault.azure.net/secrets/mySecret";

        KeyVaultCredentialProvider provider = new KeyVaultCredentialProvider() {

            @Override
            public TokenCredential getKeyVaultCredential(String uri) {
                assertEquals("https://keyvault.vault.azure.net", uri);
                return credentialMock;
            }
        };

        clientStore = new KeyVaultClient(azureProperties, new URI(keyVaultUri), provider, null, null);

        KeyVaultClient test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        test.build();
        fail();
    }

    @Test
    public void configProviderAuth() throws IOException, URISyntaxException {
        azureProperties = new AppConfigurationProperties();
        AppConfigManagedIdentityProperties msiProps = null;
        azureProperties.setManagedIdentity(msiProps);

        String keyVaultUri = "https://keyvault.vault.azure.net/secrets/mySecret";

        KeyVaultCredentialProvider provider = new KeyVaultCredentialProvider() {

            @Override
            public TokenCredential getKeyVaultCredential(String uri) {
                assertEquals("https://keyvault.vault.azure.net", uri);
                return credentialMock;
            }
        };

        clientStore = new KeyVaultClient(azureProperties, new URI(keyVaultUri), provider, null, null);

        KeyVaultClient test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.vaultUrl(Mockito.any())).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        test.build();

        when(clientMock.getSecret(Mockito.any(), Mockito.any()))
            .thenReturn(monoSecret);
        when(monoSecret.block(Mockito.any())).thenReturn(new KeyVaultSecret("", ""));

        assertNotNull(test.getSecret(new URI(keyVaultUri), 10));
        assertEquals(test.getSecret(new URI(keyVaultUri), 10).getName(), "");
    }

    @Test
    public void configClientIdAuth() throws IOException, URISyntaxException {
        azureProperties = new AppConfigurationProperties();
        AppConfigManagedIdentityProperties msiProps = new AppConfigManagedIdentityProperties();
        msiProps.setClientId("testClientId");
        AppConfigManagedIdentityProperties test2 = Mockito.spy(msiProps);
        azureProperties.setManagedIdentity(test2);

        String keyVaultUri = "https://keyvault.vault.azure.net/secrets/mySecret";

        clientStore = new KeyVaultClient(azureProperties, new URI(keyVaultUri), null, null, null);

        KeyVaultClient test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.vaultUrl(Mockito.any())).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        test.build();

        when(clientMock.getSecret(Mockito.any(), Mockito.any()))
            .thenReturn(monoSecret);
        when(monoSecret.block(Mockito.any())).thenReturn(new KeyVaultSecret("", ""));

        assertNotNull(test.getSecret(new URI(keyVaultUri), 10));
        assertEquals(test.getSecret(new URI(keyVaultUri), 10).getName(), "");

        verify(test2, times(2)).getClientId();
    }

    @Test
    public void systemAssignedCredentials() throws IOException, URISyntaxException {
        azureProperties = new AppConfigurationProperties();
        AppConfigManagedIdentityProperties msiProps = new AppConfigManagedIdentityProperties();
        msiProps.setClientId("");
        AppConfigManagedIdentityProperties test2 = Mockito.spy(msiProps);
        azureProperties.setManagedIdentity(test2);

        String keyVaultUri = "https://keyvault.vault.azure.net/secrets/mySecret";

        clientStore = new KeyVaultClient(azureProperties, new URI(keyVaultUri), null, null, null);

        KeyVaultClient test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.vaultUrl(Mockito.any())).thenReturn(builderMock);
        when(builderMock.buildAsyncClient()).thenReturn(clientMock);

        test.build();

        when(clientMock.getSecret(Mockito.any(), Mockito.any()))
            .thenReturn(monoSecret);
        when(monoSecret.block(Mockito.any())).thenReturn(new KeyVaultSecret("", ""));

        assertNotNull(test.getSecret(new URI(keyVaultUri), 10));
        assertEquals(test.getSecret(new URI(keyVaultUri), 10).getName(), "");

        verify(test2, times(1)).getClientId();
    }

    @Test
    public void secretResolverTest() throws URISyntaxException {
        azureProperties = new AppConfigurationProperties();

        String keyVaultUri = "https://keyvault.vault.azure.net/secrets/mySecret";

        clientStore = new KeyVaultClient(azureProperties, new URI(keyVaultUri), null, null, new TestSecretResolver());

        KeyVaultClient test = Mockito.spy(clientStore);
        Mockito.doReturn(builderMock).when(test).getBuilder();

        when(builderMock.vaultUrl(Mockito.any())).thenReturn(builderMock);

        assertEquals("Test-Value", test.getSecret(new URI(keyVaultUri + "/testSecret"), 10).getValue());
        assertEquals("Default-Secret", test.getSecret(new URI(keyVaultUri + "/testSecret2"), 10).getValue());
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
