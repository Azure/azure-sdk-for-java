// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys;

import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.v2.security.keyvault.keys.models.DeletedKey;
import com.azure.v2.security.keyvault.keys.models.KeyProperties;
import com.azure.v2.security.keyvault.keys.models.KeyType;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyClientTest extends KeyClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(KeyClientTest.class);

    protected KeyClient keyClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    protected void createKeyClient(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion, null);
    }

    protected void createKeyClient(HttpClient httpClient, KeyServiceVersion serviceVersion, String testTenantId) {
        keyClient = getKeyClientBuilder(httpClient, testTenantId, getEndpoint(), serviceVersion).buildClient();
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createKeyRunner((keyToCreate) -> {
            KeyVaultKey createdKey = keyClient.createKey(keyToCreate);

            assertKeyEquals(keyToCreate, createdKey);

            if (!isHsmEnabled) {
                assertEquals("0", createdKey.getProperties().getHsmPlatform());
            }
        });
    }

    /**
     * Tests that an RSA key can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        createRsaKeyRunner((keyToCreate) -> {
            KeyVaultKey createdKey = keyClient.createRsaKey(keyToCreate);

            assertRsaKeyEquals(keyToCreate, createdKey);
        });
    }

    /**
     * Tests that a key can be updated in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        updateKeyRunner((originalKey, updatedKey) -> {
            KeyVaultKey originalKeyResponse = keyClient.createKey(originalKey);
            KeyVaultKey updatedKeyResponse = keyClient.updateKeyProperties(originalKeyResponse.getProperties());

            assertKeyEquals(updatedKey, updatedKeyResponse);
        });
    }

    /**
     * Tests that a key can be retrieved from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        getKeyRunner((keyToGet) -> {
            KeyVaultKey createdKey = keyClient.createKey(keyToGet);
            KeyVaultKey retrievedKey = keyClient.getKey(keyToGet.getName());

            assertKeyEquals(keyToGet, retrievedKey);
        });
    }

    /**
     * Tests that a key can be deleted from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        deleteKeyRunner((keyToDelete) -> {
            KeyVaultKey createdKey = keyClient.createKey(keyToDelete);
            DeletedKey deletedKey = keyClient.beginDeleteKey(keyToDelete.getName()).getFinalResult();

            assertNotNull(deletedKey.getDeletedOn());
            assertNotNull(deletedKey.getRecoveryId());
            assertNotNull(deletedKey.getScheduledPurgeDate());
            assertEquals(keyToDelete.getName(), deletedKey.getName());
        });
    }

    /**
     * Tests that a deleted key can be retrieved from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        getDeletedKeyRunner((keyToDeleteAndGet) -> {
            KeyVaultKey createdKey = keyClient.createKey(keyToDeleteAndGet);
            DeletedKey deletedKey = keyClient.beginDeleteKey(keyToDeleteAndGet.getName()).getFinalResult();
            DeletedKey retrievedDeletedKey = keyClient.getDeletedKey(keyToDeleteAndGet.getName());

            assertNotNull(retrievedDeletedKey.getDeletedOn());
            assertNotNull(retrievedDeletedKey.getRecoveryId());
            assertNotNull(retrievedDeletedKey.getScheduledPurgeDate());
            assertEquals(keyToDeleteAndGet.getName(), retrievedDeletedKey.getName());
        });
    }

    /**
     * Tests that a deleted key can be recovered from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            KeyVaultKey createdKey = keyClient.createKey(keyToDeleteAndRecover);
            DeletedKey deletedKey = keyClient.beginDeleteKey(keyToDeleteAndRecover.getName()).getFinalResult();
            KeyVaultKey recoveredKey = keyClient.beginRecoverDeletedKey(keyToDeleteAndRecover.getName()).getFinalResult();

            assertEquals(keyToDeleteAndRecover.getName(), recoveredKey.getName());
            assertEquals(keyToDeleteAndRecover.getKeyType(), recoveredKey.getKey().getKeyType());
        });
    }

    /**
     * Tests that a deleted key can be permanently purged from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void purgeDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        purgeDeletedKeyRunner((keyToDeleteAndPurge) -> {
            KeyVaultKey createdKey = keyClient.createKey(keyToDeleteAndPurge);
            DeletedKey deletedKey = keyClient.beginDeleteKey(keyToDeleteAndPurge.getName()).getFinalResult();

            keyClient.purgeDeletedKey(keyToDeleteAndPurge.getName());

            sleepIfRunningAgainstService(10000);

            HttpResponseException exception = assertThrows(HttpResponseException.class,
                () -> keyClient.getDeletedKey(keyToDeleteAndPurge.getName()));
            assertEquals(HttpURLConnection.HTTP_NOT_FOUND, exception.getResponse().getStatusCode());
        });
    }

    /**
     * Tests that keys can be listed from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        listKeysRunner((keys) -> {
            List<KeyProperties> output = new ArrayList<>();

            for (KeyProperties actualKey : keyClient.listPropertiesOfKeys()) {
                if (keys.containsKey(actualKey.getName())) {
                    output.add(actualKey);
                }
            }

            assertEquals(keys.size(), output.size());
        });
    }

    /**
     * Tests that key versions can be listed from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeyVersions(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        listKeyVersionsRunner((keyVersions) -> {
            List<KeyProperties> output = new ArrayList<>();

            for (KeyProperties actualKey : keyClient.listPropertiesOfKeyVersions(keyVersions.get(0).getName())) {
                output.add(actualKey);
            }

            assertEquals(keyVersions.size(), output.size());
        });
    }

    /**
     * Tests that deleted keys can be listed from the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        listDeletedKeysRunner((deletedKeys) -> {
            List<DeletedKey> output = new ArrayList<>();

            for (DeletedKey actualKey : keyClient.listDeletedKeys()) {
                if (deletedKeys.containsKey(actualKey.getName())) {
                    output.add(actualKey);
                }
            }

            assertEquals(deletedKeys.size(), output.size());
        });
    }

    private void assertKeyEquals(CreateKeyOptions expected, KeyVaultKey actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getKeyType(), actual.getKey().getKeyType());
        assertEquals(expected.getExpiresOn(), actual.getProperties().getExpiresOn());
        assertEquals(expected.getNotBefore(), actual.getProperties().getNotBefore());
        assertEquals(expected.getTags(), actual.getProperties().getTags());
    }

    private void assertRsaKeyEquals(CreateRsaKeyOptions expected, KeyVaultKey actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getExpiresOn(), actual.getProperties().getExpiresOn());
        assertEquals(expected.getNotBefore(), actual.getProperties().getNotBefore());
        assertEquals(expected.getTags(), actual.getProperties().getTags());
    }
}
