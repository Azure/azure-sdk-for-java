// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.ReleaseKeyResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class KeyClientTest extends KeyClientTestBase {
    protected KeyClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    protected void createKeyClient(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        HttpPipeline httpPipeline = getHttpPipeline(httpClient, serviceVersion);
        KeyAsyncClient asyncClient = spy(new KeyClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline)
            .serviceVersion(serviceVersion)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(asyncClient.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }

        client = new KeyClient(asyncClient);
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        setKeyRunner((expected) -> assertKeyEquals(expected, client.createKey(expected)));
    }

    /**
     * Tests that an RSA key is created.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void createRsaKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        createRsaKeyRunner((expected) -> assertKeyEquals(expected, client.createRsaKey(expected)));
    }

    /**
     * Tests that an attempt to create a key with empty string name throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setKeyEmptyName(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        final KeyType keyType;

        if (isManagedHsmTest) {
            keyType = KeyType.RSA_HSM;
        } else {
            keyType = KeyType.RSA;
        }

        assertRestException(() -> client.createKey("", keyType), ResourceModifiedException.class,
            HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that we cannot create keys when key type is null.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setKeyNullType(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        setKeyEmptyValueRunner((key) -> {
            assertRestException(() -> client.createKey(key.getName(), key.getKeyType()), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
        });
    }

    /**
     * Verifies that an exception is thrown when null key object is passed for creation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setKeyNull(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        assertRunnableThrowsException(() -> client.createKey(null), NullPointerException.class);
        assertRunnableThrowsException(() -> client.createKey(null), NullPointerException.class);
    }

    /**
     * Tests that a key is able to be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        updateKeyRunner((original, updated) -> {
            assertKeyEquals(original, client.createKey(original));
            KeyVaultKey keyToUpdate = client.getKey(original.getName());
            client.updateKeyProperties(keyToUpdate.getProperties().setExpiresOn(updated.getExpiresOn()));
            assertKeyEquals(updated, client.getKey(original.getName()));
        });
    }

    /**
     * Tests that a key is able to be updated when it is disabled.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        updateDisabledKeyRunner((original, updated) -> {
            assertKeyEquals(original, client.createKey(original));
            KeyVaultKey keyToUpdate = client.getKey(original.getName());
            client.updateKeyProperties(keyToUpdate.getProperties().setExpiresOn(updated.getExpiresOn()));
            assertKeyEquals(updated, client.getKey(original.getName()));
        });
    }

    /**
     * Tests that an existing key can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        getKeyRunner((original) -> {
            client.createKey(original);
            assertKeyEquals(original, client.getKey(original.getName()));
        });
    }

    /**
     * Tests that a specific version of the key can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKeySpecificVersion(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        getKeySpecificVersionRunner((key, keyWithNewVal) -> {
            KeyVaultKey keyVersionOne = client.createKey(key);
            KeyVaultKey keyVersionTwo = client.createKey(keyWithNewVal);
            assertKeyEquals(key, client.getKey(keyVersionOne.getName(), keyVersionOne.getProperties().getVersion()));
            assertKeyEquals(keyWithNewVal, client.getKey(keyVersionTwo.getName(), keyVersionTwo.getProperties().getVersion()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing key throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        assertRestException(() -> client.getKey("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an existing key can be deleted.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        deleteKeyRunner((keyToDelete) -> {
            sleepInRecordMode(30000);
            assertKeyEquals(keyToDelete,  client.createKey(keyToDelete));

            SyncPoller<DeletedKey, Void> deletedKeyPoller = client.beginDeleteKey(keyToDelete.getName());

            PollResponse<DeletedKey> pollResponse = deletedKeyPoller.poll();
            DeletedKey deletedKey = pollResponse.getValue();

            // Key is being deleted on server.
            while (!pollResponse.getStatus().isComplete()) {
                sleepInRecordMode(10000);
                pollResponse = deletedKeyPoller.poll();
            }

            assertNotNull(deletedKey.getDeletedOn());
            assertNotNull(deletedKey.getRecoveryId());
            assertNotNull(deletedKey.getScheduledPurgeDate());
            assertEquals(keyToDelete.getName(), deletedKey.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        assertRestException(() -> client.beginDeleteKey("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        assertRestException(() -> client.getDeletedKey("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }


    /**
     * Tests that a deleted key can be recovered on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            assertKeyEquals(keyToDeleteAndRecover, client.createKey(keyToDeleteAndRecover));
            SyncPoller<DeletedKey, Void> poller = client.beginDeleteKey(keyToDeleteAndRecover.getName());
            PollResponse<DeletedKey> pollResponse = poller.poll();
            while (!pollResponse.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                pollResponse = poller.poll();
            }
            assertNotNull(pollResponse.getValue());

            SyncPoller<KeyVaultKey, Void> recoverPoller = client.beginRecoverDeletedKey(keyToDeleteAndRecover.getName());
            PollResponse<KeyVaultKey> recoverPollResponse = recoverPoller.poll();

            KeyVaultKey recoveredKey = recoverPollResponse.getValue();
            //
            recoverPollResponse = recoverPoller.poll();
            while (!recoverPollResponse.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                recoverPollResponse = recoverPoller.poll();
            }

            assertEquals(keyToDeleteAndRecover.getName(), recoveredKey.getName());
            assertEquals(keyToDeleteAndRecover.getNotBefore(), recoveredKey.getProperties().getNotBefore());
            assertEquals(keyToDeleteAndRecover.getExpiresOn(), recoveredKey.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        assertRestException(() -> client.beginRecoverDeletedKey("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        backupKeyRunner((keyToBackup) -> {
            assertKeyEquals(keyToBackup, client.createKey(keyToBackup));
            byte[] backupBytes = (client.backupKey(keyToBackup.getName()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    /**
     * Tests that an attempt to backup a non existing key throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupKeyNotFound(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        assertRestException(() -> client.backupKey("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        restoreKeyRunner((keyToBackupAndRestore) -> {
            assertKeyEquals(keyToBackupAndRestore, client.createKey(keyToBackupAndRestore));
            byte[] backupBytes = (client.backupKey(keyToBackupAndRestore.getName()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);

            SyncPoller<DeletedKey, Void> poller = client.beginDeleteKey(keyToBackupAndRestore.getName());
            PollResponse<DeletedKey> pollResponse = poller.poll();

            while (!pollResponse.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                pollResponse = poller.poll();
            }

            client.purgeDeletedKey(keyToBackupAndRestore.getName());
            pollOnKeyPurge(keyToBackupAndRestore.getName());
            sleepInRecordMode(60000);
            KeyVaultKey restoredKey = client.restoreKeyBackup(backupBytes);
            assertEquals(keyToBackupAndRestore.getName(), restoredKey.getName());
            assertEquals(keyToBackupAndRestore.getExpiresOn(), restoredKey.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to restore a key from malformed backup bytes throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreKeyFromMalformedBackup(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        byte[] keyBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreKeyBackup(keyBackupBytes), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that keys can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        listKeysRunner((keys) -> {
            HashMap<String, CreateKeyOptions> keysToList = keys;
            for (CreateKeyOptions key :  keysToList.values()) {
                assertKeyEquals(key, client.createKey(key));
                sleepInRecordMode(5000);
            }

            for (KeyProperties actualKey : client.listPropertiesOfKeys()) {
                if (keys.containsKey(actualKey.getName())) {
                    CreateKeyOptions expectedKey = keys.get(actualKey.getName());
                    assertEquals(expectedKey.getExpiresOn(), actualKey.getExpiresOn());
                    assertEquals(expectedKey.getNotBefore(), actualKey.getNotBefore());
                    keys.remove(actualKey.getName());
                }
            }
            assertEquals(0, keys.size());
        });
    }

    /**
     * Tests that a deleted key can be retrieved on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        getDeletedKeyRunner((keyToDeleteAndGet) -> {
            assertKeyEquals(keyToDeleteAndGet, client.createKey(keyToDeleteAndGet));
            SyncPoller<DeletedKey, Void> poller = client.beginDeleteKey(keyToDeleteAndGet.getName());
            PollResponse<DeletedKey>  pollResponse = poller.poll();
            while (!pollResponse.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                pollResponse = poller.poll();
            }
            sleepInRecordMode(30000);
            DeletedKey deletedKey = client.getDeletedKey(keyToDeleteAndGet.getName());
            assertNotNull(deletedKey.getDeletedOn());
            assertNotNull(deletedKey.getRecoveryId());
            assertNotNull(deletedKey.getScheduledPurgeDate());
            assertEquals(keyToDeleteAndGet.getName(), deletedKey.getName());
        });
    }

    /**
     * Tests that deleted keys can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedKeys(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);

        /*if (!interceptorManager.isPlaybackMode()) {
            return;
        }*/

        listDeletedKeysRunner((keys) -> {
            HashMap<String, CreateKeyOptions> keysToDelete = keys;

            for (CreateKeyOptions key : keysToDelete.values()) {
                assertKeyEquals(key, client.createKey(key));
            }

            for (CreateKeyOptions key : keysToDelete.values()) {
                SyncPoller<DeletedKey, Void> poller = client.beginDeleteKey(key.getName());
                PollResponse<DeletedKey> pollResponse = poller.poll();
                while (!pollResponse.getStatus().isComplete()) {
                    sleepInRecordMode(1000);
                    pollResponse = poller.poll();
                }
            }

            sleepInRecordMode(300000);

            Iterable<DeletedKey> deletedKeys = client.listDeletedKeys();
            assertTrue(deletedKeys.iterator().hasNext());

            for (DeletedKey deletedKey : deletedKeys) {
                assertNotNull(deletedKey.getDeletedOn());
                assertNotNull(deletedKey.getRecoveryId());
            }
        });
    }

    /**
     * Tests that key versions can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listKeyVersions(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        createKeyClient(httpClient, serviceVersion);
        listKeyVersionsRunner((keys) -> {
            List<CreateKeyOptions> keyVersions = keys;
            String keyName = null;
            for (CreateKeyOptions key : keyVersions) {
                keyName = key.getName();
                sleepInRecordMode(4000);
                assertKeyEquals(key, client.createKey(key));
            }

            Iterable<KeyProperties> keyVersionsOutput =  client.listPropertiesOfKeyVersions(keyName);
            List<KeyProperties> keyVersionsList = new ArrayList<>();
            keyVersionsOutput.forEach(keyVersionsList::add);
            assertEquals(keyVersions.size(), keyVersionsList.size());
        });
    }

    /**
     * Tests that an existing key can be released.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void releaseKey(HttpClient httpClient, KeyServiceVersion serviceVersion) {
        // TODO: Remove assumption once Key Vault allows for creating exportable keys.
        Assumptions.assumeTrue(isManagedHsmTest);

        createKeyClient(httpClient, serviceVersion);
        releaseKeyRunner((keyToRelease, attestationUrl) -> {
            assertKeyEquals(keyToRelease,  client.createRsaKey(keyToRelease));

            String target = "testAttestationToken";

            if (getTestMode() != TestMode.PLAYBACK) {
                if (!attestationUrl.endsWith("/")) {
                    attestationUrl = attestationUrl + "/";
                }

                try {
                    target = getAttestationToken(attestationUrl + "generate-test-token");
                } catch (IOException e) {
                    fail("Found error when deserializing attestation token.", e);
                }
            }

            ReleaseKeyResult releaseKeyResult = client.releaseKey(keyToRelease.getName(), target);

            assertNotNull(releaseKeyResult.getValue());
        });
    }

    private DeletedKey pollOnKeyPurge(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 10) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKey(keyName);
            } catch (ResourceNotFoundException e) {
            }
            if (deletedKey != null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
                continue;
            } else {
                return deletedKey;
            }
        }
        System.err.printf("Deleted Key %s was not purged \n", keyName);
        return null;
    }
}
