// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.cert.Certificate;
import java.util.List;

import static com.azure.security.keyvault.jca.UriUtil.AAD_LOGIN_URI_CN;
import static com.azure.security.keyvault.jca.UriUtil.AAD_LOGIN_URI_DE;
import static com.azure.security.keyvault.jca.UriUtil.AAD_LOGIN_URI_GLOBAL;
import static com.azure.security.keyvault.jca.UriUtil.AAD_LOGIN_URI_US;
import static com.azure.security.keyvault.jca.UriUtil.KEY_VAULT_BASE_URI_CN;
import static com.azure.security.keyvault.jca.UriUtil.KEY_VAULT_BASE_URI_DE;
import static com.azure.security.keyvault.jca.UriUtil.KEY_VAULT_BASE_URI_GLOBAL;
import static com.azure.security.keyvault.jca.UriUtil.KEY_VAULT_BASE_URI_US;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class KeyVaultClientTest {

    private static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";
    private static final String KEY_VAULT_TEST_URI_CN = "https://fake.vault.azure.cn/";
    private static final String KEY_VAULT_TEST_URI_US = "https://fake.vault.usgovcloudapi.net/";
    private static final String KEY_VAULT_TEST_URI_DE = "https://fake.vault.microsoftazure.de/";

    private KeyVaultClient kvClient;

    /**
     * Test initialization of keyVaultBaseUri and aadAuthenticationUrl.
     *
     */
    @Test
    public void testInitializationOfGlobalURI() {
        kvClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL);
        Assertions.assertEquals(kvClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_GLOBAL);
        Assertions.assertEquals(kvClient.getAadAuthenticationUrl(), AAD_LOGIN_URI_GLOBAL);
    }

    @Test
    public void testInitializationOfCNURI() {
        kvClient = new KeyVaultClient(KEY_VAULT_TEST_URI_CN);
        Assertions.assertEquals(kvClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_CN);
        Assertions.assertEquals(kvClient.getAadAuthenticationUrl(), AAD_LOGIN_URI_CN);
    }

    @Test
    public void testInitializationOfUSURI() {
        kvClient = new KeyVaultClient(KEY_VAULT_TEST_URI_US);
        Assertions.assertEquals(kvClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_US);
        Assertions.assertEquals(kvClient.getAadAuthenticationUrl(), AAD_LOGIN_URI_US);
    }

    @Test
    public void testInitializationOfDEURI() {
        kvClient = new KeyVaultClient(KEY_VAULT_TEST_URI_DE);
        Assertions.assertEquals(kvClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_DE);
        Assertions.assertEquals(kvClient.getAadAuthenticationUrl(), AAD_LOGIN_URI_DE);
    }

    @Test
    @Disabled
    public void testGetAliases() {
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        KeyVaultClient keyVaultClient = new KeyVaultClient(
            keyVaultUri, System.getProperty("azure.keyvault.aad-authentication-url"),
            tenantId,
            clientId,
            clientSecret);
        List<String> result = keyVaultClient.getAliases();
        assertNotNull(result);
    }

    @Test
    @Disabled
    public void testGetCertificate() {
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        KeyVaultClient keyVaultClient = new KeyVaultClient(
            keyVaultUri, System.getProperty("azure.keyvault.aad-authentication-url"),
            tenantId,
            clientId,
            clientSecret);
        Certificate certificate = keyVaultClient.getCertificate("myalias");
        assertNotNull(certificate);
    }

    @Test
    @Disabled
    public void testGetKey() {
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        String keyVaultUri = System.getProperty("azure.keyvault.uri");
        KeyVaultClient keyVaultClient = new KeyVaultClient(
            keyVaultUri, System.getProperty("azure.keyvault.aad-authentication-url"), tenantId, clientId, clientSecret);
        assertNull(keyVaultClient.getKey("myalias", null));
    }
}
