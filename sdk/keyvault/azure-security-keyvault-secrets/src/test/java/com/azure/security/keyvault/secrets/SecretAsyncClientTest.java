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

    private SecretAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void initializeClient(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        client = spy(new SecretClientBuilder()
            .pipeline(getHttpPipeline(httpClient, serviceVersion))
            .vaultUrl(getEndpoint())
            .serviceVersion(serviceVersion)
            .buildAsyncClient());

        if (interceptorManager.isPlaybackMode()) {
            when(client.getPollDuration()).thenReturn(Duration.ofMillis(10));
        }
    }

    /**
     * Tests that a secret can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        setSecretRunner((expected) -> StepVerifier.create(client.setSecret(expected))
                .assertNext(response -> assertSecretEquals(expected, response))
                .verifyComplete());
    }

    /**
     * Tests that we cannot create a secret when the key is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyName(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        StepVerifier.create(client.setSecret("", "A value"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD));
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyValue(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        setSecretEmptyValueRunner((secret) -> {

            StepVerifier.create(client.setSecret(secret.getName(), secret.getValue()))
                .assertNext(response -> assertSecretEquals(secret, response))
                .verifyComplete();

            StepVerifier.create(client.getSecret(secret.getName()))
                    .assertNext(response -> assertSecretEquals(secret, response))
                    .verifyComplete();
        });
    }

    /**
     * Verifies that an exception is thrown when null secret object is passed for creation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretNull(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        StepVerifier.create(client.setSecret(null))
            .verifyError(NullPointerException.class);
    }

    /**
     * Tests that a secret is able to be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        updateSecretRunner((original, updated) -> {
            StepVerifier.create(client.setSecret(original))
                    .assertNext(response -> assertSecretEquals(original, response))
                    .verifyComplete();
            KeyVaultSecret secretToUpdate = client.getSecret(original.getName()).block();

            StepVerifier.create(client.updateSecretProperties(secretToUpdate.getProperties().setExpiresOn(updated.getProperties().getExpiresOn())))
                    .assertNext(response -> {
                        assertNotNull(response);
                        assertEquals(original.getName(), response.getName());
                    }).verifyComplete();

            StepVerifier.create(client.getSecret(original.getName()))
                    .assertNext(updatedSecretResponse -> assertSecretEquals(updated, updatedSecretResponse))
                    .verifyComplete();
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
            StepVerifier.create(client.setSecret(original))
                    .assertNext(response -> assertSecretEquals(original, response))
                    .verifyComplete();

            StepVerifier.create(client.getSecret(original.getName()))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_FORBIDDEN));
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
            StepVerifier.create(client.setSecret(original))
                .assertNext(response -> assertSecretEquals(original, response))
                .verifyComplete();

            StepVerifier.create(client.getSecret(original.getName()))
                    .assertNext(response -> assertSecretEquals(original, response))
                    .verifyComplete();
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
            final KeyVaultSecret secretVersionOne = client.setSecret(secret).block();
            final KeyVaultSecret secretVersionTwo = client.setSecret(secretWithNewVal).block();

            StepVerifier.create(client.getSecret(secret.getName(), secretVersionOne.getProperties().getVersion()))
                    .assertNext(response -> assertSecretEquals(secret, response))
                    .verifyComplete();

            StepVerifier.create(client.getSecret(secretWithNewVal.getName(), secretVersionTwo.getProperties().getVersion()))
                    .assertNext(response -> assertSecretEquals(secretWithNewVal, response))
                    .verifyComplete();
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        StepVerifier.create(client.getSecret("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }


    /**
     * Tests that an existing secret can be deleted.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        deleteSecretRunner((secretToDelete) -> {
            StepVerifier.create(client.setSecret(secretToDelete))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToDelete, secretResponse);
                    }).verifyComplete();

            PollerFlux<DeletedSecret, Void> poller = client.beginDeleteSecret(secretToDelete.getName());
            AsyncPollResponse<DeletedSecret, Void> lastResponse
                    = poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            DeletedSecret deletedSecretResponse = lastResponse.getValue();
            assertNotNull(deletedSecretResponse.getDeletedOn());
            assertNotNull(deletedSecretResponse.getRecoveryId());
            assertNotNull(deletedSecretResponse.getScheduledPurgeDate());
            assertEquals(secretToDelete.getName(), deletedSecretResponse.getName());
        });
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        StepVerifier.create(client.beginDeleteSecret("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        getDeletedSecretRunner((secretToDeleteAndGet) -> {
            StepVerifier.create(client.setSecret(secretToDeleteAndGet))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToDeleteAndGet, secretResponse);
                    }).verifyComplete();

            PollerFlux<DeletedSecret, Void> poller = client.beginDeleteSecret(secretToDeleteAndGet.getName());
            poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            StepVerifier.create(client.getDeletedSecret(secretToDeleteAndGet.getName()))
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
        initializeClient(httpClient, serviceVersion);
        StepVerifier.create(client.getDeletedSecret("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        recoverDeletedSecretRunner((secretToDeleteAndRecover) -> {
            StepVerifier.create(client.setSecret(secretToDeleteAndRecover))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToDeleteAndRecover, secretResponse);
                    }).verifyComplete();

            PollerFlux<DeletedSecret, Void> poller = client.beginDeleteSecret(secretToDeleteAndRecover.getName());
            poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED).blockLast();

            PollerFlux<KeyVaultSecret, Void> recoverPoller
                    = client.beginRecoverDeletedSecret(secretToDeleteAndRecover.getName());
            AsyncPollResponse<KeyVaultSecret, Void> lastResponse = recoverPoller
                    .takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();

            KeyVaultSecret secretResponse = lastResponse.getValue();

            assertEquals(secretToDeleteAndRecover.getName(), secretResponse.getName());
            assertEquals(secretToDeleteAndRecover.getProperties().getNotBefore(), secretResponse.getProperties().getNotBefore());
            assertEquals(secretToDeleteAndRecover.getProperties().getExpiresOn(), secretResponse.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        StepVerifier.create(client.beginRecoverDeletedSecret("non-existing"))
            .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        backupSecretRunner((secretToBackup) -> {
            StepVerifier.create(client.setSecret(secretToBackup))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToBackup, secretResponse);
                    }).verifyComplete();

            StepVerifier.create(client.backupSecret(secretToBackup.getName()))
                    .assertNext(response -> {
                        byte[] backupBytes = response;
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
        initializeClient(httpClient, serviceVersion);
        StepVerifier.create(client.backupSecret("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND));
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        restoreSecretRunner((secretToBackupAndRestore) -> {
            StepVerifier.create(client.setSecret(secretToBackupAndRestore))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToBackupAndRestore, secretResponse);
                    }).verifyComplete();
            byte[] backup = client.backupSecret(secretToBackupAndRestore.getName()).block();

            PollerFlux<DeletedSecret, Void> poller = client.beginDeleteSecret(secretToBackupAndRestore.getName());
            poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                  .blockLast();

            StepVerifier.create(client.purgeDeletedSecretWithResponse(secretToBackupAndRestore.getName()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, voidResponse.getStatusCode());
                    }).verifyComplete();
            pollOnSecretPurge(secretToBackupAndRestore.getName());

            sleepInRecordMode(60000);

            StepVerifier.create(client.restoreSecretBackup(backup))
                    .assertNext(response -> {
                        assertEquals(secretToBackupAndRestore.getName(), response.getName());
                        assertEquals(secretToBackupAndRestore.getProperties().getNotBefore(), response.getProperties().getNotBefore());
                        assertEquals(secretToBackupAndRestore.getProperties().getExpiresOn(), response.getProperties().getExpiresOn());
                    }).verifyComplete();
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
        StepVerifier.create(client.restoreSecretBackup(secretBackupBytes))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpURLConnection.HTTP_BAD_REQUEST));
    }

    /**
     * Tests that deleted secrets can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        if (!interceptorManager.isPlaybackMode()) {
            return;
        }

        listDeletedSecretsRunner((secrets) -> {
            List<DeletedSecret> deletedSecrets = new ArrayList<>();

            for (KeyVaultSecret secret : secrets.values()) {
                StepVerifier.create(client.setSecret(secret))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secret, secretResponse);
                    }).verifyComplete();
            }
            sleepInRecordMode(10000);

            for (KeyVaultSecret secret : secrets.values()) {
                PollerFlux<DeletedSecret, Void> poller = client.beginDeleteSecret(secret.getName());
                poller.takeUntil(apr -> apr.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
                    .blockLast();
            }

            sleepInRecordMode(120000);
            DeletedSecret delSecret = client.listDeletedSecrets().map(deletedSecret -> {
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
        initializeClient(httpClient, serviceVersion);
        listSecretVersionsRunner((secrets) -> {
            List<SecretProperties> output = new ArrayList<>();
            String secretName = null;
            for (KeyVaultSecret secret : secrets) {
                secretName = secret.getName();
                client.setSecret(secret).subscribe(secretResponse -> assertSecretEquals(secret, secretResponse));
                sleepInRecordMode(1000);
            }
            sleepInRecordMode(30000);
            client.listPropertiesOfSecretVersions(secretName).subscribe(output::add);
            sleepInRecordMode(30000);

            assertEquals(secrets.size(), output.size());
        });
    }

    /**
     * Tests that secrets can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        initializeClient(httpClient, serviceVersion);
        listSecretsRunner((secrets) -> {
            HashMap<String, KeyVaultSecret> secretsToList = secrets;
            List<SecretProperties> output = new ArrayList<>();
            for (KeyVaultSecret secret : secretsToList.values()) {
                assertSecretEquals(secret, client.setSecret(secret).block());
            }
            sleepInRecordMode(10000);
            client.listPropertiesOfSecrets().map(secret -> {
                if (secretsToList.containsKey(secret.getName())) {
                    output.add(secret);
                    KeyVaultSecret expectedSecret = secrets.get(secret.getName());
                    assertEquals(expectedSecret.getProperties().getExpiresOn(), secret.getExpiresOn());
                    assertEquals(expectedSecret.getProperties().getNotBefore(), secret.getNotBefore());
                    secrets.remove(secret.getName());
                }
                return secret;
            }).blockLast();
            assertEquals(0, secrets.size());
        });
    }

    private void pollOnSecretPurge(String secretName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 10) {
            DeletedSecret deletedSecret = null;
            try {
                deletedSecret = client.getDeletedSecret(secretName).block();
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
