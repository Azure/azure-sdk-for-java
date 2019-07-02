// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SecretAsyncClientTest extends SecretClientTestBase {

    private SecretAsyncClient client;

    @Override
    protected void beforeTest() {
        beforeTestSetup();

        if (interceptorManager.isPlaybackMode()) {
            client = clientSetup(credentials -> SecretAsyncClient.builder()
                    .credential(credentials)
                    .endpoint(getEndpoint())
                    .httpClient(interceptorManager.getPlaybackClient())
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .buildAsyncClient());
        } else {
            client = clientSetup(credentials -> SecretAsyncClient.builder()
                    .credential(credentials)
                    .endpoint(getEndpoint())
                    .httpClient(HttpClient.createDefault().wiretap(true))
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .addPolicy(new RetryPolicy())
                    .buildAsyncClient());
        }
    }

    /**
     * Tests that a secret can be created in the key vault.
     */
    public void setSecret() {

        setSecretRunner((expected) -> StepVerifier.create(client.setSecret(expected))
                .assertNext(response -> assertSecretEquals(expected, response))
                .verifyComplete());
    }

    /**
     * Tests that we cannot create a secret when the key is an empty string.
     */
    public void setSecretEmptyName() {
        StepVerifier.create(client.setSecret("", "A value"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, HttpResponseException.class, HttpResponseStatus.METHOD_NOT_ALLOWED.code()));
    }

    /**
     * Tests that we can create secrets when value is not null or an empty string.
     */
    public void setSecretEmptyValue() {
        setSecretEmptyValueRunner((secret) -> {

            StepVerifier.create(client.setSecret(secret.name(), secret.value()))
                .assertNext(response -> assertSecretEquals(secret, response))
                .verifyComplete();

            StepVerifier.create(client.getSecret(secret.name()))
                    .assertNext(response -> assertSecretEquals(secret, response))
                    .verifyComplete();
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
            StepVerifier.create(client.setSecret(original))
                    .assertNext(response -> assertSecretEquals(original, response))
                    .verifyComplete();
            Secret secretToUpdate = client.getSecret(original.name()).block().value();

            StepVerifier.create(client.updateSecret(secretToUpdate.expires(updated.expires())))
                    .assertNext(response -> {
                        assertNotNull(response.value());
                        Assert.assertEquals(original.name(), response.value().name());
                    }).verifyComplete();

            StepVerifier.create(client.getSecret(original.name()))
                    .assertNext(updatedSecretResponse -> assertSecretEquals(updated, updatedSecretResponse))
                    .verifyComplete();
        });
    }

    /**
     * Tests that a secret is not able to be updated when it is disabled. 403 error is expected.
     */
    public void updateDisabledSecret() {
        updateDisabledSecretRunner((original, updated) -> {
            StepVerifier.create(client.setSecret(original))
                    .assertNext(response -> assertSecretEquals(original, response))
                    .verifyComplete();

            StepVerifier.create(client.getSecret(original.name()))
                    .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpResponseStatus.FORBIDDEN.code()));
        });
    }


    /**
     * Tests that an existing secret can be retrieved.
     */
    public void getSecret() {
        getSecretRunner((original) -> {
            client.setSecret(original);
            StepVerifier.create(client.getSecret(original.name()))
                    .assertNext(response -> assertSecretEquals(original, response))
                    .verifyComplete();
        });
    }

    /**
     * Tests that a specific version of the secret can be retrieved.
     */
    public void getSecretSpecificVersion() {
        getSecretSpecificVersionRunner((secret, secretWithNewVal) -> {
            final Secret secretVersionOne = client.setSecret(secret).block().value();
            final Secret secretVersionTwo = client.setSecret(secretWithNewVal).block().value();

            StepVerifier.create(client.getSecret(secret.name(), secretVersionOne.version()))
                    .assertNext(response -> assertSecretEquals(secret, response))
                    .verifyComplete();

            StepVerifier.create(client.getSecret(secretWithNewVal.name(), secretVersionTwo.version()))
                    .assertNext(response -> assertSecretEquals(secretWithNewVal, response))
                    .verifyComplete();
        });
    }

    /**
     * Tests that an attempt to get a non-existing secret throws an error.
     */
    public void getSecretNotFound() {
        StepVerifier.create(client.getSecret("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code()));
    }


    /**
     * Tests that an existing secret can be deleted.
     */
    public void deleteSecret() {
        deleteSecretRunner((secretToDelete) -> {
            StepVerifier.create(client.setSecret(secretToDelete))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToDelete, secretResponse.value());
                    }).verifyComplete();

            StepVerifier.create(client.deleteSecret(secretToDelete.name()))
                    .assertNext(deletedSecretResponse -> {
                        DeletedSecret deletedSecret = deletedSecretResponse.value();
                        assertNotNull(deletedSecret.deletedDate());
                        assertNotNull(deletedSecret.recoveryId());
                        assertNotNull(deletedSecret.scheduledPurgeDate());
                        Assert.assertEquals(secretToDelete.name(), deletedSecret.name());
                    }).verifyComplete();
            sleepInRecordMode(30000);

            StepVerifier.create(client.purgeDeletedSecret(secretToDelete.name()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpResponseStatus.NO_CONTENT.code(), voidResponse.statusCode());
                    }).verifyComplete();
            sleepInRecordMode(15000);
        });
    }

    public void deleteSecretNotFound() {
        StepVerifier.create(client.deleteSecret("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code()));
    }

    /**
     * Tests that a deleted secret can be retrieved on a soft-delete enabled vault.
     */
    public void getDeletedSecret() {
        getDeletedSecretRunner((secretToDeleteAndGet) -> {
            StepVerifier.create(client.setSecret(secretToDeleteAndGet))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToDeleteAndGet, secretResponse.value());
                    }).verifyComplete();

            StepVerifier.create(client.deleteSecret(secretToDeleteAndGet.name()))
                    .assertNext(deletedSecretResponse -> {
                        DeletedSecret deletedSecret = deletedSecretResponse.value();
                        assertNotNull(deletedSecret);
                    }).verifyComplete();
            pollOnSecretDeletion(secretToDeleteAndGet.name());
            sleep(30000);

            StepVerifier.create(client.getDeletedSecret(secretToDeleteAndGet.name()))
                    .assertNext(deletedSecretResponse -> {
                        DeletedSecret deletedSecret = deletedSecretResponse.value();
                        assertNotNull(deletedSecret.deletedDate());
                        assertNotNull(deletedSecret.recoveryId());
                        assertNotNull(deletedSecret.scheduledPurgeDate());
                        Assert.assertEquals(secretToDeleteAndGet.name(), deletedSecret.name());
                    }).verifyComplete();

            StepVerifier.create(client.purgeDeletedSecret(secretToDeleteAndGet.name()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpResponseStatus.NO_CONTENT.code(), voidResponse.statusCode());
                    }).verifyComplete();
            pollOnSecretPurge(secretToDeleteAndGet.name());
            sleep(10000);
        });
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void getDeletedSecretNotFound() {
        StepVerifier.create(client.getDeletedSecret("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code()));
    }

    /**
     * Tests that a deleted secret can be recovered on a soft-delete enabled vault.
     */
    public void recoverDeletedSecret() {
        recoverDeletedSecretRunner((secretToDeleteAndRecover) -> {
            StepVerifier.create(client.setSecret(secretToDeleteAndRecover))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToDeleteAndRecover, secretResponse.value());
                    }).verifyComplete();

            StepVerifier.create(client.deleteSecret(secretToDeleteAndRecover.name()))
                    .assertNext(deletedSecretResponse -> {
                        DeletedSecret deletedSecret = deletedSecretResponse.value();
                        assertNotNull(deletedSecret);
                    }).verifyComplete();
            sleepInRecordMode(30000);

            StepVerifier.create(client.recoverDeletedSecret(secretToDeleteAndRecover.name()))
                    .assertNext(secretResponse -> {
                        Secret recoveredSecret = secretResponse.value();
                        Assert.assertEquals(secretToDeleteAndRecover.name(), recoveredSecret.name());
                        Assert.assertEquals(secretToDeleteAndRecover.notBefore(), recoveredSecret.notBefore());
                        Assert.assertEquals(secretToDeleteAndRecover.expires(), recoveredSecret.expires());
                    }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void recoverDeletedSecretNotFound() {
        StepVerifier.create(client.recoverDeletedSecret("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code()));
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    public void backupSecret() {
        backupSecretRunner((secretToBackup) -> {
            StepVerifier.create(client.setSecret(secretToBackup))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToBackup, secretResponse.value());
                    }).verifyComplete();

            StepVerifier.create(client.backupSecret(secretToBackup.name()))
                    .assertNext(response -> {
                        byte[] backupBytes = response.value();
                        assertNotNull(backupBytes);
                        assertTrue(backupBytes.length > 0);
                    }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to backup a non existing secret throws an error.
     */
    public void backupSecretNotFound() {
        StepVerifier.create(client.backupSecret("non-existing"))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceNotFoundException.class, HttpResponseStatus.NOT_FOUND.code()));
    }

    /**
     * Tests that a secret can be backed up in the key vault.
     */
    public void restoreSecret() {
        restoreSecretRunner((secretToBackupAndRestore) -> {
            StepVerifier.create(client.setSecret(secretToBackupAndRestore))
                    .assertNext(secretResponse -> {
                        assertSecretEquals(secretToBackupAndRestore, secretResponse.value());
                    }).verifyComplete();
            byte[] backup = client.backupSecret(secretToBackupAndRestore.name()).block().value();

            StepVerifier.create(client.deleteSecret(secretToBackupAndRestore.name()))
                    .assertNext(deletedSecretResponse -> {
                        DeletedSecret deletedSecret = deletedSecretResponse.value();
                        assertNotNull(deletedSecret);
                    }).verifyComplete();
            pollOnSecretDeletion(secretToBackupAndRestore.name());

            StepVerifier.create(client.purgeDeletedSecret(secretToBackupAndRestore.name()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpResponseStatus.NO_CONTENT.code(), voidResponse.statusCode());
                    }).verifyComplete();
            pollOnSecretPurge(secretToBackupAndRestore.name());

            sleep(60000);

            StepVerifier.create(client.restoreSecret(backup))
                    .assertNext(response -> {
                        Secret restoredSecret = response.value();
                        Assert.assertEquals(secretToBackupAndRestore.name(), restoredSecret.name());
                        Assert.assertEquals(secretToBackupAndRestore.notBefore(), restoredSecret.notBefore());
                        Assert.assertEquals(secretToBackupAndRestore.expires(), restoredSecret.expires());
                    }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to restore a secret from malformed backup bytes throws an error.
     */
    public void restoreSecretFromMalformedBackup() {
        byte[] secretBackupBytes = "non-existing".getBytes();
        StepVerifier.create(client.restoreSecret(secretBackupBytes))
                .verifyErrorSatisfies(ex -> assertRestException(ex, ResourceModifiedException.class, HttpResponseStatus.BAD_REQUEST.code()));
    }

    /**
     * Tests that deleted secrets can be listed in the key vault.
     */
    @Override
    public void listDeletedSecrets() {
        listDeletedSecretsRunner((secrets) -> {
            HashMap<String, Secret> secretsToDelete = secrets;
            List<DeletedSecret> deletedSecrets = new ArrayList<>();

            for (Secret secret : secretsToDelete.values()) {
                StepVerifier.create(client.setSecret(secret))
                        .assertNext(secretResponse -> {
                            assertSecretEquals(secret, secretResponse.value());
                        }).verifyComplete();
            }
            sleepInRecordMode(10000);

            for (Secret secret : secretsToDelete.values()) {
                StepVerifier.create(client.deleteSecret(secret.name()))
                        .assertNext(deletedSecretResponse -> {
                            DeletedSecret deletedSecret = deletedSecretResponse.value();
                            assertNotNull(deletedSecret);
                        }).verifyComplete();
                pollOnSecretDeletion(secret.name());
            }

            sleepInRecordMode(35000);
            client.listDeletedSecrets().subscribe(deletedSecrets::add);
            sleepInRecordMode(30000);

            for (DeletedSecret actualSecret : deletedSecrets) {
                if (secretsToDelete.containsKey(actualSecret.name())) {
                    assertNotNull(actualSecret.deletedDate());
                    assertNotNull(actualSecret.recoveryId());
                    secretsToDelete.remove(actualSecret.name());
                }
            }

            assertEquals(0, secretsToDelete.size());

            for (DeletedSecret deletedSecret : deletedSecrets) {
                StepVerifier.create(client.purgeDeletedSecret(deletedSecret.name()))
                        .assertNext(voidResponse -> {
                            assertEquals(HttpResponseStatus.NO_CONTENT.code(), voidResponse.statusCode());
                        }).verifyComplete();
                pollOnSecretPurge(deletedSecret.name());
            }
        });
    }

    /**
     * Tests that secret versions can be listed in the key vault.
     */
    @Override
    public void listSecretVersions() {
        listSecretVersionsRunner((secrets) -> {
            List<Secret> secretVersions = secrets;
            List<SecretBase> output = new ArrayList<>();
            String secretName = null;
            for (Secret secret : secretVersions) {
                secretName = secret.name();
                client.setSecret(secret).subscribe(secretResponse -> assertSecretEquals(secret, secretResponse.value()));
                sleepInRecordMode(1000);
            }
            sleep(30000);
            client.listSecretVersions(secretName).subscribe(output::add);
            sleep(30000);

            assertEquals(secretVersions.size(), output.size());

            StepVerifier.create(client.deleteSecret(secretName))
                    .assertNext(deletedSecretResponse -> {
                        DeletedSecret deletedSecret = deletedSecretResponse.value();
                        assertNotNull(deletedSecret);
                    }).verifyComplete();
            pollOnSecretDeletion(secretName);


            StepVerifier.create(client.purgeDeletedSecret(secretName))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpResponseStatus.NO_CONTENT.code(), voidResponse.statusCode());
                    }).verifyComplete();
            pollOnSecretPurge(secretName);
        });

    }

    /**
     * Tests that secrets can be listed in the key vault.
     */
    public void listSecrets() {
        listSecretsRunner((secrets) -> {
            HashMap<String, Secret> secretsToList = secrets;
            List<SecretBase> output = new ArrayList<>();
            for (Secret secret : secretsToList.values()) {
                client.setSecret(secret).subscribe(secretResponse -> assertSecretEquals(secret, secretResponse.value()));
                sleepInRecordMode(1000);
            }
            sleep(30000);
            client.listSecrets().subscribe(output::add);
            sleep(30000);

            for (SecretBase actualSecret : output) {
                if (secretsToList.containsKey(actualSecret.name())) {
                    Secret expectedSecret = secrets.get(actualSecret.name());
                    assertEquals(expectedSecret.expires(), actualSecret.expires());
                    assertEquals(expectedSecret.notBefore(), actualSecret.notBefore());
                    secrets.remove(actualSecret.name());
                }
            }
            assertEquals(0, secrets.size());
        });
    }

    private DeletedSecret pollOnSecretDeletion(String secretName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 30) {
            DeletedSecret deletedSecret = null;
            try {
                deletedSecret = client.getDeletedSecret(secretName).block().value();
            } catch (ResourceNotFoundException e) {
            }
            if (deletedSecret == null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
                continue;
            } else {
                return deletedSecret;
            }
        }
        System.err.printf("Deleted Secret %s not found \n", secretName);
        return null;
    }

    private DeletedSecret pollOnSecretPurge(String secretName) {
        int pendingPollCount = 0;
        while (pendingPollCount < 10) {
            DeletedSecret deletedSecret = null;
            try {
                deletedSecret = client.getDeletedSecret(secretName).block().value();
            } catch (ResourceNotFoundException e) {
            }
            if (deletedSecret != null) {
                sleepInRecordMode(2000);
                pendingPollCount += 1;
                continue;
            } else {
                return deletedSecret;
            }
        }
        System.err.printf("Deleted Secret %s was not purged \n", secretName);
        return null;
    }
}
