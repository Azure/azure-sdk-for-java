// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.secrets.implementation.SecretPropertiesHelper;
import com.azure.security.keyvault.secrets.implementation.DeletedSecretHelper;
import com.azure.security.keyvault.secrets.implementation.models.SecretBundle;
import com.azure.security.keyvault.secrets.implementation.models.DeletedSecretBundle;
import com.azure.security.keyvault.secrets.implementation.models.SecretsModelsUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the {@code previousVersion} property on {@link SecretProperties}, {@link KeyVaultSecret},
 * and {@link DeletedSecret}.
 */
class SecretPropertiesPreviousVersionTest {

    // ---- SecretProperties getter tests ----

    @Test
    void previousVersionDefaultsToNull() {
        SecretProperties properties = new SecretProperties();
        assertNull(properties.getPreviousVersion());
    }

    @Test
    void previousVersionSetViaHelper() {
        SecretProperties properties = new SecretProperties();
        SecretPropertiesHelper.setPreviousVersion(properties, "abc123");
        assertEquals("abc123", properties.getPreviousVersion());
    }

    @Test
    void previousVersionSetViaHelperNull() {
        SecretProperties properties = new SecretProperties();
        SecretPropertiesHelper.setPreviousVersion(properties, "v1");
        SecretPropertiesHelper.setPreviousVersion(properties, null);
        assertNull(properties.getPreviousVersion());
    }

    // ---- SecretProperties JSON deserialization ----

