// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyCreateOptions;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import java.net.HttpURLConnection;
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
            client = clientSetup(pipeline -> new KeyClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(pipeline)
                .buildClient());
        } else {
            client = clientSetup(pipeline -> new KeyClientBuilder()
                .endpoint(getEndpoint())
                .pipeline(pipeline)
                .buildClient());
        }
    }

    /**
     * Tests that a key can be getCreated in the key vault.
     */
    public void setKey() {
        setKeyRunner((expected) -> assertKeyEquals(expected, client.createKey(expected)));
    }

    /**
     * Tests that an attempt to create a key with empty string getName throws an error.
     */
    public void setKeyEmptyName() {
        assertRestException(() -> client.createKey("", KeyType.RSA), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that we cannot create keys when key type is null.
     */
    public void setKeyNullType() {
        setKeyEmptyValueRunner((key) -> {
            assertRestException(() -> client.createKey(key.getName(), key.keyType()), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
        });
    }

    /**
     * Verifies that an exception is thrown when null key object is passed for creation.
     */
    public void setKeyNull() {
        assertRunnableThrowsException(() -> client.createKey(null), NullPointerException.class);
    }

    /**
     * Tests that a key is able to be getUpdated when it exists.
     */
    public void updateKey() {
        updateKeyRunner((original, updated) -> {
            assertKeyEquals(original, client.createKey(original));
            Key keyToUpdate = client.getKey(original.getName());
            client.updateKeyProperties(keyToUpdate.getProperties().setExpires(updated.getExpires()));
            assertKeyEquals(updated, client.getKey(original.getName()));
        });
    }

    /**
     * Tests that a key is able to be getUpdated when it is disabled.
     */
    public void updateDisabledKey() {
        updateDisabledKeyRunner((original, updated) -> {
            assertKeyEquals(original, client.createKey(original));
            Key keyToUpdate = client.getKey(original.getName());
            client.updateKeyProperties(keyToUpdate.getProperties().setExpires(updated.getExpires()));
            assertKeyEquals(updated, client.getKey(original.getName()));
        });
    }

    /**
     * Tests that an existing key can be retrieved.
     */
    public void getKey() {
        getKeyRunner((original) -> {
            client.createKey(original);
            assertKeyEquals(original, client.getKey(original.getName()));
        });
    }

    /**
     * Tests that a specific getVersion of the key can be retrieved.
     */
    public void getKeySpecificVersion() {
        getKeySpecificVersionRunner((key, keyWithNewVal) -> {
            Key keyVersionOne = client.createKey(key);
            Key keyVersionTwo = client.createKey(keyWithNewVal);
            assertKeyEquals(key, client.getKey(keyVersionOne.getName(), keyVersionOne.getProperties().getVersion()));
            assertKeyEquals(keyWithNewVal, client.getKey(keyVersionTwo.getName(), keyVersionTwo.getProperties().getVersion()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing key throws an error.
     */
    public void getKeyNotFound() {
        assertRestException(() -> client.getKey("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an existing key can be deleted.
     */
    public void deleteKey() {
        deleteKeyRunner((keyToDelete) -> {
            assertKeyEquals(keyToDelete,  client.createKey(keyToDelete));
            DeletedKey deletedKey = client.deleteKey(keyToDelete.getName());
            pollOnKeyDeletion(keyToDelete.getName());
            assertNotNull(deletedKey.getDeletedDate());
            assertNotNull(deletedKey.getRecoveryId());
            assertNotNull(deletedKey.getScheduledPurgeDate());
            assertEquals(keyToDelete.getName(), deletedKey.getName());
            client.purgeDeletedKey(keyToDelete.getName());
            pollOnKeyPurge(keyToDelete.getName());
        });
    }

    public void deleteKeyNotFound() {
        assertRestException(() -> client.deleteKey("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }


    /**
     * Tests that an attempt to retrieve a non existing deleted key throws an error on a soft-delete setEnabled vault.
     */
    public void getDeletedKeyNotFound() {
        assertRestException(() -> client.getDeletedKey("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }


    /**
     * Tests that a deleted key can be recovered on a soft-delete setEnabled vault.
     */
    public void recoverDeletedKey() {
        recoverDeletedKeyRunner((keyToDeleteAndRecover) -> {
            assertKeyEquals(keyToDeleteAndRecover, client.createKey(keyToDeleteAndRecover));
            assertNotNull(client.deleteKey(keyToDeleteAndRecover.getName()));
            pollOnKeyDeletion(keyToDeleteAndRecover.getName());
            Key recoveredKey = client.recoverDeletedKey(keyToDeleteAndRecover.getName());
            assertEquals(keyToDeleteAndRecover.getName(), recoveredKey.getName());
            assertEquals(keyToDeleteAndRecover.notBefore(), recoveredKey.getProperties().getNotBefore());
            assertEquals(keyToDeleteAndRecover.getExpires(), recoveredKey.getProperties().getExpires());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted key throws an error on a soft-delete setEnabled vault.
     */
    public void recoverDeletedKeyNotFound() {
        assertRestException(() -> client.recoverDeletedKey("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    public void backupKey() {
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
    public void backupKeyNotFound() {
        assertRestException(() -> client.backupKey("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a key can be backed up in the key vault.
     */
    public void restoreKey() {
        restoreKeyRunner((keyToBackupAndRestore) -> {
            assertKeyEquals(keyToBackupAndRestore, client.createKey(keyToBackupAndRestore));
            byte[] backupBytes = (client.backupKey(keyToBackupAndRestore.getName()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
            client.deleteKey(keyToBackupAndRestore.getName());
            pollOnKeyDeletion(keyToBackupAndRestore.getName());
            client.purgeDeletedKey(keyToBackupAndRestore.getName());
            pollOnKeyPurge(keyToBackupAndRestore.getName());
            sleepInRecordMode(60000);
            Key restoredKey = client.restoreKey(backupBytes);
            assertEquals(keyToBackupAndRestore.getName(), restoredKey.getName());
            assertEquals(keyToBackupAndRestore.getExpires(), restoredKey.getProperties().getExpires());
        });
    }

    /**
     * Tests that an attempt to restore a key from malformed backup bytes throws an error.
     */
    public void restoreKeyFromMalformedBackup() {
        byte[] keyBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreKey(keyBackupBytes), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
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

            for (KeyProperties actualKey : client.listKeys()) {
                if (keys.containsKey(actualKey.getName())) {
                    KeyCreateOptions expectedKey = keys.get(actualKey.getName());
                    assertEquals(expectedKey.getExpires(), actualKey.getExpires());
                    assertEquals(expectedKey.notBefore(), actualKey.getNotBefore());
                    keys.remove(actualKey.getName());
                }
            }
            assertEquals(0, keys.size());
        });
    }

    /**
     * Tests that a deleted key can be retrieved on a soft-delete setEnabled vault.
     */
    public void getDeletedKey() {
        getDeletedKeyRunner((keyToDeleteAndGet) -> {
            assertKeyEquals(keyToDeleteAndGet, client.createKey(keyToDeleteAndGet));
            assertNotNull(client.deleteKey(keyToDeleteAndGet.getName()));
            pollOnKeyDeletion(keyToDeleteAndGet.getName());
            sleepInRecordMode(30000);
            DeletedKey deletedKey = client.getDeletedKey(keyToDeleteAndGet.getName());
            assertNotNull(deletedKey.getDeletedDate());
            assertNotNull(deletedKey.getRecoveryId());
            assertNotNull(deletedKey.getScheduledPurgeDate());
            assertEquals(keyToDeleteAndGet.getName(), deletedKey.getName());
            client.purgeDeletedKey(keyToDeleteAndGet.getName());
            pollOnKeyPurge(keyToDeleteAndGet.getName());
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
                client.deleteKey(key.getName());
                pollOnKeyDeletion(key.getName());
            }
            sleepInRecordMode(60000);
            Iterable<DeletedKey> deletedKeys =  client.listDeletedKeys();
            for (DeletedKey actualKey : deletedKeys) {
                if (keysToDelete.containsKey(actualKey.getName())) {
                    assertNotNull(actualKey.getDeletedDate());
                    assertNotNull(actualKey.getRecoveryId());
                    keysToDelete.remove(actualKey.getName());
                }
            }

            assertEquals(0, keysToDelete.size());

            for (DeletedKey deletedKey : deletedKeys) {
                client.purgeDeletedKey(deletedKey.getName());
                pollOnKeyPurge(deletedKey.getName());
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
                keyName = key.getName();
                assertKeyEquals(key, client.createKey(key));
            }

            Iterable<KeyProperties> keyVersionsOutput =  client.listKeyVersions(keyName);
            List<KeyProperties> keyVersionsList = new ArrayList<>();
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
