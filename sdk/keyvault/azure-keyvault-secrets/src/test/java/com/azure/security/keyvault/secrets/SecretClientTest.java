// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SecretClientTest extends SecretClientTestBase {

    private SecretClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(pipeline -> new SecretClientBuilder()
                .pipeline(pipeline)
                .endpoint(getEndpoint())
                .buildClient());
        } else {
            client = clientSetup(pipeline -> new SecretClientBuilder()
                .pipeline(pipeline)
                .endpoint(getEndpoint())
                .buildClient());
        }
    }

    /**
     * Tests that a secret can be created in the key vault.
     */
    public void setSecret() {
        setSecretRunner((expected) -> assertSecretEquals(expected, client.setSecret(expected)));
    }

    /**
     * Tests that we cannot create a secret when the secret is an empty string.
     */
    public void setSecretEmptyName() {
        assertRestException(() -> client.setSecret("", "A value"), HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    public void setSecretEmptyValue() {
        setSecretEmptyValueRunner((secret) -> {
            assertSecretEquals(secret, client.setSecret(secret.getName(), secret.getValue()));
            assertSecretEquals(secret, client.getSecret(secret.getName()));
        });
    }

    /**
     * Verifies that an exception is thrown when null secret object is passed for creation.
     */
    public void setSecretNull() {
        assertRunnableThrowsException(() -> client.setSecret(null), NullPointerException.class);
    }

    /**
     * Tests that a secret is able to be updated when it exists.
     */
    public void updateSecret() {
        updateSecretRunner((original, updated) -> {
            assertSecretEquals(original, client.setSecret(original));
            Secret secretToUpdate = client.getSecret(original.getName());
            client.updateSecret(secretToUpdate.setExpires(updated.getExpires()));
            assertSecretEquals(updated, client.getSecret(original.getName()));
        });
    }

    /**
     * Tests that a secret is not able to be updated when it is disabled. 403 error is expected.
     */
    public void updateDisabledSecret() {
        updateDisabledSecretRunner((original, updated) -> {
            assertSecretEquals(original, client.setSecret(original));
            assertRestException(() -> client.getSecret(original.getName()), ResourceModifiedException.class, HttpURLConnection.HTTP_FORBIDDEN);
        });
    }

    /**
     * Tests that an existing secret can be retrieved.
     */
    public void getSecret() {
        getSecretRunner((original) -> {
            client.setSecret(original);
            assertSecretEquals(original, client.getSecret(original.getName()));
        });
    }

    /**
     * Tests that a specific version of the secret can be retrieved.
     */
    public void getSecretSpecificVersion() {
        getSecretSpecificVersionRunner((secret, secretWithNewVal) -> {
            Secret secretVersionOne = client.setSecret(secret);
            Secret secretVersionTwo = client.setSecret(secretWithNewVal);
            assertSecretEquals(secret, client.getSecret(secretVersionOne.getName(), secretVersionOne.getVersion()));
            assertSecretEquals(secretWithNewVal, client.getSecret(secretVersionTwo.getName(), secretVersionTwo.getVersion()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    public void getSecretNotFound() {
        assertRestException(() -> client.getSecret("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an existing secret can be deleted.
     */
    public void deleteSecret() {
        deleteSecretRunner((secretToDelete) -> {
            assertSecretEquals(secretToDelete,  client.setSecret(secretToDelete));
            DeletedSecret deletedSecret = client.deleteSecret(secretToDelete.getName());
            pollOnSecretDeletion(secretToDelete.getName());
            assertNotNull(deletedSecret.getDeletedDate());
            assertNotNull(deletedSecret.getRecoveryId());
            assertNotNull(deletedSecret.getScheduledPurgeDate());
            assertEquals(secretToDelete.getName(), deletedSecret.getName());
            client.purgeDeletedSecret(secretToDelete.getName());
            pollOnSecretPurge(secretToDelete.getName());
        });
    }

    public void deleteSecretNotFound() {
        assertRestException(() -> client.deleteSecret("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    public void getDeletedSecret() {
        getDeletedSecretRunner((secretToDeleteAndGet) -> {
            assertSecretEquals(secretToDeleteAndGet, client.setSecret(secretToDeleteAndGet));
            assertNotNull(client.deleteSecret(secretToDeleteAndGet.getName()));
            pollOnSecretDeletion(secretToDeleteAndGet.getName());
            sleepInRecordMode(30000);
            DeletedSecret deletedSecret = client.getDeletedSecret(secretToDeleteAndGet.getName());
            assertNotNull(deletedSecret.getDeletedDate());
            assertNotNull(deletedSecret.getRecoveryId());
            assertNotNull(deletedSecret.getScheduledPurgeDate());
            assertEquals(secretToDeleteAndGet.getName(), deletedSecret.getName());
            client.purgeDeletedSecret(secretToDeleteAndGet.getName());
            pollOnSecretPurge(secretToDeleteAndGet.getName());
            sleepInRecordMode(10000);
        });
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void getDeletedSecretNotFound() {
        assertRestException(() -> client.getDeletedSecret("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }


    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    public void recoverDeletedSecret() {
        recoverDeletedSecretRunner((secretToDeleteAndRecover) -> {
            assertSecretEquals(secretToDeleteAndRecover, client.setSecret(secretToDeleteAndRecover));
            assertNotNull(client.deleteSecret(secretToDeleteAndRecover.getName()));
            pollOnSecretDeletion(secretToDeleteAndRecover.getName());
            Secret recoveredSecret = client.recoverDeletedSecret(secretToDeleteAndRecover.getName());
            assertEquals(secretToDeleteAndRecover.getName(), recoveredSecret.getName());
            assertEquals(secretToDeleteAndRecover.getNotBefore(), recoveredSecret.getNotBefore());
            assertEquals(secretToDeleteAndRecover.getExpires(), recoveredSecret.getExpires());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void recoverDeletedSecretNotFound() {
        assertRestException(() -> client.recoverDeletedSecret("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    public void backupSecret() {
        backupSecretRunner((secretToBackup) -> {
            assertSecretEquals(secretToBackup, client.setSecret(secretToBackup));
            byte[] backupBytes = (client.backupSecret(secretToBackup.getName()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    /**
     * Tests that an attempt to backup a non existing secret throws an error.
     */
    public void backupSecretNotFound() {
        assertRestException(() -> client.backupSecret("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    public synchronized void restoreSecret() {
        restoreSecretRunner((secretToBackupAndRestore) -> {
            assertSecretEquals(secretToBackupAndRestore, client.setSecret(secretToBackupAndRestore));
            byte[] backupBytes = (client.backupSecret(secretToBackupAndRestore.getName()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
            client.deleteSecret(secretToBackupAndRestore.getName());
            pollOnSecretDeletion(secretToBackupAndRestore.getName());
            client.purgeDeletedSecret(secretToBackupAndRestore.getName());
            pollOnSecretPurge(secretToBackupAndRestore.getName());
            sleepInRecordMode(60000);
            Secret restoredSecret = client.restoreSecret(backupBytes);
            assertEquals(secretToBackupAndRestore.getName(), restoredSecret.getName());
            assertEquals(secretToBackupAndRestore.getExpires(), restoredSecret.getExpires());
        });
    }

    /**
     * Tests that an attempt to restore a secret from malformed backup bytes throws an error.
     */
    public void restoreSecretFromMalformedBackup() {
        byte[] secretBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreSecret(secretBackupBytes), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that secrets can be listed in the key vault.
     */
    public void listSecrets() {
        listSecretsRunner((secrets) -> {
            for (Secret secret :  secrets.values()) {
                assertSecretEquals(secret, client.setSecret(secret));
            }

            for (SecretBase actualSecret : client.listSecrets()) {
                if (secrets.containsKey(actualSecret.getName())) {
                    Secret expectedSecret = secrets.get(actualSecret.getName());
                    assertEquals(expectedSecret.getExpires(), actualSecret.getExpires());
                    assertEquals(expectedSecret.getNotBefore(), actualSecret.getNotBefore());
                    secrets.remove(actualSecret.getName());
                }
            }
            assertEquals(0, secrets.size());
        });
    }

    /**
     * Tests that deleted secrets can be listed in the key vault.
     */
    @Override
    public void listDeletedSecrets() {
        listDeletedSecretsRunner((secrets) -> {

            for (Secret secret : secrets.values()) {
                assertSecretEquals(secret, client.setSecret(secret));
            }

            for (Secret secret : secrets.values()) {
                client.deleteSecret(secret.getName());
                pollOnSecretDeletion(secret.getName());
            }

            sleepInRecordMode(60000);
            Iterable<DeletedSecret> deletedSecrets =  client.listDeletedSecrets();
            for (DeletedSecret actualSecret : deletedSecrets) {
                if (secrets.containsKey(actualSecret.getName())) {
                    assertNotNull(actualSecret.getDeletedDate());
                    assertNotNull(actualSecret.getRecoveryId());
                    secrets.remove(actualSecret.getName());
                }
            }

            assertEquals(0, secrets.size());

            for (DeletedSecret deletedSecret : deletedSecrets) {
                client.purgeDeletedSecret(deletedSecret.getName());
                pollOnSecretPurge(deletedSecret.getName());
            }
            sleepInRecordMode(10000);
        });
    }

    /**
     * Tests that secret versions can be listed in the key vault.
     */
    @Override
    public void listSecretVersions() {
        listSecretVersionsRunner((secrets) -> {
            List<Secret> secretVersions = secrets;
            String secretName = null;
            for (Secret secret : secretVersions) {
                secretName = secret.getName();
                assertSecretEquals(secret, client.setSecret(secret));
            }

            Iterable<SecretBase> secretVersionsOutput =  client.listSecretVersions(secretName);
            List<SecretBase> secretVersionsList = new ArrayList<>();
            secretVersionsOutput.forEach(secretVersionsList::add);
            assertEquals(secretVersions.size(), secretVersionsList.size());

            client.deleteSecret(secretName);
            pollOnSecretDeletion(secretName);

            client.purgeDeletedSecret(secretName);
            pollOnSecretPurge(secretName);
        });

    }

    private void pollOnSecretDeletion(String secretName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 30) {
            DeletedSecret deletedSecret = null;
            try {
                deletedSecret = client.getDeletedSecret(secretName);
            } catch (ResourceNotFoundException e) {
            }
            if (deletedSecret == null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
            } else {
                return;
            }
        }
        System.err.printf("Deleted Secret %s not found \n", secretName);
    }

    private void pollOnSecretPurge(String secretName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 10) {
            DeletedSecret deletedSecret = null;
            try {
                deletedSecret = client.getDeletedSecret(secretName);
            } catch (ResourceNotFoundException e) {
            }
            if (deletedSecret != null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
            } else {
                return;
            }
        }
        System.err.printf("Deleted Secret %s was not purged \n", secretName);
    }
}
