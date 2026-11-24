// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.security.keyvault.secrets.implementation.models.SecretBundle;
import com.azure.security.keyvault.secrets.implementation.models.SecretsModelsUtils;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for outContentType (PFX-to-PEM conversion) support in the Secrets SDK.
 *
 * <p>These tests verify:</p>
 * <ul>
 *   <li>New getSecret/getSecretWithResponse overloads exist and reject invalid names</li>
 *   <li>SecretBundle with different contentType values deserializes correctly</li>
 *   <li>Mapping from SecretBundle to KeyVaultSecret preserves contentType</li>
 * </ul>
 */
class OutContentTypeTest {

    private static final String PFX_CONTENT_TYPE = "application/x-pkcs12";
    private static final String PEM_CONTENT_TYPE = "application/x-pem-file";

    // ---- SecretClient parameter validation ----

    @Test
    void syncGetSecretWithOutContentTypeRejectsNullName() {
        // SecretClient.getSecret(null, version, outContentType) should throw
        // We can't instantiate a real SecretClient without a vault, but the static validation
        // is tested by verifying the method signature exists and compiles.
        // The method throws IllegalArgumentException for null name before hitting the network.
        assertThrows(IllegalArgumentException.class, () -> {
            SecretClient client = new SecretClientBuilder().vaultUrl("https://fake-vault.vault.azure.net")
                .credential(new com.azure.core.test.utils.MockTokenCredential())
                .buildClient();
            client.getSecret(null, "ver1", PEM_CONTENT_TYPE);
        });
    }

    @Test
    void syncGetSecretWithOutContentTypeRejectsEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            SecretClient client = new SecretClientBuilder().vaultUrl("https://fake-vault.vault.azure.net")
                .credential(new com.azure.core.test.utils.MockTokenCredential())
                .buildClient();
            client.getSecret("", "ver1", PEM_CONTENT_TYPE);
        });
    }

    @Test
    void syncGetSecretWithResponseAndOutContentTypeRejectsNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            SecretClient client = new SecretClientBuilder().vaultUrl("https://fake-vault.vault.azure.net")
                .credential(new com.azure.core.test.utils.MockTokenCredential())
                .buildClient();
            client.getSecretWithResponse(null, "ver1", PEM_CONTENT_TYPE, com.azure.core.util.Context.NONE);
        });
    }

    @Test
    void asyncGetSecretWithOutContentTypeRejectsNullName() {
        SecretAsyncClient client = new SecretClientBuilder().vaultUrl("https://fake-vault.vault.azure.net")
            .credential(new com.azure.core.test.utils.MockTokenCredential())
            .buildAsyncClient();

        reactor.test.StepVerifier.create(client.getSecret(null, "ver1", PEM_CONTENT_TYPE))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    void asyncGetSecretWithOutContentTypeRejectsEmptyName() {
        SecretAsyncClient client = new SecretClientBuilder().vaultUrl("https://fake-vault.vault.azure.net")
            .credential(new com.azure.core.test.utils.MockTokenCredential())
            .buildAsyncClient();

        reactor.test.StepVerifier.create(client.getSecret("", "ver1", PEM_CONTENT_TYPE))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    @Test
    void asyncGetSecretWithResponseAndOutContentTypeRejectsNullName() {
        SecretAsyncClient client = new SecretClientBuilder().vaultUrl("https://fake-vault.vault.azure.net")
            .credential(new com.azure.core.test.utils.MockTokenCredential())
            .buildAsyncClient();

        reactor.test.StepVerifier.create(client.getSecretWithResponse(null, "ver1", PEM_CONTENT_TYPE))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    // ---- SecretBundle contentType deserialization ----

    @Test
    void secretBundleDeserializesWithPfxContentType() throws IOException {
        String json = "{\"value\": \"base64-cert-data\","
            + "\"id\": \"https://myvault.vault.azure.net/secrets/cert-secret/ver1\","
            + "\"contentType\": \"application/x-pkcs12\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            assertEquals(PFX_CONTENT_TYPE, bundle.getContentType());
            assertEquals("base64-cert-data", bundle.getValue());
        }
    }

    @Test
    void secretBundleDeserializesWithPemContentType() throws IOException {
        String json = "{\"value\": \"-----BEGIN CERTIFICATE-----\\nMIIB...\\n-----END CERTIFICATE-----\","
            + "\"id\": \"https://myvault.vault.azure.net/secrets/cert-secret/ver1\","
            + "\"contentType\": \"application/x-pem-file\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            assertEquals(PEM_CONTENT_TYPE, bundle.getContentType());
        }
    }

    @Test
    void secretBundleDeserializesWithNullContentType() throws IOException {
        String json
            = "{\"value\": \"some-value\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/plain-secret/ver1\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            assertNull(bundle.getContentType());
        }
    }

    // ---- Mapping from SecretBundle to KeyVaultSecret preserves contentType ----

    @Test
    void keyVaultSecretMappedFromPfxBundleHasCorrectContentType() throws IOException {
        String json = "{\"value\": \"pfx-data\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/cert/ver1\","
            + "\"contentType\": \"application/x-pkcs12\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            KeyVaultSecret secret = SecretsModelsUtils.createKeyVaultSecret(bundle);
            assertNotNull(secret);
            assertEquals(PFX_CONTENT_TYPE, secret.getProperties().getContentType());
            assertEquals("pfx-data", secret.getValue());
        }
    }

    @Test
    void keyVaultSecretMappedFromPemBundleHasCorrectContentType() throws IOException {
        String json = "{\"value\": \"pem-data\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/cert/ver1\","
            + "\"contentType\": \"application/x-pem-file\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            KeyVaultSecret secret = SecretsModelsUtils.createKeyVaultSecret(bundle);
            assertNotNull(secret);
            assertEquals(PEM_CONTENT_TYPE, secret.getProperties().getContentType());
            assertEquals("pem-data", secret.getValue());
        }
    }

    @Test
    void keyVaultSecretMappedFromBundleWithPreviousVersionAndContentType() throws IOException {
        String json = "{\"value\": \"cert-data\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/cert/ver2\","
            + "\"previousVersion\": \"ver1\"," + "\"contentType\": \"application/x-pem-file\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            KeyVaultSecret secret = SecretsModelsUtils.createKeyVaultSecret(bundle);
            assertNotNull(secret);
            assertEquals(PEM_CONTENT_TYPE, secret.getProperties().getContentType());
            assertEquals("ver1", secret.getProperties().getPreviousVersion());
            assertEquals("ver2", secret.getProperties().getVersion());
        }
    }

    // ---- ContentType enum values ----

    @Test
    void contentTypeEnumPfxValue() {
        assertEquals("application/x-pkcs12",
            com.azure.security.keyvault.secrets.implementation.models.ContentType.PFX.toString());
    }

    @Test
    void contentTypeEnumPemValue() {
        assertEquals("application/x-pem-file",
            com.azure.security.keyvault.secrets.implementation.models.ContentType.PEM.toString());
    }

    @Test
    void contentTypeFromStringRoundTrip() {
        com.azure.security.keyvault.secrets.implementation.models.ContentType custom
            = com.azure.security.keyvault.secrets.implementation.models.ContentType.fromString("custom/type");
        assertEquals("custom/type", custom.toString());
    }
}
