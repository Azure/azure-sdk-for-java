// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import com.azure.security.keyvault.jca.PropertyConvertorUtils;
import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.model.CertificateItem;
import com.azure.security.keyvault.jca.implementation.model.CertificateListResult;
import com.azure.security.keyvault.jca.implementation.utils.AccessTokenUtil;
import com.azure.security.keyvault.jca.implementation.utils.HttpUtil;
import com.azure.security.keyvault.jca.implementation.utils.JsonConverterUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class KeyVaultClientTest {
    private static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";

    @Test
    public void testGetAliasWithCertificateInfoWith0Page() {
        try (MockedStatic<HttpUtil> utilities = Mockito.mockStatic(HttpUtil.class)) {
            utilities.when(() -> HttpUtil.get(anyString(), anyMap())).thenReturn("fakeValue");

            KeyVaultClient keyVaultClient = mock(KeyVaultClient.class);
            List<String> result = keyVaultClient.getAliases();

            assertEquals(0, result.size());
        }
    }

    @Test
    public void testGetAliasWithCertificateInfoWith1Page() {
        try (MockedStatic<HttpUtil> utilities = Mockito.mockStatic(HttpUtil.class)) {
            utilities.when(() -> HttpUtil.validateUri(anyString(), anyString())).thenCallRealMethod();
            utilities.when(() -> HttpUtil.addTrailingSlashIfRequired(anyString())).thenCallRealMethod();

            // Create fake certificates.
            CertificateItem fakeCertificateItem1 = new CertificateItem();
            fakeCertificateItem1.setId("certificates/fakeCertificateItem1");

            CertificateListResult certificateListResult = new CertificateListResult();
            certificateListResult.setValue(Arrays.asList(fakeCertificateItem1));

            String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

            utilities.when(() -> HttpUtil.get(notNull(), anyMap())).thenReturn(certificateListResultString);

            KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null);
            List<String> result = keyVaultClient.getAliases();

            assertEquals(1, result.size());
            assertTrue(result.contains("fakeCertificateItem1"));
        }
    }

    @Test
    public void testGetAliasWithCertificateInfoWith2Pages() {
        try (MockedStatic<HttpUtil> utilities = Mockito.mockStatic(HttpUtil.class)) {
            utilities.when(() -> HttpUtil.validateUri(anyString(), anyString())).thenCallRealMethod();
            utilities.when(() -> HttpUtil.addTrailingSlashIfRequired(anyString())).thenCallRealMethod();

            // create fake certificates
            CertificateItem fakeCertificateItem1 = new CertificateItem();
            fakeCertificateItem1.setId("certificates/fakeCertificateItem1");

            CertificateItem fakeCertificateItem2 = new CertificateItem();
            fakeCertificateItem2.setId("certificates/fakeCertificateItem2");

            CertificateItem fakeCertificateItem3 = new CertificateItem();
            fakeCertificateItem3.setId("certificates/fakeCertificateItem3");

            // Create first page certificate result.
            CertificateListResult certificateListResult = new CertificateListResult();
            certificateListResult.setNextLink("fakeNextLint");
            certificateListResult.setValue(Arrays.asList(fakeCertificateItem1));

            // Create next page certificate result.
            CertificateListResult certificateListResultNext = new CertificateListResult();
            certificateListResultNext.setValue(Arrays.asList(fakeCertificateItem2, fakeCertificateItem3));

            String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);
            String certificateListResultStringNext = JsonConverterUtil.toJson(certificateListResultNext);

            utilities.when(() -> HttpUtil.get(notNull(), anyMap())).thenReturn(certificateListResultString);
            utilities.when(() -> HttpUtil.get(eq("fakeNextLint"), anyMap()))
                .thenReturn(certificateListResultStringNext);

            KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null);
            List<String> result = keyVaultClient.getAliases();

            assertEquals(3, result.size());
            assertTrue(result
                .containsAll(Arrays.asList("fakeCertificateItem1", "fakeCertificateItem2", "fakeCertificateItem3")));
        }
    }

    @Test
    public void testCacheToken() {
        try (MockedStatic<AccessTokenUtil> tokenUtilMockedStatic = Mockito.mockStatic(AccessTokenUtil.class);
            MockedStatic<HttpUtil> httpUtilMockedStatic = Mockito.mockStatic(HttpUtil.class)) {

            httpUtilMockedStatic.when(() -> HttpUtil.validateUri(anyString(), anyString())).thenCallRealMethod();
            httpUtilMockedStatic.when(() -> HttpUtil.addTrailingSlashIfRequired(anyString())).thenCallRealMethod();

            AccessToken cacheToken = new AccessToken();
            cacheToken.setExpiresIn(300); // 300 seconds.

            tokenUtilMockedStatic.when(() -> AccessTokenUtil.getAccessToken(anyString(), anyString()))
                .thenReturn(cacheToken);

            CertificateItem fakeCertificateItem = new CertificateItem();
            fakeCertificateItem.setId("certificates/fakeCertificateItem");

            CertificateListResult certificateListResult = new CertificateListResult();
            certificateListResult.setValue(Arrays.asList(fakeCertificateItem));

            String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);
            httpUtilMockedStatic.when(() -> HttpUtil.get(anyString(), anyMap()))
                .thenReturn(certificateListResultString);

            KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, "");
            keyVaultClient.getAliases();
            keyVaultClient.getAliases(); // Get aliases the second time.

            tokenUtilMockedStatic.verify(() -> AccessTokenUtil.getAccessToken(anyString(), anyString()), times(1));
        }
    }

    @Test
    public void testCacheTokenExpired() {
        try (MockedStatic<AccessTokenUtil> tokenUtilMockedStatic = Mockito.mockStatic(AccessTokenUtil.class);
            MockedStatic<HttpUtil> httpUtilMockedStatic = Mockito.mockStatic(HttpUtil.class)) {

            httpUtilMockedStatic.when(() -> HttpUtil.validateUri(anyString(), anyString())).thenCallRealMethod();
            httpUtilMockedStatic.when(() -> HttpUtil.addTrailingSlashIfRequired(anyString())).thenCallRealMethod();

            AccessToken cacheToken = new AccessToken();
            cacheToken.setExpiresIn(50); // 50 seconds.

            tokenUtilMockedStatic.when(() -> AccessTokenUtil.getAccessToken(anyString(), anyString()))
                .thenReturn(cacheToken);

            CertificateItem fakeCertificateItem = new CertificateItem();
            fakeCertificateItem.setId("certificates/fakeCertificateItem");

            CertificateListResult certificateListResult = new CertificateListResult();
            certificateListResult.setValue(Arrays.asList(fakeCertificateItem));

            String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);
            httpUtilMockedStatic.when(() -> HttpUtil.get(anyString(), anyMap()))
                .thenReturn(certificateListResultString);

            KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, "");
            keyVaultClient.getAliases();
            keyVaultClient.getAliases(); // Get aliases the second time.

            tokenUtilMockedStatic.verify(() -> AccessTokenUtil.getAccessToken(anyString(), anyString()), times(2));
        }
    }

    @Test
    public void testAccessTokenAuthentication() {
        try (MockedStatic<HttpUtil> httpUtilMockedStatic = Mockito.mockStatic(HttpUtil.class)) {
            httpUtilMockedStatic.when(() -> HttpUtil.validateUri(anyString(), anyString())).thenCallRealMethod();
            httpUtilMockedStatic.when(() -> HttpUtil.addTrailingSlashIfRequired(anyString())).thenCallRealMethod();

            CertificateItem fakeCertificateItem = new CertificateItem();
            fakeCertificateItem.setId("certificates/fakeCertificateItem");

            CertificateListResult certificateListResult = new CertificateListResult();
            certificateListResult.setValue(Arrays.asList(fakeCertificateItem));

            String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);
            httpUtilMockedStatic.when(() -> HttpUtil.get(anyString(), anyMap()))
                .thenReturn(certificateListResultString);

            // Create client with access token
            String testAccessToken = "test-bearer-token-12345";
            KeyVaultClient keyVaultClient
                = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null, null, null, null, testAccessToken, false);

            List<String> result = keyVaultClient.getAliases();

            // Verify that the access token was used
            assertEquals(1, result.size());
            assertTrue(result.contains("fakeCertificateItem"));
        }
    }

    @Test
    public void testAuthenticationPriority() {
        try (MockedStatic<HttpUtil> httpUtilMockedStatic = Mockito.mockStatic(HttpUtil.class);
            MockedStatic<AccessTokenUtil> tokenUtilMockedStatic = Mockito.mockStatic(AccessTokenUtil.class)) {

            httpUtilMockedStatic.when(() -> HttpUtil.validateUri(anyString(), anyString())).thenCallRealMethod();
            httpUtilMockedStatic.when(() -> HttpUtil.addTrailingSlashIfRequired(anyString())).thenCallRealMethod();

            AccessToken accessToken = new AccessToken("fake-token", 3600);
            tokenUtilMockedStatic.when(() -> AccessTokenUtil.getAccessToken(anyString(), anyString()))
                .thenReturn(accessToken);

            CertificateItem fakeCertificateItem = new CertificateItem();
            fakeCertificateItem.setId("certificates/fakeCertificateItem");

            CertificateListResult certificateListResult = new CertificateListResult();
            certificateListResult.setValue(Arrays.asList(fakeCertificateItem));

            String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);
            httpUtilMockedStatic.when(() -> HttpUtil.get(anyString(), anyMap()))
                .thenReturn(certificateListResultString);

            // Test 1: Managed Identity should take priority over access token
            KeyVaultClient client1
                = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null, null, null, "managed-id", "bearer-token", false);
            client1.getAliases();
            tokenUtilMockedStatic.verify(() -> AccessTokenUtil.getAccessToken(anyString(), eq("managed-id")), times(1));

            // Test 2: Access token should be used when managed identity is not set
            KeyVaultClient client2
                = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null, null, null, null, "bearer-token", false);
            List<String> result = client2.getAliases();
            assertEquals(1, result.size());
            assertTrue(result.contains("fakeCertificateItem"));
        }
    }

    @EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
    @Test
    public void testKeyVaultClients() {
        String accessToken = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ACCESS_TOKEN");
        KeyVaultClient keyVaultClient;
        if (accessToken != null && !accessToken.isEmpty()) {
            keyVaultClient = new KeyVaultClient(
                PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ENDPOINT"),
                null,
                null,
                null,
                null,
                accessToken,
                false);

        } else {
            keyVaultClient = new KeyVaultClient(
                PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ENDPOINT"),
                PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_TENANT_ID"),
                PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_ID"),
                PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_SECRET"));
        }
        String certificateName = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CERTIFICATE_NAME");

        assertTrue(keyVaultClient.getAliases().contains(certificateName));
        assertNotNull(keyVaultClient.getCertificate(certificateName));
        assertNotNull(keyVaultClient.getCertificateChain(certificateName));
        assertNotNull(keyVaultClient.getKey(certificateName, null));
    }
}