    @Test
    void secretPropertiesDeserializesWithPreviousVersion() throws IOException {
        String json = "{\"id\": \"https://myvault.vault.azure.net/secrets/mysecret/ver1\","
            + "\"previousVersion\": \"ver0\"," + "\"managed\": false}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretProperties properties = SecretProperties.fromJson(reader);
            assertEquals("ver0", properties.getPreviousVersion());
            assertEquals("mysecret", properties.getName());
            assertEquals("ver1", properties.getVersion());
        }
    }

    @Test
    void secretPropertiesDeserializesWithoutPreviousVersion() throws IOException {
        String json = "{\"id\": \"https://myvault.vault.azure.net/secrets/mysecret/ver1\"," + "\"managed\": true}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretProperties properties = SecretProperties.fromJson(reader);
            assertNull(properties.getPreviousVersion());
        }
    }

    @Test
    void secretPropertiesDeserializesNullPreviousVersion() throws IOException {
        String json = "{\"previousVersion\": null}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretProperties properties = SecretProperties.fromJson(reader);
            assertNull(properties.getPreviousVersion());
        }
    }

    // ---- KeyVaultSecret JSON deserialization ----

    @Test
    void keyVaultSecretDeserializesWithPreviousVersion() throws IOException {
        String json = "{\"value\": \"secret-value\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/test/ver2\","
            + "\"previousVersion\": \"ver1\"," + "\"contentType\": \"text/plain\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            KeyVaultSecret secret = KeyVaultSecret.fromJson(reader);
            assertEquals("secret-value", secret.getValue());
            assertEquals("ver1", secret.getProperties().getPreviousVersion());
            assertEquals("ver2", secret.getProperties().getVersion());
        }
    }

    @Test
    void keyVaultSecretDeserializesWithoutPreviousVersion() throws IOException {
        String json
            = "{\"value\": \"secret-value\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/test/ver2\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            KeyVaultSecret secret = KeyVaultSecret.fromJson(reader);
            assertNull(secret.getProperties().getPreviousVersion());
        }
    }

    // ---- DeletedSecret JSON deserialization ----

    @Test
    void deletedSecretDeserializesWithPreviousVersion() throws IOException {
        String json = "{\"value\": \"secret-value\","
            + "\"id\": \"https://myvault.vault.azure.net/secrets/delsecret/ver3\"," + "\"previousVersion\": \"ver2\","
            + "\"recoveryId\": \"https://myvault.vault.azure.net/deletedsecrets/delsecret\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            DeletedSecret deletedSecret = DeletedSecret.fromJson(reader);
            assertEquals("ver2", deletedSecret.getProperties().getPreviousVersion());
            assertEquals("delsecret", deletedSecret.getName());
        }
    }

    @Test
    void deletedSecretDeserializesWithoutPreviousVersion() throws IOException {
        String json
            = "{\"value\": \"secret-value\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/delsecret/ver3\","
                + "\"recoveryId\": \"https://myvault.vault.azure.net/deletedsecrets/delsecret\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            DeletedSecret deletedSecret = DeletedSecret.fromJson(reader);
            assertNull(deletedSecret.getProperties().getPreviousVersion());
        }
    }

    // ---- SecretsModelsUtils mapping tests ----

    @Test
    void createKeyVaultSecretMapsFromSecretBundle() throws IOException {
        String json
            = "{\"value\": \"my-secret\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/name1/version1\","
                + "\"previousVersion\": \"version0\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            assertEquals("version0", bundle.getPreviousVersion());

            KeyVaultSecret secret = SecretsModelsUtils.createKeyVaultSecret(bundle);
            assertNotNull(secret);
            assertEquals("version0", secret.getProperties().getPreviousVersion());
            assertEquals("my-secret", secret.getValue());
        }
    }

    @Test
    void createSecretPropertiesMapsFromSecretBundle() throws IOException {
        String json = "{\"id\": \"https://myvault.vault.azure.net/secrets/name1/version1\","
            + "\"previousVersion\": \"version0\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            SecretProperties properties = SecretsModelsUtils.createSecretProperties(bundle);
            assertNotNull(properties);
            assertEquals("version0", properties.getPreviousVersion());
        }
    }

    @Test
    void createSecretPropertiesMapsNullPreviousVersion() throws IOException {
        String json = "{\"id\": \"https://myvault.vault.azure.net/secrets/name1/version1\"}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            SecretProperties properties = SecretsModelsUtils.createSecretProperties(bundle);
            assertNotNull(properties);
            assertNull(properties.getPreviousVersion());
        }
    }

    @Test
    void createDeletedSecretMapsFromDeletedSecretBundle() throws IOException {
        String json = "{\"value\": \"my-secret\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/delname/ver3\","
            + "\"previousVersion\": \"ver2\","
            + "\"recoveryId\": \"https://myvault.vault.azure.net/deletedsecrets/delname\","
            + "\"attributes\": {\"enabled\": true, \"recoveryLevel\": \"Recoverable+Purgeable\"}}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            DeletedSecretBundle bundle = DeletedSecretBundle.fromJson(reader);
            assertEquals("ver2", bundle.getPreviousVersion());

            DeletedSecret deletedSecret = SecretsModelsUtils.createDeletedSecret(bundle);
            assertNotNull(deletedSecret);
            assertEquals("ver2", deletedSecret.getProperties().getPreviousVersion());
            assertEquals("delname", deletedSecret.getName());
        }
    }

    // ---- DeletedSecretHelper tests ----

    @Test
    void deletedSecretHelperSetsPreviousVersion() {
        DeletedSecret deletedSecret = new DeletedSecret();
        DeletedSecretHelper.setPreviousVersion(deletedSecret, "prevVer");
        assertEquals("prevVer", deletedSecret.getProperties().getPreviousVersion());
    }

    // ---- Full round-trip with all fields ----

    @Test
    void fullSecretBundleRoundTripWithPreviousVersion() throws IOException {
        String json
            = "{\"value\": \"super-secret\"," + "\"id\": \"https://myvault.vault.azure.net/secrets/fulltest/ver5\","
                + "\"previousVersion\": \"ver4\"," + "\"contentType\": \"application/json\","
                + "\"kid\": \"https://myvault.vault.azure.net/keys/mykey/keyver1\"," + "\"managed\": true,"
                + "\"tags\": {\"env\": \"prod\"},"
                + "\"attributes\": {\"enabled\": true, \"created\": 1700000000, \"updated\": 1700100000}}";

        try (JsonReader reader = JsonProviders.createReader(new StringReader(json))) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            KeyVaultSecret secret = SecretsModelsUtils.createKeyVaultSecret(bundle);

            assertNotNull(secret);
            assertEquals("super-secret", secret.getValue());
            assertEquals("ver4", secret.getProperties().getPreviousVersion());
            assertEquals("ver5", secret.getProperties().getVersion());
            assertEquals("fulltest", secret.getProperties().getName());
            assertEquals("application/json", secret.getProperties().getContentType());
            assertEquals("https://myvault.vault.azure.net/keys/mykey/keyver1", secret.getProperties().getKeyId());
            assertEquals(true, secret.getProperties().isManaged());
            assertEquals("prod", secret.getProperties().getTags().get("env"));
            assertEquals(true, secret.getProperties().isEnabled());
            assertNotNull(secret.getProperties().getCreatedOn());
            assertNotNull(secret.getProperties().getUpdatedOn());
        }
    }
}
