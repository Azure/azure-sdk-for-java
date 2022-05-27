// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class SecretAsyncClientTest extends SecretClientTestBase {
    private SecretAsyncClient secretAsyncClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void createSecretAsyncClient(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion, null);
    }

    private void createSecretAsyncClient(HttpClient httpClient, SecretServiceVersion serviceVersion,
                                         String testTenantId) {
        secretAsyncClient = spy(new SecretClientBuilder()
            .pipeline(getHttpPipeline(httpClient, testTenantId))
            .vaultUrl(getEndpoint())
            .serviceVersion(serviceVersion)
            .buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(secretAsyncClient.getDefaultPollingInterval()).thenReturn(Duration.ofMillis(10));
        }
    }

    /**
     * Tests that a secret can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        setSecretRunner((secretToSet) ->
            StepVerifier.create(secretAsyncClient.setSecret(secretToSet))
                .assertNext(response -> assertSecretEquals(secretToSet, response))
                .verifyComplete());
    }

    /**
     * Tests that a secret can be created in the key vault while using a different tenant ID than the one that will be
     * provided in the authentication challenge.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretWithMultipleTenants(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion, testResourceNamer.randomUuid());

        setSecretRunner((secretToSet) ->
            StepVerifier.create(secretAsyncClient.setSecret(secretToSet))
                .assertNext(response -> assertSecretEquals(secretToSet, response))
                .verifyComplete());

        KeyVaultCredentialPolicy.clearCache(); // Ensure we don't have anything cached and try again.

        setSecretRunner((secretToSet) ->
            StepVerifier.create(secretAsyncClient.setSecret(secretToSet))
                .assertNext(response -> assertSecretEquals(secretToSet, response))
                .verifyComplete());
    }

    /**
     * Tests that we cannot create a secret when the key is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyName(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(secretAsyncClient.setSecret("", "A value"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyValue(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        setSecretEmptyValueRunner((secretToSet) -> {
            StepVerifier.create(secretAsyncClient.setSecret(secretToSet.getName(), secretToSet.getValue()))
                .assertNext(response -> assertSecretEquals(secretToSet, response))
                .verifyComplete();

            StepVerifier.create(secretAsyncClient.getSecret(secretToSet.getName()))
                .assertNext(response -> assertSecretEquals(secretToSet, response))
                .verifyComplete();
        });
    }

    /**
     * Verifies that an exception is thrown when null secret object is passed for creation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretNull(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(secretAsyncClient.setSecret(null))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests that a secret is able to be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        updateSecretRunner((originalSecret, updatedSecret) -> {
            StepVerifier.create(secretAsyncClient.setSecret(originalSecret))
                .assertNext(response -> assertSecretEquals(originalSecret, response))
                .verifyComplete();

            KeyVaultSecret secretToUpdate = secretAsyncClient.getSecret(originalSecret.getName()).block();

            StepVerifier.create(secretAsyncClient.updateSecretProperties(
                    secretToUpdate.getProperties().setExpiresOn(updatedSecret.getProperties().getExpiresOn())))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(originalSecret.getName(), response.getName());
                }).verifyComplete();

            StepVerifier.create(secretAsyncClient.getSecret(originalSecret.getName()))
                .assertNext(response -> assertSecretEquals(updatedSecret, response))
                .verifyComplete();
        });
    }

    /**
     * Tests that a secret is not able to be updated when it is disabled. 403 error is expected.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        updateDisabledSecretRunner((originalSecret, updatedSecret) -> {
            StepVerifier.create(secretAsyncClient.setSecret(originalSecret))
                .assertNext(response -> assertSecretEquals(originalSecret, response))
                .verifyComplete();

            StepVerifier.create(secretAsyncClient.getSecret(originalSecret.getName()))
                .verifyErrorSatisfies(e ->
                    assertRestException(e, ResourceModifiedException.class, HttpURLConnection.HTTP_FORBIDDEN));
        });
    }

    /**
     * Tests that an existing secret can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        getSecretRunner((secretToGet) -> {
            StepVerifier.create(secretAsyncClient.setSecret(secretToGet))
                .assertNext(response -> assertSecretEquals(secretToGet, response))
                .verifyComplete();

            StepVerifier.create(secretAsyncClient.getSecret(secretToGet.getName()))
                .assertNext(response -> assertSecretEquals(secretToGet, response))
                .verifyComplete();
        });
    }

    /**
     * Tests that a specific version of the secret can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecretSpecificVersion(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        getSecretSpecificVersionRunner((secretWithOriginalValue, secretWithNewValue) -> {
            final KeyVaultSecret secretVersionOne = secretAsyncClient.setSecret(secretWithOriginalValue).block();
            final KeyVaultSecret secretVersionTwo = secretAsyncClient.setSecret(secretWithNewValue).block();

            StepVerifier.create(secretAsyncClient.getSecret(secretWithOriginalValue.getName(),
                    secretVersionOne.getProperties().getVersion()))
                .assertNext(response -> assertSecretEquals(secretWithOriginalValue, response))
                .verifyComplete();

            StepVerifier.create(secretAsyncClient.getSecret(secretWithNewValue.getName(),
                    secretVersionTwo.getProperties().getVersion()))
                .assertNext(response -> assertSecretEquals(secretWithNewValue, response))
                .verifyComplete();
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(secretAsyncClient.getSecret("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that an existing secret can be deleted.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        deleteSecretRunner((secretToDelete) -> {
            StepVerifier.create(secretAsyncClient.setSecret(secretToDelete))
                .assertNext(response -> assertSecretEquals(secretToDelete, response))
                .verifyComplete();

            PollerFlux<DeletedSecret, Void> poller = secretAsyncClient.beginDeleteSecret(secretToDelete.getName());
            AsyncPollResponse<DeletedSecret, Void> lastResponse =
                poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();
            DeletedSecret deletedSecretResponse = lastResponse.getValue();

            assertNotNull(deletedSecretResponse.getDeletedOn());
            assertNotNull(deletedSecretResponse.getRecoveryId());
            assertNotNull(deletedSecretResponse.getScheduledPurgeDate());
            assertEquals(secretToDelete.getName(), deletedSecretResponse.getName());
        });
    }

    /**
     * Tests that an attempt to delete a non-existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(secretAsyncClient.beginDeleteSecret("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        getDeletedSecretRunner((secretToDeleteAndGet) -> {
            StepVerifier.create(secretAsyncClient.setSecret(secretToDeleteAndGet))
                .assertNext(secretResponse -> assertSecretEquals(secretToDeleteAndGet, secretResponse))
                .verifyComplete();

            PollerFlux<DeletedSecret, Void> poller =
                secretAsyncClient.beginDeleteSecret(secretToDeleteAndGet.getName());

            poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                .blockLast();

            StepVerifier.create(secretAsyncClient.getDeletedSecret(secretToDeleteAndGet.getName()))
                .assertNext(deletedSecretResponse -> {
                    assertNotNull(deletedSecretResponse.getDeletedOn());
                    assertNotNull(deletedSecretResponse.getRecoveryId());
                    assertNotNull(deletedSecretResponse.getScheduledPurgeDate());
                    assertEquals(secretToDeleteAndGet.getName(), deletedSecretResponse.getName());
                }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(secretAsyncClient.getDeletedSecret("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        recoverDeletedSecretRunner((secretToDeleteAndRecover) -> {
            StepVerifier.create(secretAsyncClient.setSecret(secretToDeleteAndRecover))
                .assertNext(secretResponse -> assertSecretEquals(secretToDeleteAndRecover, secretResponse))
                .verifyComplete();

            PollerFlux<DeletedSecret, Void> poller =
                secretAsyncClient.beginDeleteSecret(secretToDeleteAndRecover.getName());

            poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED).blockLast();

            PollerFlux<KeyVaultSecret, Void> recoverPoller =
                secretAsyncClient.beginRecoverDeletedSecret(secretToDeleteAndRecover.getName());
            AsyncPollResponse<KeyVaultSecret, Void> lastResponse =
                recoverPoller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            KeyVaultSecret secretResponse = lastResponse.getValue();

            assertEquals(secretToDeleteAndRecover.getName(), secretResponse.getName());
            assertEquals(secretToDeleteAndRecover.getProperties().getNotBefore(),
                secretResponse.getProperties().getNotBefore());
            assertEquals(secretToDeleteAndRecover.getProperties().getExpiresOn(),
                secretResponse.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(secretAsyncClient.beginRecoverDeletedSecret("non-existing"))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        backupSecretRunner((secretToBackup) -> {
            StepVerifier.create(secretAsyncClient.setSecret(secretToBackup))
                .assertNext(secretResponse -> assertSecretEquals(secretToBackup, secretResponse))
                .verifyComplete();

            StepVerifier.create(secretAsyncClient.backupSecret(secretToBackup.getName()))
                .assertNext(backupBytes -> {
                    assertNotNull(backupBytes);
                    assertTrue(backupBytes.length > 0);
                }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to backup a non existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        StepVerifier.create(secretAsyncClient.backupSecret("non-existing"))
            .verifyErrorSatisfies(ex ->
                assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        restoreSecretRunner((secretToBackupAndRestore) -> {
            StepVerifier.create(secretAsyncClient.setSecret(secretToBackupAndRestore))
                .assertNext(secretResponse -> assertSecretEquals(secretToBackupAndRestore, secretResponse))
                .verifyComplete();

            byte[] backup = secretAsyncClient.backupSecret(secretToBackupAndRestore.getName()).block();

            PollerFlux<DeletedSecret, Void> poller =
                secretAsyncClient.beginDeleteSecret(secretToBackupAndRestore.getName());

            poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED).blockLast();

            StepVerifier.create(secretAsyncClient.purgeDeletedSecretWithResponse(secretToBackupAndRestore.getName()))
                .assertNext(voidResponse ->
                    assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode()))
                .verifyComplete();

            pollOnSecretPurge(secretToBackupAndRestore.getName());

            sleepInRecordMode(60000);

            StepVerifier.create(secretAsyncClient.restoreSecretBackup(backup))
                .assertNext(response -> {
                    assertEquals(secretToBackupAndRestore.getName(), response.getName());
                    assertEquals(secretToBackupAndRestore.getProperties().getNotBefore(),
                        response.getProperties().getNotBefore());
                    assertEquals(secretToBackupAndRestore.getProperties().getExpiresOn(),
                        response.getProperties().getExpiresOn());
                }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to restore a secret from malformed backup bytes throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreSecretFromMalformedBackup(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        byte[] secretBackupBytes = "non-existing".getBytes();

        StepVerifier.create(secretAsyncClient.restoreSecretBackup(secretBackupBytes))
            .verifyErrorSatisfies(e ->
                assertRestException(e, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Tests that deleted secrets can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        // Skip when running against the service to avoid having pipeline runs take longer than they have to.
        if (interceptorManager.isLiveMode()) {
            return;
        }

        listDeletedSecretsRunner((secretsToSetAndDelete) -> {
            List<DeletedSecret> deletedSecrets = new ArrayList<>();

            for (KeyVaultSecret secret : secretsToSetAndDelete.values()) {
                StepVerifier.create(secretAsyncClient.setSecret(secret))
                    .assertNext(secretResponse -> assertSecretEquals(secret, secretResponse)).verifyComplete();
            }

            sleepInRecordMode(10000);

            for (KeyVaultSecret secret : secretsToSetAndDelete.values()) {
                PollerFlux<DeletedSecret, Void> poller = secretAsyncClient.beginDeleteSecret(secret.getName());

                poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();
            }

            sleepInRecordMode(120000);

            DeletedSecret delSecret = secretAsyncClient.listDeletedSecrets().map(deletedSecret -> {
                deletedSecrets.add(deletedSecret);

                assertNotNull(deletedSecret.getDeletedOn());
                assertNotNull(deletedSecret.getRecoveryId());

                return deletedSecret;
            }).blockLast();

            assertNotNull(delSecret);
        });
    }

    /**
     * Tests that secret versions can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listSecretVersions(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        listSecretVersionsRunner((secretsToSetAndList) -> {
            List<SecretProperties> output = new ArrayList<>();
            String secretName = null;

            for (KeyVaultSecret secret : secretsToSetAndList) {
                secretName = secret.getName();

                secretAsyncClient.setSecret(secret)
                    .subscribe(secretResponse -> assertSecretEquals(secret, secretResponse));

                sleepInRecordMode(1000);
            }

            sleepInRecordMode(30000);

            secretAsyncClient.listPropertiesOfSecretVersions(secretName).subscribe(output::add);

            sleepInRecordMode(30000);

            assertEquals(secretsToSetAndList.size(), output.size());
        });
    }

    /**
     * Tests that secrets can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        listSecretsRunner((secretsToSetAndList) -> {
            HashMap<String, KeyVaultSecret> secretsToList = secretsToSetAndList;
            List<SecretProperties> output = new ArrayList<>();

            for (KeyVaultSecret secret : secretsToList.values()) {
                assertSecretEquals(secret, secretAsyncClient.setSecret(secret).block());
            }

            sleepInRecordMode(10000);

            secretAsyncClient.listPropertiesOfSecrets().map(secret -> {
                if (secretsToList.containsKey(secret.getName())) {
                    output.add(secret);

                    KeyVaultSecret expectedSecret = secretsToSetAndList.get(secret.getName());

                    assertEquals(expectedSecret.getProperties().getExpiresOn(), secret.getExpiresOn());
                    assertEquals(expectedSecret.getProperties().getNotBefore(), secret.getNotBefore());

                    secretsToSetAndList.remove(secret.getName());
                }

                return secret;
            }).blockLast();

            assertEquals(0, secretsToSetAndList.size());
        });
    }

    private void pollOnSecretPurge(String secretName) {
        int pendingPollCount = 0;

        while (pendingPollCount < 10) {
            DeletedSecret deletedSecret = null;

            try {
                deletedSecret = secretAsyncClient.getDeletedSecret(secretName).block();
            } catch (ResourceNotFoundException ignored) {
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
