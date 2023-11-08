// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.secrets.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecretClientTest extends SecretClientTestBase {
    private SecretClient secretClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void createClient(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion, null);
    }

    private void createClient(HttpClient httpClient, SecretServiceVersion serviceVersion, String testTenantId) {
        secretClient = getClientBuilder(buildSyncAssertingClient(interceptorManager.isPlaybackMode()
            ? interceptorManager.getPlaybackClient() : httpClient), testTenantId, getEndpoint(), serviceVersion)
            .buildClient();
    }

    private HttpClient buildSyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertSync()
            .build();
    }

    /**
     * Tests that a secret can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        setSecretRunner((secretToSet) -> assertSecretEquals(secretToSet, secretClient.setSecret(secretToSet)));
    }

    /**
     * Tests that a secret can be created in the key vault while using a different tenant ID than the one that will be
     * provided in the authentication challenge.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretWithMultipleTenants(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion, testResourceNamer.randomUuid());

        setSecretRunner((secretToSet) -> assertSecretEquals(secretToSet, secretClient.setSecret(secretToSet)));

        KeyVaultCredentialPolicy.clearCache(); // Ensure we don't have anything cached and try again.

        setSecretRunner((secretToSet) -> assertSecretEquals(secretToSet, secretClient.setSecret(secretToSet)));
    }

    /**
     * Tests that we cannot create a secret when the secret is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyName(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.setSecret("", "A value"), KeyVaultErrorException.class,
            HttpURLConnection.HTTP_BAD_METHOD);
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyValue(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        setSecretEmptyValueRunner((secretToSet) -> {
            assertSecretEquals(secretToSet, secretClient.setSecret(secretToSet.getName(), secretToSet.getValue()));
            assertSecretEquals(secretToSet, secretClient.getSecret(secretToSet.getName()));
        });
    }

    /**
     * Verifies that an exception is thrown when null secret object is passed for creation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretNull(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);
        assertThrows(NullPointerException.class, () -> secretClient.setSecret(null));
    }

    /**
     * Tests that a secret can be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        updateSecretRunner((originalSecret, updatedSecret) -> {
            assertSecretEquals(originalSecret, secretClient.setSecret(originalSecret));

            KeyVaultSecret secretToUpdate = secretClient.getSecret(originalSecret.getName());

            secretClient.updateSecretProperties(
                secretToUpdate.getProperties().setExpiresOn(updatedSecret.getProperties().getExpiresOn()));

            assertSecretEquals(updatedSecret, secretClient.getSecret(originalSecret.getName()));
        });
    }

    /**
     * Tests that a secret cannot be updated when it is disabled. 403 error is expected.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        updateDisabledSecretRunner((originalSecret, updatedSecret) -> {
            assertSecretEquals(originalSecret, secretClient.setSecret(originalSecret));
            assertRestException(() -> secretClient.getSecret(originalSecret.getName()),
                ResourceModifiedException.class, HttpURLConnection.HTTP_FORBIDDEN);
        });
    }

    /**
     * Tests that an existing secret can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        getSecretRunner((secretToGet) -> {
            secretClient.setSecret(secretToGet);

            assertSecretEquals(secretToGet, secretClient.getSecret(secretToGet.getName()));
        });
    }

    /**
     * Tests that a specific version of the secret can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecretSpecificVersion(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        getSecretSpecificVersionRunner((secretWithOriginalValue, secretWithNewValue) -> {
            KeyVaultSecret secretVersionOne = secretClient.setSecret(secretWithOriginalValue);
            KeyVaultSecret secretVersionTwo = secretClient.setSecret(secretWithNewValue);

            assertSecretEquals(secretWithOriginalValue,
                secretClient.getSecret(secretVersionOne.getName(), secretVersionOne.getProperties().getVersion()));
            assertSecretEquals(secretWithNewValue,
                secretClient.getSecret(secretVersionTwo.getName(), secretVersionTwo.getProperties().getVersion()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.getSecret("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an existing secret can be deleted.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        deleteSecretRunner((secretToDelete) -> {
            assertSecretEquals(secretToDelete, secretClient.setSecret(secretToDelete));

            SyncPoller<DeletedSecret, Void> poller = setPlaybackSyncPollerPollInterval(
                secretClient.beginDeleteSecret(secretToDelete.getName()));

            DeletedSecret deletedSecret = poller.waitForCompletion().getValue();

            assertNotNull(deletedSecret.getDeletedOn());
            assertNotNull(deletedSecret.getRecoveryId());
            assertNotNull(deletedSecret.getScheduledPurgeDate());
            assertEquals(secretToDelete.getName(), deletedSecret.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.beginDeleteSecret("non-existing"),
            ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        getDeletedSecretRunner((secretToDeleteAndGet) -> {
            assertSecretEquals(secretToDeleteAndGet, secretClient.setSecret(secretToDeleteAndGet));

            SyncPoller<DeletedSecret, Void> poller = setPlaybackSyncPollerPollInterval(
                secretClient.beginDeleteSecret(secretToDeleteAndGet.getName()));

            poller.waitForCompletion();

            DeletedSecret deletedSecret = secretClient.getDeletedSecret(secretToDeleteAndGet.getName());

            assertNotNull(deletedSecret.getDeletedOn());
            assertNotNull(deletedSecret.getRecoveryId());
            assertNotNull(deletedSecret.getScheduledPurgeDate());
            assertEquals(secretToDeleteAndGet.getName(), deletedSecret.getName());
        });
    }

    /**
     * Tests that an attempt to retrieve a non-existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.getDeletedSecret("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }


    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        recoverDeletedSecretRunner((secretToDeleteAndRecover) -> {
            assertSecretEquals(secretToDeleteAndRecover, secretClient.setSecret(secretToDeleteAndRecover));

            SyncPoller<DeletedSecret, Void> delPoller = setPlaybackSyncPollerPollInterval(
                secretClient.beginDeleteSecret(secretToDeleteAndRecover.getName()));

            delPoller.waitForCompletion();

            SyncPoller<KeyVaultSecret, Void> poller = setPlaybackSyncPollerPollInterval(
                secretClient.beginRecoverDeletedSecret(secretToDeleteAndRecover.getName()));

            KeyVaultSecret recoveredSecret = poller.waitForCompletion().getValue();

            assertEquals(secretToDeleteAndRecover.getName(), recoveredSecret.getName());
            assertEquals(secretToDeleteAndRecover.getProperties().getNotBefore(),
                recoveredSecret.getProperties().getNotBefore());
            assertEquals(secretToDeleteAndRecover.getProperties().getExpiresOn(),
                recoveredSecret.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to recover a non-existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.beginRecoverDeletedSecret("non-existing"),
            ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code());
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        backupSecretRunner((secretToBackup) -> {
            assertSecretEquals(secretToBackup, secretClient.setSecret(secretToBackup));

            byte[] backupBytes = (secretClient.backupSecret(secretToBackup.getName()));

            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    /**
     * Tests that an attempt to back up a non-existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.backupSecret("non-existing"),
            ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public synchronized void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        restoreSecretRunner((secretToBackupAndRestore) -> {
            assertSecretEquals(secretToBackupAndRestore, secretClient.setSecret(secretToBackupAndRestore));

            byte[] backupBytes = (secretClient.backupSecret(secretToBackupAndRestore.getName()));

            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);

            SyncPoller<DeletedSecret, Void> poller = setPlaybackSyncPollerPollInterval(
                secretClient.beginDeleteSecret(secretToBackupAndRestore.getName()));

            poller.waitForCompletion();

            secretClient.purgeDeletedSecret(secretToBackupAndRestore.getName());
            pollOnSecretPurge(secretToBackupAndRestore.getName());

            sleepIfRunningAgainstService(60000);

            KeyVaultSecret restoredSecret = secretClient.restoreSecretBackup(backupBytes);

            assertEquals(secretToBackupAndRestore.getName(), restoredSecret.getName());
            assertEquals(secretToBackupAndRestore.getProperties().getExpiresOn(),
                restoredSecret.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to restore a secret from malformed backup bytes throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreSecretFromMalformedBackup(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        byte[] secretBackupBytes = "non-existing".getBytes();

        assertRestException(() -> secretClient.restoreSecretBackup(secretBackupBytes),
            ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that secrets can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        listSecretsRunner((secretsToSetAndList) -> {
            for (KeyVaultSecret secret : secretsToSetAndList.values()) {
                assertSecretEquals(secret, secretClient.setSecret(secret));
            }

            for (SecretProperties actualSecret : secretClient.listPropertiesOfSecrets()) {
                if (secretsToSetAndList.containsKey(actualSecret.getName())) {
                    KeyVaultSecret expectedSecret = secretsToSetAndList.get(actualSecret.getName());

                    assertEquals(expectedSecret.getProperties().getExpiresOn(), actualSecret.getExpiresOn());
                    assertEquals(expectedSecret.getProperties().getNotBefore(), actualSecret.getNotBefore());
                    secretsToSetAndList.remove(actualSecret.getName());
                }
            }

            assertEquals(0, secretsToSetAndList.size());
        });
    }

    /**
     * Tests that deleted secrets can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        // Skip when running against the service to avoid having pipeline runs take longer than they have to.
        if (interceptorManager.isLiveMode()) {
            return;
        }

        listDeletedSecretsRunner((secretsToSetAndDelete) -> {
            for (KeyVaultSecret secret : secretsToSetAndDelete.values()) {
                assertSecretEquals(secret, secretClient.setSecret(secret));
            }

            for (KeyVaultSecret secret : secretsToSetAndDelete.values()) {
                SyncPoller<DeletedSecret, Void> poller = setPlaybackSyncPollerPollInterval(
                    secretClient.beginDeleteSecret(secret.getName()));

                poller.waitForCompletion();
            }

            sleepIfRunningAgainstService(60000);

            Iterable<DeletedSecret> deletedSecrets = secretClient.listDeletedSecrets();

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
        createClient(httpClient, serviceVersion);

        listSecretVersionsRunner((secretsToSetAndList) -> {
            String secretName = null;

            for (KeyVaultSecret secret : secretsToSetAndList) {
                secretName = secret.getName();

                assertSecretEquals(secret, secretClient.setSecret(secret));
            }

            Iterable<SecretProperties> secretVersionsOutput = secretClient.listPropertiesOfSecretVersions(secretName);
            List<SecretProperties> secretVersionsList = new ArrayList<>();

            secretVersionsOutput.forEach(secretVersionsList::add);

            assertEquals(secretsToSetAndList.size(), secretVersionsList.size());
        });

    }

    private void pollOnSecretPurge(String secretName) {
        int pendingPollCount = 0;

        while (pendingPollCount < 10) {
            DeletedSecret deletedSecret = null;

            try {
                deletedSecret = secretClient.getDeletedSecret(secretName);
            } catch (ResourceNotFoundException ignored) {
            }

            if (deletedSecret != null) {
                sleepIfRunningAgainstService(2000);
                pendingPollCount += 1;
            } else {
                return;
            }
        }

        System.err.printf("Deleted Secret %s was not purged \n", secretName);
    }
}
