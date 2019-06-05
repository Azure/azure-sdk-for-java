package com.azure.keyvault;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.keyvault.models.DeletedSecret;
import com.azure.keyvault.models.Secret;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.test.StepVerifier;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

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
                    .build());
        } else {
            client = clientSetup(credentials -> SecretAsyncClient.builder()
                    .credential(credentials)
                    .endpoint(getEndpoint())
                    .httpClient(HttpClient.createDefault().wiretap(true))
                    .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .addPolicy(new RetryPolicy())
                    .build());
        }
    }

    @Override
    protected void afterTest() {

       // for (SecretBase secret : client.listSecrets()) {
            //   client.deleteSecret(secret.name());
      //  }
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
                        assertEquals(original.name(), response.value().name());
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
        StepVerifier.create(client.getSecret("non-existing-secret"))
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
                        assertEquals(secretToDelete.name(), deletedSecret.name());
                    }).verifyComplete();
            sleep(30000);

            StepVerifier.create(client.purgeDeletedSecret(secretToDelete.name()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpResponseStatus.NO_CONTENT.code(),voidResponse.statusCode());
                    }).verifyComplete();
            sleep(15000);
        });
    }

    public void deleteSecretNotFound() {
        StepVerifier.create(client.deleteSecret("non-existing-secret"))
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
            sleep(30000);

            StepVerifier.create(client.getDeletedSecret(secretToDeleteAndGet.name()))
                    .assertNext(deletedSecretResponse -> {
                        DeletedSecret deletedSecret = deletedSecretResponse.value();
                        assertNotNull(deletedSecret.deletedDate());
                        assertNotNull(deletedSecret.recoveryId());
                        assertNotNull(deletedSecret.scheduledPurgeDate());
                        assertEquals(secretToDeleteAndGet.name(), deletedSecret.name());
                    }).verifyComplete();

            StepVerifier.create(client.purgeDeletedSecret(secretToDeleteAndGet.name()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpResponseStatus.NO_CONTENT.code(),voidResponse.statusCode());
                    }).verifyComplete();
            sleep(15000);
        });
    }

    /**
     * Tests that an attempt to retrieve a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void getDeletedSecretNotFound() {
        StepVerifier.create(client.getDeletedSecret("non-existing-secret"))
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
            sleep(30000);

            StepVerifier.create(client.recoverDeletedSecret(secretToDeleteAndRecover.name()))
                    .assertNext(secretResponse -> {
                        Secret recoveredSecret = secretResponse.value();
                        assertEquals(secretToDeleteAndRecover.name(), recoveredSecret.name());
                        assertEquals(secretToDeleteAndRecover.notBefore(), recoveredSecret.notBefore());
                        assertEquals(secretToDeleteAndRecover.expires(), recoveredSecret.expires());
                    }).verifyComplete();
        });
    }

    /**
     * Tests that an attempt to recover a non existing deleted secret throws an error on a soft-delete enabled vault.
     */
    public void recoverDeletedSecretNotFound() {
        StepVerifier.create(client.recoverDeletedSecret("non-existing-secret"))
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
        StepVerifier.create(client.backupSecret("non-existing-secret"))
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
            sleep(30000);

            StepVerifier.create(client.purgeDeletedSecret(secretToBackupAndRestore.name()))
                    .assertNext(voidResponse -> {
                        assertEquals(HttpResponseStatus.NO_CONTENT.code(),voidResponse.statusCode());
                    }).verifyComplete();
            sleep(15000);

            StepVerifier.create(client.restoreSecret(backup))
                    .assertNext(response -> {
                        Secret restoredSecret = response.value();
                        assertEquals(secretToBackupAndRestore.name(), restoredSecret.name());
                        assertEquals(secretToBackupAndRestore.notBefore(), restoredSecret.notBefore());
                        assertEquals(secretToBackupAndRestore.expires(), restoredSecret.expires());
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

    @Override
    public void listSecrets() {

    }




//
//    /**
//     * Tests that secrets can be listed in the key vault.
//     */
//    public void listSecrets() {
//        listSecretsRunner((secrets) -> {
//            HashMap<String, Secret> secretsToList = secrets;
//            for(Secret secret :  secretsToList.values()){
//                client.setSecret(secret);
//                sleep(1000);
//            }
//
//            client.listSecrets().subscribe(actualSecret -> {
//                if(secretsToList.containsKey(actualSecret.name())){
//                    Secret expectedSecret = secretsToList.get(actualSecret.name());
//                    assertEquals(expectedSecret.expires(), actualSecret.expires());
//                    assertEquals(expectedSecret.notBefore(), actualSecret.notBefore());
//                    secretsToList.remove(actualSecret.name());
//                }
//            });
//            sleep(10000);
//            assertEquals(0,secretsToList.size());
//        });
//    }
}
