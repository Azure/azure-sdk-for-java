// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class SecretClientTest extends SecretClientTestBase {

    private SecretClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(pipeline -> new SecretClientBuilder()
                .pipeline(pipeline)
                .vaultUrl(getEndpoint())
                .buildClient());
        } else {
            client = clientSetup(pipeline -> new SecretClientBuilder()
                .pipeline(pipeline)
                .vaultUrl(getEndpoint())
                .buildClient());
        }
    }

    /**
     * Tests that a secret can be created in the key vault.
     */
    @Test
    public void setSecret() {
        setSecretRunner((expected) -> assertSecretEquals(expected, client.setSecret(expected)));
    }

    /**
     * Tests that we cannot create a secret when the secret is an empty string.
     */
    @Test
    public void setSecretEmptyName() {
        assertRestException(() -> client.setSecret("", "A value"), HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    @Test
    public void setSecretEmptyValue() {
        setSecretEmptyValueRunner((secret) -> {
            assertSecretEquals(secret, client.setSecret(secret.getName(), secret.getValue()));
            assertSecretEquals(secret, client.getSecret(secret.getName()));
        });
    }

    /**
     * Verifies that an exception is thrown when null secret object is passed for creation.
     */
    @Test
    public void setSecretNull() {
        assertRunnableThrowsException(() -> client.setSecret(null), NullPointerException.class);
    }

    /**
     * Tests that a secret is able to be updated when it exists.
     */
    @Test
    public void updateSecret() {
        updateSecretRunner((original, updated) -> {
            assertSecretEquals(original, client.setSecret(original));
            KeyVaultSecret secretToUpdate = client.getSecret(original.getName());
            client.updateSecretProperties(secretToUpdate.getProperties().setExpiresOn(updated.getProperties().getExpiresOn()));
            assertSecretEquals(updated, client.getSecret(original.getName()));
        });
    }

    /**
     * Tests that a secret is not able to be updated when it is disabled. 403 error is expected.
     */
    @Test
    public void updateDisabledSecret() {
        updateDisabledSecretRunner((original, updated) -> {
            assertSecretEquals(original, client.setSecret(original));
            assertRestException(() -> client.getSecret(original.getName()), ResourceModifiedException.class, HttpURLConnection.HTTP_FORBIDDEN);
        });
    }

    /**
     * Tests that an existing secret can be retrieved.
     */
    @Test
    public void getSecret() {
        getSecretRunner((original) -> {
            client.setSecret(original);
            assertSecretEquals(original, client.getSecret(original.getName()));
        });
    }

    /**
     * Tests that a specific version of the secret can be retrieved.
     */
    @Test
    public void getSecretSpecificVersion() {
        getSecretSpecificVersionRunner((secret, secretWithNewVal) -> {
            KeyVaultSecret secretVersionOne = client.setSecret(secret);
            KeyVaultSecret secretVersionTwo = client.setSecret(secretWithNewVal);
            assertSecretEquals(secret, client.getSecret(secretVersionOne.getName(), secretVersionOne.getProperties().getVersion()));
            assertSecretEquals(secretWithNewVal, client.getSecret(secretVersionTwo.getName(), secretVersionTwo.getProperties().getVersion()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    @Test
    public void getSecretNotFound() {
        assertRestException(() -> client.getSecret("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an existing secret can be deleted.
     */
    @Test
    public void deleteSecret() {
        deleteSecretRunner((secretToDelete) -> {
            assertSecretEquals(secretToDelete,  client.setSecret(secretToDelete));
            SyncPoller<DeletedSecret, Void> poller = client.beginDeleteSecret(secretToDelete.getName());

            PollResponse<DeletedSecret> pollResponse = poller.poll();
            while (!pollResponse.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                pollResponse = poller.poll();
            }

            DeletedSecret deletedSecret = pollResponse.getValue();
            assertNotNull(deletedSecret.getDeletedOn());
            assertNotNull(deletedSecret.getRecoveryId());
            assertNotNull(deletedSecret.getScheduledPurgeDate());
            assertEquals(secretToDelete.getName(), deletedSecret.getName());
            client.purgeDeletedSecret(secretToDelete.getName());
            pollOnSecretPurge(secretToDelete.getName());
        });
    }

    @Test
    public void deleteSecretNotFound() {
        assertRestException(() -> client.beginDeleteSecret("non-existing"), ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    @Test
    public void getDeletedSecret() {
        getDeletedSecretRunner((secretToDeleteAndGet) -> {
            assertSecretEquals(secretToDeleteAndGet, client.setSecret(secretToDeleteAndGet));
            SyncPoller<DeletedSecret, Void> poller = client.beginDeleteSecret(secretToDeleteAndGet.getName());
            PollResponse<DeletedSecret> pollResponse = poller.poll();
            while (!pollResponse.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                pollResponse = poller.poll();
            }
            DeletedSecret deletedSecret = client.getDeletedSecret(secretToDeleteAndGet.getName());
            assertNotNull(deletedSecret.getDeletedOn());
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
    @Test
    public void getDeletedSecretNotFound() {
        assertRestException(() -> client.getDeletedSecret("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }


    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    @Test
    public void recoverDeletedSecret() {
        recoverDeletedSecretRunner((secretToDeleteAndRecover) -> {
            assertSecretEquals(secretToDeleteAndRecover, client.setSecret(secretToDeleteAndRecover));
            SyncPoller<DeletedSecret, Void> delPoller = client.beginDeleteSecret(secretToDeleteAndRecover.getName());
            PollResponse<DeletedSecret> pollResponse = delPoller.poll();
            while (!pollResponse.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                pollResponse = delPoller.poll();
            }
            SyncPoller<KeyVaultSecret, Void> poller = client.beginRecoverDeletedSecret(secretToDeleteAndRecover.getName());
            PollResponse<KeyVaultSecret> response = poller.poll();
            while (!response.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                response = poller.poll();
            }
            KeyVaultSecret recoveredSecret = response.getValue();
            assertEquals(secretToDeleteAndRecover.getName(), recoveredSecret.getName());
            assertEquals(secretToDeleteAndRecover.getProperties().getNotBefore(), recoveredSecret.getProperties().getNotBefore());
            assertEquals(secretToDeleteAndRecover.getProperties().getExpiresOn(), recoveredSecret.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @Test
    public void recoverDeletedSecretNotFound() {
        assertRestException(() -> client.beginRecoverDeletedSecret("non-existing"), ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @Test
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
    @Test
    public void backupSecretNotFound() {
        assertRestException(() -> client.backupSecret("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @Test
    public synchronized void restoreSecret() {
        restoreSecretRunner((secretToBackupAndRestore) -> {
            assertSecretEquals(secretToBackupAndRestore, client.setSecret(secretToBackupAndRestore));
            byte[] backupBytes = (client.backupSecret(secretToBackupAndRestore.getName()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
            SyncPoller<DeletedSecret, Void> poller = client.beginDeleteSecret(secretToBackupAndRestore.getName());
            PollResponse<DeletedSecret> pollResponse = poller.poll();
            while (!pollResponse.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                pollResponse = poller.poll();
            }
            client.purgeDeletedSecret(secretToBackupAndRestore.getName());
            pollOnSecretPurge(secretToBackupAndRestore.getName());
            sleepInRecordMode(60000);
            KeyVaultSecret restoredSecret = client.restoreSecretBackup(backupBytes);
            assertEquals(secretToBackupAndRestore.getName(), restoredSecret.getName());
            assertEquals(secretToBackupAndRestore.getProperties().getExpiresOn(), restoredSecret.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to restore a secret from malformed backup bytes throws an error.
     */
    @Test
    public void restoreSecretFromMalformedBackup() {
        byte[] secretBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreSecretBackup(secretBackupBytes), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that secrets can be listed in the key vault.
     */
    @Test
    public void listSecrets() {
        listSecretsRunner((secrets) -> {
            for (KeyVaultSecret secret :  secrets.values()) {
                assertSecretEquals(secret, client.setSecret(secret));
            }

            for (SecretProperties actualSecret : client.listPropertiesOfSecrets()) {
                if (secrets.containsKey(actualSecret.getName())) {
                    KeyVaultSecret expectedSecret = secrets.get(actualSecret.getName());
                    assertEquals(expectedSecret.getProperties().getExpiresOn(), actualSecret.getExpiresOn());
                    assertEquals(expectedSecret.getProperties().getNotBefore(), actualSecret.getNotBefore());
                    secrets.remove(actualSecret.getName());
                }
            }
            assertEquals(0, secrets.size());
        });
    }

    /**
     * Tests that deleted secrets can be listed in the key vault.
     */
    @Test
    public void listDeletedSecrets() {
        listDeletedSecretsRunner((secrets) -> {

            for (KeyVaultSecret secret : secrets.values()) {
                assertSecretEquals(secret, client.setSecret(secret));
            }

            for (KeyVaultSecret secret : secrets.values()) {
                SyncPoller<DeletedSecret, Void> poller = client.beginDeleteSecret(secret.getName());
                PollResponse<DeletedSecret> pollResponse = poller.poll();
                while (!pollResponse.getStatus().isComplete()) {
                    sleepInRecordMode(1000);
                    pollResponse = poller.poll();
                }
            }

            sleepInRecordMode(60000);
            Iterable<DeletedSecret> deletedSecrets =  client.listDeletedSecrets();
            for (DeletedSecret actualSecret : deletedSecrets) {
                if (secrets.containsKey(actualSecret.getName())) {
                    assertNotNull(actualSecret.getDeletedOn());
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
    @Test
    public void listSecretVersions() {
        listSecretVersionsRunner((secrets) -> {
            List<KeyVaultSecret> secretVersions = secrets;
            String secretName = null;
            for (KeyVaultSecret secret : secretVersions) {
                secretName = secret.getName();
                assertSecretEquals(secret, client.setSecret(secret));
            }

            Iterable<SecretProperties> secretVersionsOutput =  client.listPropertiesOfSecretVersions(secretName);
            List<SecretProperties> secretVersionsList = new ArrayList<>();
            secretVersionsOutput.forEach(secretVersionsList::add);
            assertEquals(secretVersions.size(), secretVersionsList.size());

            SyncPoller<DeletedSecret, Void> poller = client.beginDeleteSecret(secretName);
            PollResponse<DeletedSecret> pollResponse = poller.poll();
            while (!pollResponse.getStatus().isComplete()) {
                sleepInRecordMode(1000);
                pollResponse = poller.poll();
            }

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
