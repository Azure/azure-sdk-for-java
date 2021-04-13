// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static com.azure.security.keyvault.jca.Constants.AAD_LOGIN_CN_URI;
import static com.azure.security.keyvault.jca.Constants.AAD_LOGIN_DE_URI;
import static com.azure.security.keyvault.jca.Constants.AAD_LOGIN_GLOBAL_URI;
import static com.azure.security.keyvault.jca.Constants.AAD_LOGIN_US_URI;
import static com.azure.security.keyvault.jca.Constants.KEY_VAULT_BASE_URI_CN;
import static com.azure.security.keyvault.jca.Constants.KEY_VAULT_BASE_URI_DE;
import static com.azure.security.keyvault.jca.Constants.KEY_VAULT_BASE_URI_GLOBAL;
import static com.azure.security.keyvault.jca.Constants.KEY_VAULT_BASE_URI_US;
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
        Assertions.assertEquals(kvClient.getAadAuthenticationUrl(), AAD_LOGIN_GLOBAL_URI);
    }

    @Test
    public void testInitializationOfCNURI() {
        kvClient = new KeyVaultClient(KEY_VAULT_TEST_URI_CN);
        Assertions.assertEquals(kvClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_CN);
        Assertions.assertEquals(kvClient.getAadAuthenticationUrl(), AAD_LOGIN_CN_URI);
    }

    @Test
    public void testInitializationOfUSURI() {
        kvClient = new KeyVaultClient(KEY_VAULT_TEST_URI_US);
        Assertions.assertEquals(kvClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_US);
        Assertions.assertEquals(kvClient.getAadAuthenticationUrl(), AAD_LOGIN_US_URI);
    }

    @Test
    public void testInitializationOfDEURI() {
        kvClient = new KeyVaultClient(KEY_VAULT_TEST_URI_DE);
        Assertions.assertEquals(kvClient.getKeyVaultBaseUri(), KEY_VAULT_BASE_URI_DE);
        Assertions.assertEquals(kvClient.getAadAuthenticationUrl(), AAD_LOGIN_DE_URI);
    }
}
