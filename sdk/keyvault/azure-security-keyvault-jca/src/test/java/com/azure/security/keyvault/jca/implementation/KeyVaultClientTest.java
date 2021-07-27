// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.cert.Certificate;
import java.util.List;

import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.AAD_LOGIN_URI_CN;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.AAD_LOGIN_URI_DE;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.AAD_LOGIN_URI_GLOBAL;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.AAD_LOGIN_URI_US;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.KEY_VAULT_BASE_URI_CN;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.KEY_VAULT_BASE_URI_DE;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.KEY_VAULT_BASE_URI_GLOBAL;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.KEY_VAULT_BASE_URI_US;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class KeyVaultClientTest {

    private static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";
    private static final String KEY_VAULT_TEST_URI_CN = "https://fake.vault.azure.cn/";
    private static final String KEY_VAULT_TEST_URI_US = "https://fake.vault.usgovcloudapi.net/";
    private static final String KEY_VAULT_TEST_URI_DE = "https://fake.vault.microsoftazure.de/";

    private KeyVaultClient keyVaultClient;

    /**
     * Test initialization of keyVaultBaseUri and aadAuthenticationUrl.
     */
    @Test
    public void testInitializationOfGlobalURI() {
        keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null);
        Assertions.assertEquals(keyVaultClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_GLOBAL);
        Assertions.assertEquals(keyVaultClient.getAadAuthenticationUrl(), AAD_LOGIN_URI_GLOBAL);
    }

    @Test
    public void testInitializationOfCNURI() {
        keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_CN, null);
        Assertions.assertEquals(keyVaultClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_CN);
        Assertions.assertEquals(keyVaultClient.getAadAuthenticationUrl(), AAD_LOGIN_URI_CN);
    }

    @Test
    public void testInitializationOfUSURI() {
        keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_US, null);
        Assertions.assertEquals(keyVaultClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_US);
        Assertions.assertEquals(keyVaultClient.getAadAuthenticationUrl(), AAD_LOGIN_URI_US);
    }

    @Test
    public void testInitializationOfDEURI() {
        keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_DE, null);
        Assertions.assertEquals(keyVaultClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_DE);
        Assertions.assertEquals(keyVaultClient.getAadAuthenticationUrl(), AAD_LOGIN_URI_DE);
    }

    @Test
    @Disabled
    public void testGetAliases() {
        List<String> result = getKeyVaultClient().getAliases();
        assertNotNull(result);
    }

    @Test
    @Disabled
    public void testGetCertificate() {
        Certificate certificate = getKeyVaultClient().getCertificate("myalias");
        assertNotNull(certificate);
    }

    @Test
    @Disabled
    public void testGetKey() {
        assertNull(getKeyVaultClient().getKey("myalias", null));
    }

    private KeyVaultClient getKeyVaultClient() {
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        return new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret);
    }
}
