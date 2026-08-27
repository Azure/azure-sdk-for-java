// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

import com.azure.security.keyvault.jca.PropertyConvertorUtils;
import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.model.CertificateBundle;
import com.azure.security.keyvault.jca.implementation.model.CertificateItem;
import com.azure.security.keyvault.jca.implementation.model.CertificateItemAttributes;
import com.azure.security.keyvault.jca.implementation.model.CertificateListResult;
import com.azure.security.keyvault.jca.implementation.model.CertificatePolicy;
import com.azure.security.keyvault.jca.implementation.model.KeyProperties;
import com.azure.security.keyvault.jca.implementation.model.SecretBundle;
import com.azure.security.keyvault.jca.implementation.utils.HttpUtil;
import com.azure.security.keyvault.jca.implementation.utils.JsonConverterUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.API_VERSION_POSTFIX;

public class KeyVaultClientTest {
    private static final String KEY_VAULT_TEST_URI_GLOBAL = "https://fake.vault.azure.net/";

    private static final String CERTIFICATE_ALIAS = "client-cert";

    private static final String CERTIFICATE_URI
        = KEY_VAULT_TEST_URI_GLOBAL + "certificates/" + CERTIFICATE_ALIAS + API_VERSION_POSTFIX;

    private static final String VERSIONED_KEY_ID = KEY_VAULT_TEST_URI_GLOBAL + "keys/" + CERTIFICATE_ALIAS + "/v1";

