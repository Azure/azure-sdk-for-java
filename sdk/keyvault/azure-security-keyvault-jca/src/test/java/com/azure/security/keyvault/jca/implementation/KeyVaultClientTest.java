// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import com.azure.security.keyvault.jca.implementation.model.CertificateItem;
import com.azure.security.keyvault.jca.implementation.model.CertificateListResult;
import com.azure.security.keyvault.jca.implementation.utils.HttpUtil;
import com.azure.security.keyvault.jca.implementation.utils.JsonConverterUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;

import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.AAD_LOGIN_URI_CN;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.AAD_LOGIN_URI_DE;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.AAD_LOGIN_URI_GLOBAL;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.AAD_LOGIN_URI_US;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.KEY_VAULT_BASE_URI_CN;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.KEY_VAULT_BASE_URI_DE;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.KEY_VAULT_BASE_URI_GLOBAL;
import static com.azure.security.keyvault.jca.implementation.KeyVaultClient.KEY_VAULT_BASE_URI_US;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;

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
    public void testGetAliasWithCertificateInfoWith0Page() {
        try (MockedStatic<HttpUtil> utilities = Mockito.mockStatic(HttpUtil.class)) {
            utilities.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn("fakeValue");
            KeyVaultClient keyVaultClient = mock(KeyVaultClient.class);
            List<String> result = keyVaultClient.getAliases();
            assertEquals(result.size(), 0);
        }
    }

    @Test
    public void testGetAliasWithCertificateInfoWith1Page() {
        try (MockedStatic<HttpUtil> utilities = Mockito.mockStatic(HttpUtil.class)) {
            // create fake certificates
            CertificateItem fakeCertificateItem1 = new CertificateItem();
            fakeCertificateItem1.setId("certificates/fakeCertificateItem1");
            CertificateListResult certificateListResult = new CertificateListResult();
            certificateListResult.setValue(Arrays.asList(fakeCertificateItem1));
            String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);
            utilities.when(() -> HttpUtil.get(notNull(), anyMap())).thenReturn(certificateListResultString);
            KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null);
            List<String> result = keyVaultClient.getAliases();
            assertEquals(result.size(), 1);
            assertTrue(result.contains("fakeCertificateItem1"));
        }
    }

    @Test
    public void testGetAliasWithCertificateInfoWith2Pages() {
        try (MockedStatic<HttpUtil> utilities = Mockito.mockStatic(HttpUtil.class)) {
            // create fake certificates
            CertificateItem fakeCertificateItem1 = new CertificateItem();
            fakeCertificateItem1.setId("certificates/fakeCertificateItem1");
            CertificateItem fakeCertificateItem2 = new CertificateItem();
            fakeCertificateItem2.setId("certificates/fakeCertificateItem2");
            CertificateItem fakeCertificateItem3 = new CertificateItem();
            fakeCertificateItem3.setId("certificates/fakeCertificateItem3");

            // create first page certificate result
            CertificateListResult certificateListResult = new CertificateListResult();
            certificateListResult.setNextLink("fakeNextLint");
            certificateListResult.setValue(Arrays.asList(fakeCertificateItem1));
            // create next page certificate result
            CertificateListResult certificateListResultNext = new CertificateListResult();
            certificateListResultNext.setValue(Arrays.asList(fakeCertificateItem2, fakeCertificateItem3));

            String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);
            String certificateListResultStringNext = JsonConverterUtil.toJson(certificateListResultNext);

            utilities.when(() -> HttpUtil.get(notNull(), anyMap())).thenReturn(certificateListResultString);
            utilities.when(() -> HttpUtil.get(eq("fakeNextLint"), anyMap())).thenReturn(certificateListResultStringNext);

            KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null);
            List<String> result = keyVaultClient.getAliases();
            assertEquals(result.size(), 3);
            assertTrue(result.containsAll(Arrays.asList("fakeCertificateItem1", "fakeCertificateItem2", "fakeCertificateItem3")));
        }
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
