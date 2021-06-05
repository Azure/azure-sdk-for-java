// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SecretClientTest extends SecretClientTestBase {

    private SecretClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void initializeClient(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        SecretAsyncClient asyncClient = spy(new SecretClientBuilder()
            .pipeline(getHttpPipeline(httpClient, serviceVersion))
            .vaultUrl(getEndpoint())
            .serviceVersion(serviceVersion)
            .buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(asyncClient.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }

        client = new SecretClient(asyncClient);
    }

    /**
     * Tests that a secret can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        setSecretRunner((expected) -> assertSecretEquals(expected, client.setSecret(expected)));
    }

    /**
     * Tests that we cannot create a secret when the secret is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyName(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        assertRestException(() -> client.setSecret("", "A value"), HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyValue(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        setSecretEmptyValueRunner((secret) -> {
            assertSecretEquals(secret, client.setSecret(secret.getName(), secret.getValue()));
            assertSecretEquals(secret, client.getSecret(secret.getName()));
        });
    }

    /**
     * Verifies that an exception is thrown when null secret object is passed for creation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretNull(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        assertRunnableThrowsException(() -> client.setSecret(null), NullPointerException.class);
    }

    /**
     * Tests that a secret is able to be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        updateDisabledSecretRunner((original, updated) -> {
            assertSecretEquals(original, client.setSecret(original));
            assertRestException(() -> client.getSecret(original.getName()), ResourceModifiedException.class, HttpURLConnection.HTTP_FORBIDDEN);
        });
    }

    /**
     * Tests that an existing secret can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        getSecretRunner((original) -> {
            client.setSecret(original);
            assertSecretEquals(original, client.getSecret(original.getName()));
        });
    }

    /**
     * Tests that a specific version of the secret can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecretSpecificVersion(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        assertRestException(() -> client.getSecret("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an existing secret can be deleted.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        assertRestException(() -> client.beginDeleteSecret("non-existing"), ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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
        });
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        assertRestException(() -> client.getDeletedSecret("non-existing"),  ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }


    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        assertRestException(() -> client.beginRecoverDeletedSecret("non-existing"), ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        assertRestException(() -> client.backupSecret("non-existing"), ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public synchronized void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreSecretFromMalformedBackup(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        byte[] secretBackupBytes = "non-existing".getBytes();
        assertRestException(() -> client.restoreSecretBackup(secretBackupBytes), ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that secrets can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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

            sleepInRecordMode(300000);
            Iterable<DeletedSecret> deletedSecrets = client.listDeletedSecrets();
            assertTrue(deletedSecrets.iterator().hasNext());
            for (DeletedSecret deletedSecret : deletedSecrets) {
                assertNotNull(deletedSecret.getDeletedOn());
                assertNotNull(deletedSecret.getRecoveryId());
            }
        });
    }

    /**
     * Tests that secret versions can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listSecretVersions(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
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
        });

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
