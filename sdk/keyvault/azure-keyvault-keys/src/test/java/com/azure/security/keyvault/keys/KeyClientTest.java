// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyBase;
import com.azure.security.keyvault.keys.models.KeyCreateOptions;
import com.azure.security.keyvault.keys.models.webkey.KeyType;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KeyClientTest extends KeyClientTestBase {

    private KeyClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(credentials -> new KeyClientBuilder()
                .credential(credentials)
                .endpoint(getEndpoint())
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildClient());
        } else {
            client = clientSetup(credentials -> new KeyClientBuilder()
                .credential(credentials)
                .endpoint(getEndpoint())
                .httpClient(HttpClient.createDefault().wiretap(true))
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .addPolicy(interceptorManager.getRecordPolicy())
                .addPolicy(new RetryPolicy())
                .buildClient());
        }
    }

    /**
     * Tests that a key can be created in the key vault.
     */
    public void setKey() {
        setKeyRunner((expected) -> assertKeyEquals(expected, client.createKey(expected)));
    }

    /**
     * Tests that an attempt to create a key with empty string name throws an error.
     */
    public void setKeyEmptyName() {
        assertRestException(() -> client.createKey("", KeyType.RSA), ResourceModifiedException.class, HttpResponseStatus.BAD_REQUEST.code());
    }

    /**
     * Tests that we cannot create keys when key type is null.
     */
    public void setKeyNullType() {
        setKeyEmptyValueRunner((key) -> {
            assertRestException(() -> client.createKey(key.name(), key.keyType()), ResourceModifiedException.class, HttpResponseStatus.BAD_REQUEST.code());
        });
    }

    /**
     * Verifies that an exception is thrown when null key object is passed for creation.
     */
    public void setKeyNull() {
        assertRunnableThrowsException(() -> client.createKey(null), NullPointerException.class);
    }

    /**
     * Tests that a key is able to be updated when it exists.
     */
    public void updateKey() {
        updateKeyRunner((original, updated) -> {
            assertKeyEquals(original, client.createKey(original));
            Key keyToUpdate = client.getKey(original.name());
            client.updateKey(keyToUpdate.expires(updated.expires()));
            assertKeyEquals(updated, client.getKey(original.name()));
        });
    }

    /**
     * Tests that a key is able to be updated when it is disabled.
     */
    public void updateDisabledKey() {
        updateDisabledKeyRunner((original, updated) -> {
            assertKeyEquals(original, client.createKey(original));
            Key keyToUpdate = client.getKey(original.name());
            client.updateKey(keyToUpdate.expires(updated.expires()));
            assertKeyEquals(updated, client.getKey(original.name()));
        });
    }

    /**
     * Tests that an existing key can be retrieved.
     */
    public void getKey() {
        getKeyRunner((original) -> {
            client.createKey(original);
            assertKeyEquals(original, client.getKey(original.name()));
        });
    }

    /**
     * Tests that a specific version of the key can be retrieved.
     */
    public void getKeySpecificVersion() {
        getKeySpecificVersionRunner((key, keyWithNewVal) -> {
            Key keyVersionOne = client.createKey(key);
            Key keyVersionTwo = client.createKey(keyWithNewVal);
            assertKeyEquals(key, client.getKey(keyVersionOne.name(), keyVersionOne.version()));
            assertKeyEquals(keyWithNewVal, client.getKey(keyVersionTwo.name(), keyVersionTwo.version()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing key throws an error.
     */
    public void getKeyNotFound() {
        assertRestException(() -> client.getKey("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that an existing key can be deleted.
     */
    public void deleteKey() {
        deleteKeyRunner((keyToDelete) -> {
            assertKeyEquals(keyToDelete,  client.createKey(keyToDelete));
            DeletedKey deletedKey = client.deleteKey(keyToDelete.name());
            pollOnKeyDeletion(keyToDelete.name());
            assertNotNull(deletedKey.deletedDate());
            assertNotNull(deletedKey.recoveryId());
            assertNotNull(deletedKey.scheduledPurgeDate());
            assertEquals(keyToDelete.name(), deletedKey.name());
            client.purgeDeletedKey(keyToDelete.name());
            pollOnKeyPurge(keyToDelete.name());
        });
    }

    public void deleteKeyNotFound() {
        assertRestException(() -> client.deleteKey("non-existing"), ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }


    /**
     * Tests that an attempt to retrieve a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    public void getDeletedKeyNotFound() {
        assertRestException(() -> client.getDeletedKey("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }


    /**
     * Tests that a deleted key can be recovered on a soft-delete enabled vault.
     */
    public void recoverDeletedKey() {
        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            assertKeyEquals(keyToDeleteAndRecover, client.createKey(keyToDeleteAndRecover));
            assertNotNull(client.deleteKey(keyToDeleteAndRecover.name()));
            pollOnKeyDeletion(keyToDeleteAndRecover.name());
            Key recoveredKey = client.recoverDeletedKey(keyToDeleteAndRecover.name());
            assertEquals(keyToDeleteAndRecover.name(), recoveredKey.name());
            assertEquals(keyToDeleteAndRecover.notBefore(), recoveredKey.notBefore());
            assertEquals(keyToDeleteAndRecover.expires(), recoveredKey.expires());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted key throws an error on a soft-delete enabled vault.
     */
    public void recoverDeletedKeyNotFound() {
        assertRestException(() -> client.recoverDeletedKey("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    public void backupKey() {
        backupKeyRunner((keyToBackup) -> {
            assertKeyEquals(keyToBackup, client.createKey(keyToBackup));
            byte[] backupBytes = (client.backupKey(keyToBackup.name()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    /**
     * Tests that an attempt to backup a non existing key throws an error.
     */
    public void backupKeyNotFound() {
        assertRestException(() -> client.backupKey("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    public void restoreKey() {
        restoreKeyRunner((keyToBackupAndRestore) -> {
            assertKeyEquals(keyToBackupAndRestore, client.createKey(keyToBackupAndRestore));
            byte[] backupBytes = (client.backupKey(keyToBackupAndRestore.name()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
            client.deleteKey(keyToBackupAndRestore.name());
            pollOnKeyDeletion(keyToBackupAndRestore.name());
            client.purgeDeletedKey(keyToBackupAndRestore.name());
            pollOnKeyPurge(keyToBackupAndRestore.name());
            sleepInRecordMode(60000);
            Key restoredKey = client.restoreKey(backupBytes);
            assertEquals(keyToBackupAndRestore.name(), restoredKey.name());
            assertEquals(keyToBackupAndRestore.expires(), restoredKey.expires());
        });
    }

    /**
     * Tests that an attempt to restore a key from malformed backup bytes throws an error.
     */
    public void restoreKeyFromMalformedBackup() {
        byte[] keyBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreKey(keyBackupBytes), ResourceModifiedException.class, HttpResponseStatus.BAD_REQUEST.code());
    }

    /**
     * Tests that keys can be listed in the key vault.
     */
    public void listKeys() {
        listKeysRunner((keys) -> {
            HashMap<String, KeyCreateOptions> keysToList = keys;
            for (KeyCreateOptions key :  keysToList.values()) {
                assertKeyEquals(key, client.createKey(key));
                sleepInRecordMode(5000);
            }

            for (KeyBase actualKey : client.listKeys()) {
                if (keys.containsKey(actualKey.name())) {
                    KeyCreateOptions expectedKey = keys.get(actualKey.name());
                    assertEquals(expectedKey.expires(), actualKey.expires());
                    assertEquals(expectedKey.notBefore(), actualKey.notBefore());
                    keys.remove(actualKey.name());
                }
            }
            assertEquals(0, keys.size());
        });
    }

    /**
     * Tests that a deleted key can be retrieved on a soft-delete enabled vault.
     */
    public void getDeletedKey() {
        getDeletedKeyRunner((keyToDeleteAndGet) -> {
            assertKeyEquals(keyToDeleteAndGet, client.createKey(keyToDeleteAndGet));
            assertNotNull(client.deleteKey(keyToDeleteAndGet.name()));
            pollOnKeyDeletion(keyToDeleteAndGet.name());
            sleepInRecordMode(30000);
            DeletedKey deletedKey = client.getDeletedKey(keyToDeleteAndGet.name());
            assertNotNull(deletedKey.deletedDate());
            assertNotNull(deletedKey.recoveryId());
            assertNotNull(deletedKey.scheduledPurgeDate());
            assertEquals(keyToDeleteAndGet.name(), deletedKey.name());
            client.purgeDeletedKey(keyToDeleteAndGet.name());
            pollOnKeyPurge(keyToDeleteAndGet.name());
            sleepInRecordMode(10000);
        });
    }
//
//
    /**
     * Tests that deleted keys can be listed in the key vault.
     */
    @Override
    public void listDeletedKeys() {
        listDeletedKeysRunner((keys) -> {
            HashMap<String, KeyCreateOptions> keysToDelete = keys;
            for (KeyCreateOptions key : keysToDelete.values()) {
                assertKeyEquals(key, client.createKey(key));
            }

            for (KeyCreateOptions key : keysToDelete.values()) {
                client.deleteKey(key.name());
                pollOnKeyDeletion(key.name());
            }
            sleepInRecordMode(60000);
            Iterable<DeletedKey> deletedKeys =  client.listDeletedKeys();
            for (DeletedKey actualKey : deletedKeys) {
                if (keysToDelete.containsKey(actualKey.name())) {
                    assertNotNull(actualKey.deletedDate());
                    assertNotNull(actualKey.recoveryId());
                    keysToDelete.remove(actualKey.name());
                }
            }

            assertEquals(0, keysToDelete.size());

            for (DeletedKey deletedKey : deletedKeys) {
                client.purgeDeletedKey(deletedKey.name());
                pollOnKeyPurge(deletedKey.name());
            }
            sleepInRecordMode(10000);
        });
    }

    /**
     * Tests that key versions can be listed in the key vault.
     */
    @Override
    public void listKeyVersions() {
        listKeyVersionsRunner((keys) -> {
            List<KeyCreateOptions> keyVersions = keys;
            String keyName = null;
            for (KeyCreateOptions key : keyVersions) {
                keyName = key.name();
                assertKeyEquals(key, client.createKey(key));
            }

            Iterable<KeyBase> keyVersionsOutput =  client.listKeyVersions(keyName);
            List<KeyBase> keyVersionsList = new ArrayList<>();
            keyVersionsOutput.forEach(keyVersionsList::add);
            assertEquals(keyVersions.size(), keyVersionsList.size());

            client.deleteKey(keyName);
            pollOnKeyDeletion(keyName);

            client.purgeDeletedKey(keyName);
            pollOnKeyPurge(keyName);
        });

    }

    private DeletedKey pollOnKeyDeletion(String keyName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 30) {
            DeletedKey deletedKey = null;
            try {
                deletedKey = client.getDeletedKey(keyName);
            } catch (ResourceNotFoundException e) {
            }
            if (deletedKey == null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
                continue;
            } else {
                return deletedKey;
            }
        }
        System.err.printf("Deleted Key %s not found \n", keyName);
        return null;
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
