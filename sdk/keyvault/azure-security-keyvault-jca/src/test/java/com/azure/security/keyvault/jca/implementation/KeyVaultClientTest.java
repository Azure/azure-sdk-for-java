// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.model.CertificateItem;
import com.azure.security.keyvault.jca.implementation.model.CertificateListResult;
import com.azure.security.keyvault.jca.implementation.utils.JsonConverterUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        List<String> result = keyVaultClient.getAliases();

        assertEquals(result.size(), 0);
    }

    @Test
    public void testGetAliasWithCertificateInfoWith1Page() {
        // Create fake certificates.
        CertificateItem fakeCertificateItem1 = new CertificateItem();
        fakeCertificateItem1.setId("certificates/fakeCertificateItem1");

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Collections.singletonList(fakeCertificateItem1));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null) {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return (uri != null) ? certificateListResultString : null;
            }
        };
        List<String> result = keyVaultClient.getAliases();

        assertEquals(result.size(), 1);
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
        certificateListResult.setNextLink("fakeNextLint");
        certificateListResult.setValue(Collections.singletonList(fakeCertificateItem1));

        // Create next page certificate result.
        CertificateListResult certificateListResultNext = new CertificateListResult();
        certificateListResultNext.setValue(Arrays.asList(fakeCertificateItem2, fakeCertificateItem3));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);
        String certificateListResultStringNext = JsonConverterUtil.toJson(certificateListResultNext);

        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null) {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                if ("fakeNextLint".equals(uri)) {
                    return certificateListResultStringNext;
                } else if (uri != null) {
                    return certificateListResultString;
                }

                return null;
            }
        };
        List<String> result = keyVaultClient.getAliases();

        assertEquals(result.size(), 3);
        assertTrue(result.containsAll(Arrays.asList("fakeCertificateItem1", "fakeCertificateItem2", "fakeCertificateItem3")));
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
        boolean disableChallengeResourceVerification =
            Boolean.parseBoolean(System.getProperty("azure.keyvault.disable-challenge-resource-verification"));

        return new KeyVaultClient(keyVaultUri, tenantId, clientId, clientSecret, null,
            disableChallengeResourceVerification);
    }


    @Test
    public void testCacheToken() {
        AccessToken cacheToken = new AccessToken();
        cacheToken.setExpiresIn(300); // 300 seconds.

        CertificateItem fakeCertificateItem = new CertificateItem();
        fakeCertificateItem.setId("certificates/fakeCertificateItem");

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Collections.singletonList(fakeCertificateItem));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        AtomicInteger getAccessTokenCallCount = new AtomicInteger();
        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, "") {
            @Override
            AccessToken getAccessToken(String resource, String managedIdentity) {
                getAccessTokenCallCount.incrementAndGet();
                return cacheToken;
            }

            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return certificateListResultString;
            }
        };

        keyVaultClient.getAliases();
        keyVaultClient.getAliases(); // Get aliases the second time.

        Assertions.assertEquals(1, getAccessTokenCallCount.get());
    }

    @Test
    public void testCacheTokenExpired() {
        AccessToken cacheToken = new AccessToken();
        cacheToken.setExpiresIn(50); // 50 seconds.

        CertificateItem fakeCertificateItem = new CertificateItem();
        fakeCertificateItem.setId("certificates/fakeCertificateItem");

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Collections.singletonList(fakeCertificateItem));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        AtomicInteger getAccessTokenCallCount = new AtomicInteger();
        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, "") {
            @Override
            AccessToken getAccessToken(String resource, String managedIdentity) {
                getAccessTokenCallCount.incrementAndGet();
                return cacheToken;
            }

            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return certificateListResultString;
            }
        };

        keyVaultClient.getAliases();
        keyVaultClient.getAliases(); // Get aliases the second time.

        Assertions.assertEquals(2, getAccessTokenCallCount.get());
    }
}
