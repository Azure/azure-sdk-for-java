// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import com.azure.v2.security.keyvault.secrets.models.DeletedSecret;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.v2.security.keyvault.secrets.models.SecretProperties;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;
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
    private static final ClientLogger LOGGER = new ClientLogger(SecretClientTest.class);

    private SecretClient secretClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    private void createClient(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion, null);
    }

    private void createClient(HttpClient httpClient, SecretServiceVersion serviceVersion, String testTenantId) {
        secretClient = getClientBuilder(httpClient, testTenantId, getEndpoint(), serviceVersion).buildClient();

        if (!interceptorManager.isLiveMode()) {
            // Remove `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }
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

        assertRestException(() -> secretClient.setSecret("", "A value"), HttpResponseException.class,
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
            KeyVaultSecret secret = secretClient.setSecret(secretToSet);
            assertEquals(secretToSet.getName(), secret.getName());
            assertEquals(secretToSet.getValue(), secret.getValue());
        });
    }

    /**
     * Tests that we cannot create a secret when the secret is null.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void setSecretNull(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertThrows(NullPointerException.class, () -> secretClient.setSecret(null));
    }

    /**
     * Tests that a secret is able to be updated when it exists.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        updateSecretRunner((originalSecret, updatedSecret) -> {
            assertSecretEquals(originalSecret, secretClient.setSecret(originalSecret));
            SecretProperties secretProperties = secretClient.updateSecretProperties(updatedSecret.getProperties());
            assertEquals(updatedSecret.getName(), secretProperties.getName());
            assertEquals(updatedSecret.getProperties().getExpiresOn(), secretProperties.getExpiresOn());
        });
    }

    /**
     * Tests that a secret is able to be updated when it is disabled.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void updateDisabledSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        updateDisabledSecretRunner((originalSecret, updatedSecret) -> {
            assertSecretEquals(originalSecret, secretClient.setSecret(originalSecret));
            SecretProperties secretProperties = secretClient.updateSecretProperties(updatedSecret.getProperties());
            assertEquals(updatedSecret.getName(), secretProperties.getName());
            assertEquals(updatedSecret.getProperties().getExpiresOn(), secretProperties.getExpiresOn());
        });
    }

    /**
     * Tests that an existing secret can be retrieved.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        getSecretRunner((original) -> {
            secretClient.setSecret(original);
            assertSecretEquals(original, secretClient.getSecret(original.getName()));
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
            KeyVaultSecret originalSecret = secretClient.setSecret(secretWithOriginalValue);
            KeyVaultSecret updatedSecret = secretClient.setSecret(secretWithNewValue);
            assertSecretEquals(secretWithNewValue, updatedSecret);
            assertSecretEquals(secretWithOriginalValue, secretClient.getSecret(originalSecret.getName(), originalSecret.getProperties().getVersion()));
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.getSecret("non-existing"), HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
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

            DeletedSecret deletedSecret = secretClient.beginDeleteSecret(secretToDelete.getName()).getFinalResult();
            assertNotNull(deletedSecret.getDeletedOn());
            assertNotNull(deletedSecret.getRecoveryId());
            assertNotNull(deletedSecret.getScheduledPurgeDate());
            assertEquals(secretToDelete.getName(), deletedSecret.getName());
        });
    }

    /**
     * Tests that an attempt to delete a non-existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void deleteSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.beginDeleteSecret("non-existing"), HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
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

            DeletedSecret deletedSecret = secretClient.beginDeleteSecret(secretToDeleteAndGet.getName()).getFinalResult();
            DeletedSecret retrievedDeletedSecret = secretClient.getDeletedSecret(secretToDeleteAndGet.getName());
            assertEquals(deletedSecret.getName(), retrievedDeletedSecret.getName());
            assertEquals(deletedSecret.getValue(), retrievedDeletedSecret.getValue());
            assertNotNull(retrievedDeletedSecret.getDeletedOn());
            assertNotNull(retrievedDeletedSecret.getRecoveryId());
            assertNotNull(retrievedDeletedSecret.getScheduledPurgeDate());
        });
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void getDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.getDeletedSecret("non-existing"), HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
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

            DeletedSecret deletedSecret = secretClient.beginDeleteSecret(secretToDeleteAndRecover.getName()).getFinalResult();
            KeyVaultSecret recoveredSecret = secretClient.beginRecoverDeletedSecret(secretToDeleteAndRecover.getName()).getFinalResult();
            assertEquals(secretToDeleteAndRecover.getName(), recoveredSecret.getName());
            assertEquals(secretToDeleteAndRecover.getValue(), recoveredSecret.getValue());
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void recoverDeletedSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.beginRecoverDeletedSecret("non-existing"), HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
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

            byte[] backupBytes = secretClient.backupSecret(secretToBackup.getName());
            assertNotNull(backupBytes);
            assertTrue(backupBytes.length > 0);
        });
    }

    /**
     * Tests that an attempt to backup a non-existing secret throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void backupSecretNotFound(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        assertRestException(() -> secretClient.backupSecret("non-existing"), HttpResponseException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that a secret can be restored in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreSecret(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        restoreSecretRunner((secretToBackupAndRestore) -> {
            assertSecretEquals(secretToBackupAndRestore, secretClient.setSecret(secretToBackupAndRestore));

            byte[] backupBytes = secretClient.backupSecret(secretToBackupAndRestore.getName());
            secretClient.beginDeleteSecret(secretToBackupAndRestore.getName()).getFinalResult();
            secretClient.purgeDeletedSecret(secretToBackupAndRestore.getName());

            sleepIfRunningAgainstService(10000);

            KeyVaultSecret restoredSecret = secretClient.restoreSecretBackup(backupBytes);
            assertEquals(secretToBackupAndRestore.getName(), restoredSecret.getName());
            assertEquals(secretToBackupAndRestore.getValue(), restoredSecret.getValue());
        });
    }

    /**
     * Tests that an attempt to restore a secret from malformed backup bytes throws an error.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void restoreSecretFromMalformedBackup(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        byte[] malformedBytes = "non-existing".getBytes();
        assertRestException(() -> secretClient.restoreSecretBackup(malformedBytes), HttpResponseException.class, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    /**
     * Tests that secrets can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        listSecretsRunner((secrets) -> {
            secrets.forEach((secretName, secret) -> {
                assertSecretEquals(secret, secretClient.setSecret(secret));
            });

            List<SecretProperties> secretProperties = new ArrayList<>();
            for (SecretProperties secretProperty : secretClient.listPropertiesOfSecrets()) {
                if (secrets.containsKey(secretProperty.getName())) {
                    secretProperties.add(secretProperty);
                }
            }

            assertEquals(secrets.size(), secretProperties.size());
        });
    }

    /**
     * Tests that deleted secrets can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listDeletedSecrets(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        listDeletedSecretsRunner((secrets) -> {
            secrets.forEach((secretName, secret) -> {
                assertSecretEquals(secret, secretClient.setSecret(secret));
            });

            secrets.forEach((secretName, secret) -> {
                secretClient.beginDeleteSecret(secretName).getFinalResult();
            });

            sleepIfRunningAgainstService(10000);

            List<DeletedSecret> deletedSecrets = new ArrayList<>();
            for (DeletedSecret deletedSecret : secretClient.listDeletedSecrets()) {
                if (secrets.containsKey(deletedSecret.getName())) {
                    deletedSecrets.add(deletedSecret);
                }
            }

            assertEquals(secrets.size(), deletedSecrets.size());
        });
    }

    /**
     * Tests that secret versions can be listed in the key vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getTestParameters")
    public void listSecretVersions(HttpClient httpClient, SecretServiceVersion serviceVersion) {
        createClient(httpClient, serviceVersion);

        listSecretVersionsRunner((secrets) -> {
            String secretName = null;
            for (KeyVaultSecret secret : secrets) {
                secretName = secret.getName();
                assertSecretEquals(secret, secretClient.setSecret(secret));
            }

            List<SecretProperties> secretVersions = new ArrayList<>();
            for (SecretProperties secretProperty : secretClient.listPropertiesOfSecretVersions(secretName)) {
                secretVersions.add(secretProperty);
            }

            assertEquals(secrets.size(), secretVersions.size());
        });
    }
}
