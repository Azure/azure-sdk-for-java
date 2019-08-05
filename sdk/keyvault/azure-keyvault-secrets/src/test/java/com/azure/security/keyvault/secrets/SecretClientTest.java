// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;
import io.netty.handler.codec.http.HttpResponseStatus;

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
            client = clientSetup(credentials -> new SecretClientBuilder()
                .credential(credentials)
                .endpoint(getEndpoint())
                .httpClient(interceptorManager.getPlaybackClient())
                .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .buildClient());
        } else {
            client = clientSetup(credentials -> new SecretClientBuilder()
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
     * Tests that a secret can be created in the key vault.
     */
    public void setSecret() {
        setSecretRunner((expected) -> assertSecretEquals(expected, client.setSecret(expected)));
    }

    /**
     * Tests that we cannot create a secret when the secret is an empty string.
     */
    public void setSecretEmptyName() {
        assertRestException(() -> client.setSecret("", "A value"), HttpResponseStatus.METHOD_NOT_ALLOWED.code());
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    public void setSecretEmptyValue() {
        setSecretEmptyValueRunner((secret) -> {
            assertSecretEquals(secret, client.setSecret(secret.name(), secret.value()));
            assertSecretEquals(secret, client.getSecret(secret.name()));
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
            Secret secretToUpdate = client.getSecret(original.name());
            client.updateSecret(secretToUpdate.expires(updated.expires()));
            assertSecretEquals(updated, client.getSecret(original.name()));
        });
    }

    /**
     * Tests that a secret is not able to be updated when it is disabled. 403 error is expected.
     */
    public void updateDisabledSecret() {
        updateDisabledSecretRunner((original, updated) -> {
            assertSecretEquals(original, client.setSecret(original));
            assertRestException(() -> client.getSecret(original.name()), ResourceModifiedException.class, HttpResponseStatus.FORBIDDEN.code());
        });
    }

    /**
     * Tests that an existing secret can be retrieved.
     */
    public void getSecret() {
        getSecretRunner((original) -> {
            client.setSecret(original);
            assertSecretEquals(original, client.getSecret(original.name()));
        });
    }

    /**
     * Tests that a specific version of the secret can be retrieved.
     */
    public void getSecretSpecificVersion() {
        getSecretSpecificVersionRunner((secret, secretWithNewVal) -> {
            Secret secretVersionOne = client.setSecret(secret);
            Secret secretVersionTwo = client.setSecret(secretWithNewVal);
            assertSecretEquals(secret, client.getSecret(secretVersionOne.name(), secretVersionOne.version()));
            assertSecretEquals(secretWithNewVal, client.getSecret(secretVersionTwo.name(), secretVersionTwo.version()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    public void getSecretNotFound() {
        assertRestException(() -> client.getSecret("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that an existing secret can be deleted.
     */
    public void deleteSecret() {
        deleteSecretRunner((secretToDelete) -> {
            assertSecretEquals(secretToDelete,  client.setSecret(secretToDelete));
            DeletedSecret deletedSecret = client.deleteSecret(secretToDelete.name());
            pollOnSecretDeletion(secretToDelete.name());
            assertNotNull(deletedSecret.deletedDate());
            assertNotNull(deletedSecret.recoveryId());
            assertNotNull(deletedSecret.scheduledPurgeDate());
            assertEquals(secretToDelete.name(), deletedSecret.name());
            client.purgeDeletedSecret(secretToDelete.name());
            pollOnSecretPurge(secretToDelete.name());
        });
    }

    public void deleteSecretNotFound() {
        assertRestException(() -> client.deleteSecret("non-existing"), ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    public void getDeletedSecret() {
        getDeletedSecretRunner((secretToDeleteAndGet) -> {
            assertSecretEquals(secretToDeleteAndGet, client.setSecret(secretToDeleteAndGet));
            assertNotNull(client.deleteSecret(secretToDeleteAndGet.name()));
            pollOnSecretDeletion(secretToDeleteAndGet.name());
            sleepInRecordMode(30000);
            DeletedSecret deletedSecret = client.getDeletedSecret(secretToDeleteAndGet.name());
            assertNotNull(deletedSecret.deletedDate());
            assertNotNull(deletedSecret.recoveryId());
            assertNotNull(deletedSecret.scheduledPurgeDate());
            assertEquals(secretToDeleteAndGet.name(), deletedSecret.name());
            client.purgeDeletedSecret(secretToDeleteAndGet.name());
            pollOnSecretPurge(secretToDeleteAndGet.name());
            sleepInRecordMode(10000);
        });
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void getDeletedSecretNotFound() {
        assertRestException(() -> client.getDeletedSecret("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }


    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    public void recoverDeletedSecret() {
        recoverDeletedSecretRunner((secretToDeleteAndRecover) -> {
            assertSecretEquals(secretToDeleteAndRecover, client.setSecret(secretToDeleteAndRecover));
            assertNotNull(client.deleteSecret(secretToDeleteAndRecover.name()));
            pollOnSecretDeletion(secretToDeleteAndRecover.name());
            Secret recoveredSecret = client.recoverDeletedSecret(secretToDeleteAndRecover.name());
            assertEquals(secretToDeleteAndRecover.name(), recoveredSecret.name());
            assertEquals(secretToDeleteAndRecover.notBefore(), recoveredSecret.notBefore());
            assertEquals(secretToDeleteAndRecover.expires(), recoveredSecret.expires());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void recoverDeletedSecretNotFound() {
        assertRestException(() -> client.recoverDeletedSecret("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    public void backupSecret() {
        backupSecretRunner((secretToBackup) -> {
            assertSecretEquals(secretToBackup, client.setSecret(secretToBackup));
            byte[] backupBytes = (client.backupSecret(secretToBackup.name()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    /**
     * Tests that an attempt to backup a non existing secret throws an error.
     */
    public void backupSecretNotFound() {
        assertRestException(() -> client.backupSecret("non-existing"),  ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    public synchronized void restoreSecret() {
        restoreSecretRunner((secretToBackupAndRestore) -> {
            assertSecretEquals(secretToBackupAndRestore, client.setSecret(secretToBackupAndRestore));
            byte[] backupBytes = (client.backupSecret(secretToBackupAndRestore.name()));
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
            client.deleteSecret(secretToBackupAndRestore.name());
            pollOnSecretDeletion(secretToBackupAndRestore.name());
            client.purgeDeletedSecret(secretToBackupAndRestore.name());
            pollOnSecretPurge(secretToBackupAndRestore.name());
            sleepInRecordMode(60000);
            Secret restoredSecret = client.restoreSecret(backupBytes);
            assertEquals(secretToBackupAndRestore.name(), restoredSecret.name());
            assertEquals(secretToBackupAndRestore.expires(), restoredSecret.expires());
        });
    }

    /**
     * Tests that an attempt to restore a secret from malformed backup bytes throws an error.
     */
    public void restoreSecretFromMalformedBackup() {
        byte[] secretBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreSecret(secretBackupBytes), ResourceModifiedException.class, HttpResponseStatus.BAD_REQUEST.code());
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
                if (secrets.containsKey(actualSecret.name())) {
                    Secret expectedSecret = secrets.get(actualSecret.name());
                    assertEquals(expectedSecret.expires(), actualSecret.expires());
                    assertEquals(expectedSecret.notBefore(), actualSecret.notBefore());
                    secrets.remove(actualSecret.name());
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
                client.deleteSecret(secret.name());
                pollOnSecretDeletion(secret.name());
            }

            sleepInRecordMode(60000);
            Iterable<DeletedSecret> deletedSecrets =  client.listDeletedSecrets();
            for (DeletedSecret actualSecret : deletedSecrets) {
                if (secrets.containsKey(actualSecret.name())) {
                    assertNotNull(actualSecret.deletedDate());
                    assertNotNull(actualSecret.recoveryId());
                    secrets.remove(actualSecret.name());
                }
            }

            assertEquals(0, secrets.size());

            for (DeletedSecret deletedSecret : deletedSecrets) {
                client.purgeDeletedSecret(deletedSecret.name());
                pollOnSecretPurge(deletedSecret.name());
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
                secretName = secret.name();
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
