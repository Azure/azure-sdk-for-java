// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.security.keyvault.secrets.implementation.models.DeletedSecretBundle;
import com.azure.security.keyvault.secrets.implementation.models.SecretBundle;
import com.azure.security.keyvault.secrets.implementation.models.SecretsModelsUtils;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for the previousVersion feature on SecretBundle, SecretProperties, and DeletedSecret.
 */
public class SecretPreviousVersionTest {

    @Test
    public void secretBundleDeserializesPreviousVersion() throws IOException {
        String json = "{\"id\":\"https://myvault.vault.azure.net/secrets/mysecret/abc123\","
            + "\"value\":\"secretVal\","
            + "\"previousVersion\":\"previousVer123\"}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            assertNotNull(bundle);
            assertEquals("previousVer123", bundle.getPreviousVersion());
            assertEquals("secretVal", bundle.getValue());
        }
    }

    @Test
    public void secretBundleDeserializesWithoutPreviousVersion() throws IOException {
        String json = "{\"id\":\"https://myvault.vault.azure.net/secrets/mysecret/abc123\","
            + "\"value\":\"secretVal\"}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            assertNotNull(bundle);
            assertNull(bundle.getPreviousVersion());
        }
    }

    @Test
    public void deletedSecretBundleDeserializesPreviousVersion() throws IOException {
        String json = "{\"id\":\"https://myvault.vault.azure.net/secrets/mysecret/abc123\","
            + "\"value\":\"secretVal\","
            + "\"previousVersion\":\"prevVer456\","
            + "\"recoveryId\":\"https://myvault.vault.azure.net/deletedsecrets/mysecret\","
            + "\"scheduledPurgeDate\":1700000000,"
            + "\"deletedDate\":1699000000}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            DeletedSecretBundle bundle = DeletedSecretBundle.fromJson(reader);
            assertNotNull(bundle);
            assertEquals("prevVer456", bundle.getPreviousVersion());
        }
    }

    @Test
    public void keyVaultSecretMappingIncludesPreviousVersion() throws IOException {
        String json = "{\"id\":\"https://myvault.vault.azure.net/secrets/mysecret/abc123\","
            + "\"value\":\"secretVal\","
            + "\"previousVersion\":\"prevVer789\"}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            KeyVaultSecret secret = SecretsModelsUtils.createKeyVaultSecret(bundle);
            assertNotNull(secret);
            assertNotNull(secret.getProperties());
            assertEquals("prevVer789", secret.getProperties().getPreviousVersion());
        }
    }

    @Test
    public void secretPropertiesMappingIncludesPreviousVersion() throws IOException {
        String json = "{\"id\":\"https://myvault.vault.azure.net/secrets/mysecret/abc123\","
            + "\"previousVersion\":\"prevVerABC\"}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            SecretProperties properties = SecretsModelsUtils.createSecretProperties(bundle);
            assertNotNull(properties);
            assertEquals("prevVerABC", properties.getPreviousVersion());
        }
    }

    @Test
    public void deletedSecretMappingIncludesPreviousVersion() throws IOException {
        String json = "{\"id\":\"https://myvault.vault.azure.net/secrets/mysecret/abc123\","
            + "\"previousVersion\":\"prevVerDEF\","
            + "\"recoveryId\":\"https://myvault.vault.azure.net/deletedsecrets/mysecret\","
            + "\"attributes\":{\"enabled\":true,\"recoveryLevel\":\"Recoverable\"}}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            DeletedSecretBundle bundle = DeletedSecretBundle.fromJson(reader);
            DeletedSecret deletedSecret = SecretsModelsUtils.createDeletedSecret(bundle);
            assertNotNull(deletedSecret);
            assertNotNull(deletedSecret.getProperties());
            assertEquals("prevVerDEF", deletedSecret.getProperties().getPreviousVersion());
        }
    }

    @Test
    public void previousVersionNullWhenNotPresent() throws IOException {
        String json = "{\"id\":\"https://myvault.vault.azure.net/secrets/mysecret/abc123\","
            + "\"value\":\"secretVal\"}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            SecretBundle bundle = SecretBundle.fromJson(reader);
            KeyVaultSecret secret = SecretsModelsUtils.createKeyVaultSecret(bundle);
            assertNotNull(secret);
            assertNull(secret.getProperties().getPreviousVersion());
        }
    }
}
