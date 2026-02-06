// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import com.azure.security.keyvault.jca.PropertyConvertorUtils;
import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.model.CertificateItem;
import com.azure.security.keyvault.jca.implementation.model.CertificateListResult;
import com.azure.security.keyvault.jca.implementation.utils.JsonConverterUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultClientTest {
    private static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";

    @Test
    public void testGetAliasWithCertificateInfoWith0Page() {
        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null) {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return "fakeValue";
            }
        };

        assertEquals(0, keyVaultClient.getAliases().size());
    }

    @Test
    public void testGetAliasWithCertificateInfoWith1Page() {
        // Create fake certificates.
        CertificateItem fakeCertificateItem1 = new CertificateItem();
        fakeCertificateItem1.setId("certificates/fakeCertificateItem1");

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Arrays.asList(fakeCertificateItem1));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null) {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return certificateListResultString;
            }
        };

        List<String> result = keyVaultClient.getAliases();
        assertEquals(1, result.size());
        assertTrue(result.contains("fakeCertificateItem1"));
    }

    @Test
    public void testGetAliasWithCertificateInfoWith2Pages() {
        // create fake certificates
        CertificateItem fakeCertificateItem1 = new CertificateItem();
        fakeCertificateItem1.setId("certificates/fakeCertificateItem1");

        CertificateItem fakeCertificateItem2 = new CertificateItem();
        fakeCertificateItem2.setId("certificates/fakeCertificateItem2");

        CertificateItem fakeCertificateItem3 = new CertificateItem();
        fakeCertificateItem3.setId("certificates/fakeCertificateItem3");

        // Create first page certificate result.
        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setNextLink("fakeNextLink");
        certificateListResult.setValue(Arrays.asList(fakeCertificateItem1));

        // Create next page certificate result.
        CertificateListResult certificateListResultNext = new CertificateListResult();
        certificateListResultNext.setValue(Arrays.asList(fakeCertificateItem2, fakeCertificateItem3));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);
        String certificateListResultStringNext = JsonConverterUtil.toJson(certificateListResultNext);

        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null) {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return "fakeNextLink".equals(uri) ? certificateListResultStringNext : certificateListResultString;
            }
        };

        List<String> result = keyVaultClient.getAliases();
        assertEquals(3, result.size());
        assertTrue(
            result.containsAll(Arrays.asList("fakeCertificateItem1", "fakeCertificateItem2", "fakeCertificateItem3")));
    }

    @Test
    public void testCacheToken() {
        AccessToken cacheToken = new AccessToken();
        cacheToken.setExpiresIn(300); // 300 seconds.

        CertificateItem fakeCertificateItem = new CertificateItem();
        fakeCertificateItem.setId("certificates/fakeCertificateItem");

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Arrays.asList(fakeCertificateItem));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        AtomicInteger getAccessTokenCount = new AtomicInteger();
        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, "") {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return certificateListResultString;
            }

            @Override
            AccessToken getAccessToken(String resource, String identity) {
                getAccessTokenCount.incrementAndGet();
                return cacheToken;
            }
        };
        keyVaultClient.getAliases();
        keyVaultClient.getAliases(); // Get aliases the second time.

        assertEquals(1, getAccessTokenCount.get());
    }

    @Test
    public void testCacheTokenExpired() {
        AccessToken cacheToken = new AccessToken();
        cacheToken.setExpiresIn(50); // 50 seconds.

        CertificateItem fakeCertificateItem = new CertificateItem();
        fakeCertificateItem.setId("certificates/fakeCertificateItem");

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Arrays.asList(fakeCertificateItem));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        AtomicInteger getAccessTokenCount = new AtomicInteger();
        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, "") {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return certificateListResultString;
            }

            @Override
            AccessToken getAccessToken(String resource, String identity) {
                getAccessTokenCount.incrementAndGet();
                return cacheToken;
            }
        };

        keyVaultClient.getAliases();
        keyVaultClient.getAliases(); // Get aliases the second time.

        assertEquals(2, getAccessTokenCount.get());
    }

    @Test
    public void testAccessTokenAuthentication() {
        CertificateItem fakeCertificateItem = new CertificateItem();
        fakeCertificateItem.setId("certificates/fakeCertificateItem");

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Arrays.asList(fakeCertificateItem));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        // Create client with access token
        String testAccessToken = "test-bearer-token-12345";
        KeyVaultClient keyVaultClient
            = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null, null, null, null, testAccessToken, false) {
                @Override
                String httpGet(String uri, Map<String, String> headers) {
                    return certificateListResultString;
                }
            };

        List<String> result = keyVaultClient.getAliases();

        // Verify that the access token was used
        assertEquals(1, result.size());
        assertTrue(result.contains("fakeCertificateItem"));
    }

    @Test
    public void testAuthenticationPriority() {
        AtomicInteger getAccessTokenCount = new AtomicInteger();
        AccessToken accessToken = new AccessToken("fake-token", 3600);

        CertificateItem fakeCertificateItem = new CertificateItem();
        fakeCertificateItem.setId("certificates/fakeCertificateItem");

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Arrays.asList(fakeCertificateItem));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        // Test 1: Managed Identity should take priority over access token
        KeyVaultClient client1
            = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null, null, null, "managed-id", "bearer-token", false) {
                @Override
                String httpGet(String uri, Map<String, String> headers) {
                    return certificateListResultString;
                }

                @Override
                AccessToken getAccessToken(String resource, String identity) {
                    if ("managed-id".equals(identity)) {
                        getAccessTokenCount.incrementAndGet();
                    }
                    return accessToken;
                }
            };
        client1.getAliases();

        // Test 2: Access token should be used when managed identity is not set
        KeyVaultClient client2
            = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null, null, null, null, "bearer-token", false) {
                @Override
                String httpGet(String uri, Map<String, String> headers) {
                    return certificateListResultString;
                }

                @Override
                AccessToken getAccessToken(String resource, String identity) {
                    if ("managed-id".equals(identity)) {
                        getAccessTokenCount.incrementAndGet();
                    }
                    return accessToken;
                }
            };

        List<String> result = client2.getAliases();
        assertEquals(1, result.size());
        assertTrue(result.contains("fakeCertificateItem"));

        assertEquals(1, getAccessTokenCount.get());
    }

    @EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
    @Test
    public void testKeyVaultClients() {
        String accessToken = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ACCESS_TOKEN");
        KeyVaultClient keyVaultClient;
        if (accessToken != null && !accessToken.isEmpty()) {
            keyVaultClient = new KeyVaultClient(PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ENDPOINT"),
                null, null, null, null, accessToken, false);

        } else {
            keyVaultClient = new KeyVaultClient(PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ENDPOINT"),
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