    private static final String VERSIONED_SECRET_ID
        = KEY_VAULT_TEST_URI_GLOBAL + "secrets/" + CERTIFICATE_ALIAS + "/v1";

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
    public void testGetAliasFiltersOutDisabledCertificate() {
        // Enabled certificate.
        CertificateItemAttributes enabledAttributes = new CertificateItemAttributes();
        enabledAttributes.setEnabled(true);
        CertificateItem enabledCertificate = new CertificateItem();
        enabledCertificate.setId("certificates/client-cert-active");
        enabledCertificate.setAttributes(enabledAttributes);

        // Disabled certificate. This one previously caused an HTTP 403 while initializing the keystore.
        CertificateItemAttributes disabledAttributes = new CertificateItemAttributes();
        disabledAttributes.setEnabled(false);
        CertificateItem disabledCertificate = new CertificateItem();
        disabledCertificate.setId("certificates/client-cert-unused");
        disabledCertificate.setAttributes(disabledAttributes);

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Arrays.asList(enabledCertificate, disabledCertificate));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null) {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return certificateListResultString;
            }
        };
        List<String> result = keyVaultClient.getAliases();

        assertEquals(1, result.size());
        assertTrue(result.contains("client-cert-active"));
        assertFalse(result.contains("client-cert-unused"));
    }

    @Test
    public void testGetAliasKeepsEnabledAndAttributelessCertificates() {
        // Certificate explicitly enabled.
        CertificateItemAttributes enabledAttributes = new CertificateItemAttributes();
        enabledAttributes.setEnabled(true);
        CertificateItem enabledCertificate = new CertificateItem();
        enabledCertificate.setId("certificates/enabledCertificate");
        enabledCertificate.setAttributes(enabledAttributes);

        // Certificate without attributes, which must be treated as enabled for backward compatibility.
        CertificateItem attributelessCertificate = new CertificateItem();
        attributelessCertificate.setId("certificates/attributelessCertificate");

        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Arrays.asList(enabledCertificate, attributelessCertificate));

        String certificateListResultString = JsonConverterUtil.toJson(certificateListResult);

        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null) {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return certificateListResultString;
            }
        };
        List<String> result = keyVaultClient.getAliases();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList("enabledCertificate", "attributelessCertificate")));
    }

    @Test
    public void testGetAliasFiltersDisabledCertificateFromRawResponse() {
        // A response that mirrors the shape returned by the Azure Key Vault "list certificates" REST API, with one
        // enabled and one disabled certificate.
        String rawResponse
            = "{\"value\":[" + "{\"id\":\"https://fake.vault.azure.net/certificates/client-cert-active\","
                + "\"attributes\":{\"enabled\":true,\"nbf\":1783324860,\"exp\":1814861460}},"
                + "{\"id\":\"https://fake.vault.azure.net/certificates/client-cert-unused\","
                + "\"attributes\":{\"enabled\":false,\"nbf\":1783324860,\"exp\":1814861460}}]," + "\"nextLink\":null}";

        KeyVaultClient keyVaultClient = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null) {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return rawResponse;
            }
        };
        List<String> result = keyVaultClient.getAliases();

        assertEquals(1, result.size());
        assertTrue(result.contains("client-cert-active"));
        assertFalse(result.contains("client-cert-unused"));
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

    @Test
    public void testSystemAssignedManagedIdentityFallback() {
        CertificateItem certificateItem = new CertificateItem();
        certificateItem.setId("certificates/fakeCertificateItem");
        CertificateListResult certificateListResult = new CertificateListResult();
        certificateListResult.setValue(Arrays.asList(certificateItem));
        String response = JsonConverterUtil.toJson(certificateListResult);
        AtomicInteger getAccessTokenCount = new AtomicInteger();

        KeyVaultClient client = new KeyVaultClient(KEY_VAULT_TEST_URI_GLOBAL, null) {
            @Override
            String httpGet(String uri, Map<String, String> headers) {
                return response;
            }

            @Override
            AccessToken getAccessToken(String resource, String identity) {
                assertNull(identity);
                getAccessTokenCount.incrementAndGet();
                return new AccessToken("fake-token", 3600);
            }
        };

        assertEquals(1, client.getAliases().size());
        assertEquals(1, getAccessTokenCount.get());
    }

    @Test
    public void testCertificateChainUsesVersionedSecretId() throws Exception {
        CertificateBundle certificateBundle = createCertificateBundle(false);
        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setValue(new String(
            Files.readAllBytes(
                Paths.get("src/test/resources/certificate-util/SecretBundle.value/3-certificates-in-chain.pem")),
            StandardCharsets.UTF_8));

        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(CERTIFICATE_URI, JsonConverterUtil.toJson(certificateBundle));
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX,
            JsonConverterUtil.toJson(secretBundle));

        CertificateVersion certificateVersion = keyVaultClient.resolveCertificateVersion(CERTIFICATE_ALIAS);
        Certificate[] chain = keyVaultClient.getCertificateChainForVersion(certificateVersion);

        assertEquals(3, chain.length);
        assertEquals(1, keyVaultClient.getHttpCallCount(VERSIONED_SECRET_ID + API_VERSION_POSTFIX));
    }

    @Test
    public void testCertificateChainJsonParsingFailureIsPropagated() {
        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX, "{invalid-json");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID)));

        assertEquals("Failed to parse certificate chain response for alias: " + CERTIFICATE_ALIAS,
            exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    public void testCertificateChainMissingHttpResponseIsPropagated() {
        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX, (Object) null);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID)));

        assertEquals("Failed to load certificate chain response for alias: " + CERTIFICATE_ALIAS,
            exception.getMessage());
    }

    @Test
    public void testCertificateChainHttpFailureIsPropagatedWithoutWrapping() {
        RuntimeException httpFailure = new RuntimeException("Key Vault returned HTTP 429");
        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX, httpFailure);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID)));

        assertSame(httpFailure, exception);
    }

    @Test
    public void testCertificateChainMissingSecretValueIsPropagated() {
        SecretBundle secretBundle = new SecretBundle();
        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX,
            JsonConverterUtil.toJson(secretBundle));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID)));

        assertEquals("Certificate chain response has no secret value for alias: " + CERTIFICATE_ALIAS,
            exception.getMessage());
    }

    @Test
    public void testCertificateChainMissingSecretBundleIsPropagated() {
        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX, "null");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID)));

        assertEquals("Certificate chain response has no secret value for alias: " + CERTIFICATE_ALIAS,
            exception.getMessage());
    }

    @Test
    public void testCertificateChainPemDecodingFailureIsPropagated() {
        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setValue("-----BEGIN CERTIFICATE-----\ninvalid\n-----END CERTIFICATE-----");

        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX,
            JsonConverterUtil.toJson(secretBundle));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID)));

        assertEquals("Failed to decode certificate chain for alias: " + CERTIFICATE_ALIAS, exception.getMessage());
        assertTrue(exception.getCause() instanceof CertificateException);
    }

    @Test
    public void testCertificateChainUnterminatedPemFailureIsPropagated() throws Exception {
        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setValue(readUnterminatedCertificatePem());

        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX,
            JsonConverterUtil.toJson(secretBundle));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID)));

        assertEquals("Failed to decode certificate chain for alias: " + CERTIFICATE_ALIAS, exception.getMessage());
        assertTrue(exception.getCause() instanceof CertificateException);
        assertEquals("Certificate PEM block is not terminated.", exception.getCause().getMessage());
    }

    @Test
    public void testCertificateChainPkcs12DecodingFailureIsPropagated() {
        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setValue(Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3 }));

        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX,
            JsonConverterUtil.toJson(secretBundle));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID)));

        assertEquals("Failed to decode certificate chain for alias: " + CERTIFICATE_ALIAS, exception.getMessage());
        assertNotNull(exception.getCause());
    }

    @Test
    public void testCertificateChainInvalidBase64IsPropagated() {
        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setValue("not-valid-base64!");

        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX,
            JsonConverterUtil.toJson(secretBundle));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID)));

        assertEquals("Failed to decode certificate chain for alias: " + CERTIFICATE_ALIAS, exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void testCertificateChainDecodingFailureIsRetried() throws Exception {
        SecretBundle invalidBundle = new SecretBundle();
        invalidBundle.setValue(readUnterminatedCertificatePem());
        SecretBundle validBundle = new SecretBundle();
        validBundle.setValue(new String(
            Files.readAllBytes(
                Paths.get("src/test/resources/certificate-util/SecretBundle.value/3-certificates-in-chain.pem")),
            StandardCharsets.UTF_8));

        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX,
            JsonConverterUtil.toJson(invalidBundle), JsonConverterUtil.toJson(validBundle));
        CertificateVersion certificateVersion = createCertificateVersion(VERSIONED_SECRET_ID);

        assertThrows(IllegalStateException.class,
            () -> keyVaultClient.getCertificateChainForVersion(certificateVersion));
        Certificate[] chain = keyVaultClient.getCertificateChainForVersion(certificateVersion);

        assertEquals(3, chain.length);
        assertEquals(2, keyVaultClient.getHttpCallCount(VERSIONED_SECRET_ID + API_VERSION_POSTFIX));
    }

    private static String readUnterminatedCertificatePem() throws IOException {
        String pemString = new String(
            Files.readAllBytes(
                Paths.get("src/test/resources/certificate-util/SecretBundle.value/pem-non-exportable-key.pem")),
            StandardCharsets.UTF_8);
        return pemString.substring(0, pemString.indexOf("-----END CERTIFICATE-----"));
    }

    @Test
    public void testCertificateChainWithoutSecretIdReturnsEmpty() {
        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        Certificate[] chain = keyVaultClient.getCertificateChainForVersion(createCertificateVersion(null));

        assertEquals(0, chain.length);
        assertEquals(0, keyVaultClient.getTotalHttpCallCount());
    }

    @Test
    public void testCertificateChainWithoutResolvedVersionReturnsEmpty() {
        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        Certificate[] chain = keyVaultClient.getCertificateChainForVersion(null);

        assertEquals(0, chain.length);
        assertEquals(0, keyVaultClient.getTotalHttpCallCount());
    }

    @Test
    public void testCertificateChainDecodedWithoutCertificatesReturnsEmpty() throws Exception {
        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setValue(createEmptyPkcs12());
        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX,
            JsonConverterUtil.toJson(secretBundle));

        Certificate[] chain
            = keyVaultClient.getCertificateChainForVersion(createCertificateVersion(VERSIONED_SECRET_ID));

        assertEquals(0, chain.length);
    }

    @Test
    public void testExportableKeyUsesVersionedSecretId() throws Exception {
        CertificateBundle certificateBundle = createCertificateBundle(true);
        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setContentType("application/x-pkcs12");
        secretBundle.setValue(new String(
            Files.readAllBytes(
                Paths.get("src/test/resources/certificate-util/SecretBundle.value/pkcs12-exportable-key.pfx")),
            StandardCharsets.UTF_8));

        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(CERTIFICATE_URI, JsonConverterUtil.toJson(certificateBundle));
        keyVaultClient.addHttpResponses(VERSIONED_SECRET_ID + API_VERSION_POSTFIX,
            JsonConverterUtil.toJson(secretBundle));

        CertificateVersion certificateVersion = keyVaultClient.resolveCertificateVersion(CERTIFICATE_ALIAS);
        Key key = keyVaultClient.getKeyForVersion(certificateVersion, null);

        assertNotNull(key);
        assertEquals(1, keyVaultClient.getHttpCallCount(VERSIONED_SECRET_ID + API_VERSION_POSTFIX));
    }

    @Test
    public void testKeylessKeyUsesVersionedKeyId() {
        CertificateBundle certificateBundle = createCertificateBundle(false);

        ScriptedKeyVaultClient keyVaultClient = createClientWithAccessToken();
        keyVaultClient.addHttpResponses(CERTIFICATE_URI, JsonConverterUtil.toJson(certificateBundle));

        CertificateVersion certificateVersion = keyVaultClient.resolveCertificateVersion(CERTIFICATE_ALIAS);
        Key key = keyVaultClient.getKeyForVersion(certificateVersion, null);

        assertTrue(key instanceof KeyVaultPrivateKey);
        assertEquals(VERSIONED_KEY_ID, ((KeyVaultPrivateKey) key).getKid());
    }

    @Test
    void getKeyDoesNotLogPrivateKeyPem() throws Exception {
        String alias = "pem-certificate";
        String certificateSecretUri = KEY_VAULT_TEST_URI_GLOBAL + "secrets/" + alias;
        String pemString = new String(
            Files.readAllBytes(
                Paths.get("src/test/resources/certificate-util/downloaded-from-portal/pem-exportable-key.pem")),
            StandardCharsets.UTF_8);

        KeyProperties keyProperties = new KeyProperties();
        keyProperties.setExportable(true);
        keyProperties.setKty("RSA");
        CertificatePolicy certificatePolicy = new CertificatePolicy();
        certificatePolicy.setKeyProperties(keyProperties);
        CertificateBundle certificateBundle = new CertificateBundle();
        certificateBundle.setPolicy(certificatePolicy);
        certificateBundle.setSid(certificateSecretUri);

        SecretBundle secretBundle = new SecretBundle();
        secretBundle.setContentType("application/x-pem-file");
        secretBundle.setValue(pemString);

        List<String> loggedValues = new ArrayList<>();
        Handler collector = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                loggedValues.add(logRecord.getMessage());
                if (logRecord.getParameters() != null) {
                    for (Object parameter : logRecord.getParameters()) {
                        loggedValues.add(String.valueOf(parameter));
                    }
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };

        Logger logger = Logger.getLogger(KeyVaultClient.class.getName());
        Level originalLevel = logger.getLevel();
        boolean originalUseParentHandlers = logger.getUseParentHandlers();
        logger.addHandler(collector);
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        try {
            ScriptedKeyVaultClient keyVaultClient = new ScriptedKeyVaultClient("bearer-token");
            keyVaultClient.addHttpResponses(
                KEY_VAULT_TEST_URI_GLOBAL + "certificates/" + alias + HttpUtil.API_VERSION_POSTFIX,
                JsonConverterUtil.toJson(certificateBundle));
            keyVaultClient.addHttpResponses(certificateSecretUri + HttpUtil.API_VERSION_POSTFIX,
                JsonConverterUtil.toJson(secretBundle));
            Key key = keyVaultClient.getKey(alias, null);

            assertNotNull(key);
            assertEquals("RSA", key.getAlgorithm());
        } finally {
            logger.removeHandler(collector);
            logger.setLevel(originalLevel);
            logger.setUseParentHandlers(originalUseParentHandlers);
        }

        assertFalse(loggedValues.contains(pemString), "The private-key PEM must never be logged");
        assertTrue(loggedValues.stream().noneMatch(value -> value.contains("BEGIN PRIVATE KEY")),
            "No fragment of the private-key PEM may be logged");
        assertTrue(loggedValues.contains("RSA"), "The non-secret key type stays available for diagnostics");
    }

    private static CertificateBundle createCertificateBundle(boolean exportable) {
        KeyProperties keyProperties = new KeyProperties();
        keyProperties.setExportable(exportable);
        keyProperties.setKty("RSA");
        CertificatePolicy certificatePolicy = new CertificatePolicy();
        certificatePolicy.setKeyProperties(keyProperties);
        CertificateBundle certificateBundle = new CertificateBundle();
        certificateBundle.setKid(VERSIONED_KEY_ID);
        certificateBundle.setSid(VERSIONED_SECRET_ID);
        certificateBundle.setPolicy(certificatePolicy);
        return certificateBundle;
    }

    private static CertificateVersion createCertificateVersion(String secretId) {
        return new CertificateVersion(CERTIFICATE_ALIAS, null, VERSIONED_KEY_ID, secretId, true, "RSA");
    }

    private static ScriptedKeyVaultClient createClientWithAccessToken() {
        return new ScriptedKeyVaultClient("test-token");
    }

    private static String createEmptyPkcs12() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, new char[0]);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        keyStore.store(output, new char[0]);
        return Base64.getEncoder().encodeToString(output.toByteArray());
    }

    private static final class ScriptedKeyVaultClient extends KeyVaultClient {
        private final Map<String, List<Object>> httpResponses = new HashMap<>();
        private final Map<String, AtomicInteger> httpCallCounts = new HashMap<>();

        private ScriptedKeyVaultClient(String accessToken) {
            super(KEY_VAULT_TEST_URI_GLOBAL, null, null, null, null, accessToken, false);
        }

        private void addHttpResponses(String uri, Object... responses) {
            httpResponses.put(uri, new ArrayList<>(Arrays.asList(responses)));
        }

        private int getHttpCallCount(String uri) {
            AtomicInteger count = httpCallCounts.get(uri);
            return count == null ? 0 : count.get();
        }

        private int getTotalHttpCallCount() {
            return httpCallCounts.values().stream().mapToInt(AtomicInteger::get).sum();
        }

        @Override
        String httpGet(String uri, Map<String, String> headers) {
            int invocation = httpCallCounts.computeIfAbsent(uri, ignored -> new AtomicInteger()).getAndIncrement();
            List<Object> responses = httpResponses.get(uri);
            if (responses == null || responses.isEmpty()) {
                throw new AssertionError("Unexpected HTTP GET: " + uri);
            }

            Object response = responses.get(Math.min(invocation, responses.size() - 1));
            if (response instanceof RuntimeException) {
                throw (RuntimeException) response;
            }
            if (response instanceof Error) {
                throw (Error) response;
            }
            return (String) response;
        }
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
