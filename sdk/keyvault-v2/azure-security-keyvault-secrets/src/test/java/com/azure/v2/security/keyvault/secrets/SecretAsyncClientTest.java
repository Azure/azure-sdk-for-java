// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.secrets.models.DeletedSecret;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.v2.security.keyvault.secrets.models.SecretProperties;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.ResourceModifiedException;
import io.clientcore.core.http.models.ResourceNotFoundException;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.util.AsyncPollResponse;
import io.clientcore.core.util.PollerFlux;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecretAsyncClientTest extends SecretClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(SecretAsyncClientTest.class);

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
        secretAsyncClient = getClientBuilder(
            buildAsyncAssertingClient(
                interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
            testTenantId, getEndpoint(), serviceVersion).buildAsyncClient();
        if (!interceptorManager.isLiveMode()) {
            // Remove `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new TestUtils.AssertingHttpClientBuilder(httpClient).assertAsync().build();
    }

    /**
     * Tests that a secret can be created in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        setSecretRunner((secretToSet) -> {
            CompletableFuture<KeyVaultSecret> future = secretAsyncClient.setSecret(secretToSet);
            KeyVaultSecret response = future.join();
            assertSecretEquals(secretToSet, response);
        });
    }

    /**
     * Tests that a secret can be created in the key vault while using a different tenant ID than the one that will be
     * provided in the authentication challenge.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretWithMultipleTenants(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion, testResourceNamer.randomUuid());

        setSecretRunner((secretToSet) -> {
            CompletableFuture<KeyVaultSecret> future = secretAsyncClient.setSecret(secretToSet);
            KeyVaultSecret response = future.join();
            assertSecretEquals(secretToSet, response);
        });

        KeyVaultCredentialPolicy.clearCache(); // Ensure we don't have anything cached and try again.

        setSecretRunner((secretToSet) -> {
            CompletableFuture<KeyVaultSecret> future = secretAsyncClient.setSecret(secretToSet);
            KeyVaultSecret response = future.join();
            assertSecretEquals(secretToSet, response);
        });
    }

    /**
     * Tests that we cannot create a secret when the key is an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyName(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        try {
            secretAsyncClient.setSecret("", "A value").join();
            Assertions.fail("Expected HttpResponseException");
        } catch (Exception e) {
            assertRestException(e, HttpResponseException.class, HttpURLConnection.HTTP_BAD_METHOD);
        }
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretEmptyValue(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        setSecretEmptyValueRunner((secretToSet) -> {
            CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secretToSet.getName(), secretToSet.getValue());
            KeyVaultSecret setResponse = setFuture.join();
            assertSecretEquals(secretToSet, setResponse);

            CompletableFuture<KeyVaultSecret> getFuture = secretAsyncClient.getSecret(secretToSet.getName());
            KeyVaultSecret getResponse = getFuture.join();
            assertSecretEquals(secretToSet, getResponse);
        });
    }

    /**
     * Verifies that an exception is thrown when null secret object is passed for creation.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretNull(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        try {
            secretAsyncClient.setSecret(null).join();
            Assertions.fail("Expected NullPointerException");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof NullPointerException);
        }
    }

    /**
     * Tests that a secret can be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        updateSecretRunner((originalSecret, updatedSecret) -> {
            CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(originalSecret);
            setFuture.join();

            CompletableFuture<SecretProperties> updateFuture = secretAsyncClient.updateSecretProperties(updatedSecret.getProperties());
            updateFuture.join();

            CompletableFuture<KeyVaultSecret> getFuture = secretAsyncClient.getSecret(originalSecret.getName());
            KeyVaultSecret getResponse = getFuture.join();
            assertSecretEquals(updatedSecret, getResponse);
        });
    }

    /**
     * Tests that a secret cannot be updated when it is disabled. 403 error is expected.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        updateDisabledSecretRunner((originalSecret, updatedSecret) -> {
            CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(originalSecret);
            setFuture.join();

            try {
                secretAsyncClient.updateSecretProperties(updatedSecret.getProperties()).join();
                Assertions.fail("Expected ResourceModifiedException");
            } catch (Exception e) {
                assertRestException(e, ResourceModifiedException.class, HttpURLConnection.HTTP_FORBIDDEN);
            }
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
            CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secretToGet);
            KeyVaultSecret setResponse = setFuture.join();
            assertSecretEquals(secretToGet, setResponse);

            CompletableFuture<KeyVaultSecret> getFuture = secretAsyncClient.getSecret(secretToGet.getName());
            KeyVaultSecret getResponse = getFuture.join();
            assertSecretEquals(secretToGet, getResponse);
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
            CompletableFuture<KeyVaultSecret> setFuture1 = secretAsyncClient.setSecret(secretWithOriginalValue);
            KeyVaultSecret secretVersionOne = setFuture1.join();

            CompletableFuture<KeyVaultSecret> getFuture1 = secretAsyncClient.getSecret(secretWithOriginalValue.getName(),
                secretVersionOne.getProperties().getVersion());
            KeyVaultSecret getResponse1 = getFuture1.join();
            assertSecretEquals(secretWithOriginalValue, getResponse1);

            CompletableFuture<KeyVaultSecret> setFuture2 = secretAsyncClient.setSecret(secretWithNewValue);
            KeyVaultSecret secretVersionTwo = setFuture2.join();

            CompletableFuture<KeyVaultSecret> getFuture2 = secretAsyncClient.getSecret(secretWithNewValue.getName(),
                secretVersionTwo.getProperties().getVersion());
            KeyVaultSecret getResponse2 = getFuture2.join();
            assertSecretEquals(secretWithNewValue, getResponse2);
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        try {
            secretAsyncClient.getSecret("non-existing").join();
            Assertions.fail("Expected ResourceNotFoundException");
        } catch (Exception e) {
            assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    /**
     * Tests that an existing secret can be deleted.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        deleteSecretRunner((secretToDelete) -> {
            CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secretToDelete);
            KeyVaultSecret setResponse = setFuture.join();
            assertSecretEquals(secretToDelete, setResponse);

            PollerFlux<DeletedSecret, Void> poller = setPlaybackPollerFluxPollInterval(secretAsyncClient.beginDeleteSecret(secretToDelete.getName()));

            AsyncPollResponse<DeletedSecret, Void> lastResponse = poller.getLast().join();
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

        try {
            secretAsyncClient.beginDeleteSecret("non-existing").join();
            Assertions.fail("Expected ResourceNotFoundException");
        } catch (Exception e) {
            assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        getDeletedSecretRunner((secretToDeleteAndGet) -> {
            CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secretToDeleteAndGet);
            KeyVaultSecret setResponse = setFuture.join();
            assertSecretEquals(secretToDeleteAndGet, setResponse);

            PollerFlux<DeletedSecret, Void> poller = setPlaybackPollerFluxPollInterval(
                secretAsyncClient.beginDeleteSecret(secretToDeleteAndGet.getName()));

            poller.getLast().join();

            CompletableFuture<DeletedSecret> getFuture = secretAsyncClient.getDeletedSecret(secretToDeleteAndGet.getName());
            DeletedSecret deletedSecretResponse = getFuture.join();

            assertNotNull(deletedSecretResponse.getDeletedOn());
            assertNotNull(deletedSecretResponse.getRecoveryId());
            assertNotNull(deletedSecretResponse.getScheduledPurgeDate());
            assertEquals(secretToDeleteAndGet.getName(), deletedSecretResponse.getName());
        });
    }

    /**
     * Tests that an attempt to retrieve a non-existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        try {
            secretAsyncClient.getDeletedSecret("non-existing").join();
            Assertions.fail("Expected ResourceNotFoundException");
        } catch (Exception e) {
            assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        recoverDeletedSecretRunner((secretToDeleteAndRecover) -> {
            CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secretToDeleteAndRecover);
            KeyVaultSecret setResponse = setFuture.join();
            assertSecretEquals(secretToDeleteAndRecover, setResponse);

            PollerFlux<DeletedSecret, Void> deletePoller = setPlaybackPollerFluxPollInterval(
                secretAsyncClient.beginDeleteSecret(secretToDeleteAndRecover.getName()));
            deletePoller.getLast().join();

            PollerFlux<KeyVaultSecret, Void> recoverPoller = setPlaybackPollerFluxPollInterval(
                secretAsyncClient.beginRecoverDeletedSecret(secretToDeleteAndRecover.getName()));

            AsyncPollResponse<KeyVaultSecret, Void> lastResponse = recoverPoller.getLast().join();
            KeyVaultSecret secretResponse = lastResponse.getValue();

            assertEquals(secretToDeleteAndRecover.getName(), secretResponse.getName());
            assertEquals(secretToDeleteAndRecover.getProperties().getNotBefore(),
                secretResponse.getProperties().getNotBefore());
            assertEquals(secretToDeleteAndRecover.getProperties().getExpiresOn(),
                secretResponse.getProperties().getExpiresOn());
        });
    }

    /**
     * Tests that an attempt to recover a non-existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        try {
            secretAsyncClient.beginRecoverDeletedSecret("non-existing").join();
            Assertions.fail("Expected ResourceNotFoundException");
        } catch (Exception e) {
            assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        backupSecretRunner((secretToBackup) -> {
            CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secretToBackup);
            KeyVaultSecret setResponse = setFuture.join();
            assertSecretEquals(secretToBackup, setResponse);

            CompletableFuture<byte[]> backupFuture = secretAsyncClient.backupSecret(secretToBackup.getName());
            byte[] backupBytes = backupFuture.join();

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
        createSecretAsyncClient(httpClient, serviceVersion);

        try {
            secretAsyncClient.backupSecret("non-existing").join();
            Assertions.fail("Expected ResourceNotFoundException");
        } catch (Exception e) {
            assertRestException(e, ResourceNotFoundException.class, HttpURLConnection.HTTP_NOT_FOUND);
        }
    }

    /**
     * Tests that a secret can be restored in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createSecretAsyncClient(httpClient, serviceVersion);

        restoreSecretRunner((secretToBackupAndRestore) -> {
            CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secretToBackupAndRestore);
            KeyVaultSecret setResponse = setFuture.join();
            assertSecretEquals(secretToBackupAndRestore, setResponse);

            CompletableFuture<byte[]> backupFuture = secretAsyncClient.backupSecret(secretToBackupAndRestore.getName());
            byte[] backup = backupFuture.join();

            PollerFlux<DeletedSecret, Void> deletePoller = setPlaybackPollerFluxPollInterval(
                secretAsyncClient.beginDeleteSecret(secretToBackupAndRestore.getName()));
            deletePoller.getLast().join();

            CompletableFuture<Void> purgeFuture = secretAsyncClient.purgeDeletedSecret(secretToBackupAndRestore.getName());
            purgeFuture.join();

            pollOnSecretPurge(secretToBackupAndRestore.getName());

            sleepIfRunningAgainstService(60000);

            CompletableFuture<KeyVaultSecret> restoreFuture = secretAsyncClient.restoreSecretBackup(backup);
            KeyVaultSecret restoredSecret = restoreFuture.join();

            assertEquals(secretToBackupAndRestore.getName(), restoredSecret.getName());
            assertEquals(secretToBackupAndRestore.getProperties().getNotBefore(),
                restoredSecret.getProperties().getNotBefore());
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
        createSecretAsyncClient(httpClient, serviceVersion);

        byte[] malformedBackup = "non-existing".getBytes();
        try {
            secretAsyncClient.restoreSecretBackup(malformedBackup).join();
            Assertions.fail("Expected HttpResponseException");
        } catch (Exception e) {
            assertRestException(e, HttpResponseException.class, HttpURLConnection.HTTP_BAD_REQUEST);
        }
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
            for (KeyVaultSecret secret : secretsToSetAndDelete.values()) {
                CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secret);
                KeyVaultSecret setResponse = setFuture.join();
                assertSecretEquals(secret, setResponse);
            }

            sleepIfRunningAgainstService(10000);

            for (KeyVaultSecret secret : secretsToSetAndDelete.values()) {
                PollerFlux<DeletedSecret, Void> poller = setPlaybackPollerFluxPollInterval(secretAsyncClient.beginDeleteSecret(secret.getName()));
                poller.getLast().join();
            }

            sleepIfRunningAgainstService(60000);

            List<DeletedSecret> deletedSecrets = new ArrayList<>();
            secretAsyncClient.listDeletedSecrets().forEach(deletedSecret -> {
                assertNotNull(deletedSecret.getDeletedOn());
                assertNotNull(deletedSecret.getRecoveryId());
                deletedSecrets.add(deletedSecret);
            });

            assertTrue(deletedSecrets.size() >= secretsToSetAndDelete.size());
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

                CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secret);
                KeyVaultSecret setResponse = setFuture.join();
                assertSecretEquals(secret, setResponse);
            }

            sleepIfRunningAgainstService(30000);

            secretAsyncClient.listPropertiesOfSecretVersions(secretName).forEach(secretProperties -> {
                output.add(secretProperties);
            });

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
            for (KeyVaultSecret secret : secretsToSetAndList.values()) {
                CompletableFuture<KeyVaultSecret> setFuture = secretAsyncClient.setSecret(secret);
                KeyVaultSecret setResponse = setFuture.join();
                assertSecretEquals(secret, setResponse);
            }

            sleepIfRunningAgainstService(10000);

            secretAsyncClient.listPropertiesOfSecrets().forEach(secret -> {
                if (secretsToSetAndList.containsKey(secret.getName())) {
                    KeyVaultSecret expectedSecret = secretsToSetAndList.get(secret.getName());

                    assertEquals(expectedSecret.getProperties().getExpiresOn(), secret.getExpiresOn());
                    assertEquals(expectedSecret.getProperties().getNotBefore(), secret.getNotBefore());

                    secretsToSetAndList.remove(secret.getName());
                }
            });

            assertEquals(0, secretsToSetAndList.size());
        });
    }

    private void pollOnSecretPurge(String secretName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 30) {
            sleepIfRunningAgainstService(2000);
            try {
                secretAsyncClient.getDeletedSecret(secretName).join();
            } catch (Exception e) {
                // Expected when fully deleted
                return;
            }
            pendingPollCount += 1;
        }
        System.err.printf("Deleted Secret %s was not purged after polling for 60 seconds.%n", secretName);
    }
}
